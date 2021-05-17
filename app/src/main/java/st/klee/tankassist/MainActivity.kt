package st.klee.tankassist

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import st.klee.tankassist.api.ApiEndpoint
import st.klee.tankassist.api.StationListResponse
import st.klee.tankassist.api.parse
import st.klee.tankassist.data.FuelType
import st.klee.tankassist.data.LatLng
import st.klee.tankassist.data.Station
import st.klee.tankassist.intro.IntroActivity
import st.klee.tankassist.misc.Permissions
import st.klee.tankassist.service.GeocodingUtil
import java.lang.Integer.max
import java.text.DateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private var queue: RequestQueue? = null
    private val stationList: MutableList<Station> = mutableListOf()
    protected var currentLoc: LatLng? = null
    private var invokedByIntent: Boolean = false
    private var firstRun: Boolean? = null
    private var searchText: String = ""

    // location
    private var locationCancelTokenSource: CancellationTokenSource? = null

    // UI
    private var searchContainer: TextInputLayout? = null
    private var search: TextInputEditText? = null
    private var currentLocView: MaterialTextView? = null
    private var stationView: RecyclerView? = null
    private var lastStationUpdateView: MaterialTextView? = null
    private var swipeToRefresh: SwipeRefreshLayout? = null

    enum class SortKey {
        DISTANCE,
        PRICE_DIESEL,
        PRICE_E5,
        PRICE_E10
    }

    private var sortKey: SortKey = SortKey.DISTANCE


    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // network queue
        queue = Volley.newRequestQueue(this)

        // station list view setup
        stationView = findViewById(R.id.stations)
        stationView!!.layoutManager = LinearLayoutManager(this)
        stationView!!.adapter = StationListAdapter(stationList)
        lastStationUpdateView = findViewById(R.id.last_update)

        // location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // click handler for search
        searchContainer = findViewById(R.id.search_container)
        search = findViewById(R.id.search)
        // "locate" icon in edit text
        searchContainer!!.setStartIconOnClickListener { requestStationListForCurrentLocation() }
        // clicking "search" icon on keyboard or pressing enter after entering text in edit text
        search!!.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_NULL) {
                if (event != null && event.action != KeyEvent.ACTION_DOWN)
                    return@setOnEditorActionListener false

                searchText = v.text.toString()
                // get coordinates from text input
                if (searchText.isNotBlank()) {
                    requestStationList(GeocodingUtil.latLngForAddress(searchText))
                    //
                    search!!.clearFocus()
                    hideKeyboard(search!!)
                }
                true
            } else false
        }
        // swipe-to-refresh
        swipeToRefresh = findViewById(R.id.swipe_refresh)
        swipeToRefresh!!.setOnRefreshListener {
            if (searchText.isNotBlank())
                requestStationList(GeocodingUtil.latLngForAddress(searchText))
        }

        // other UI references
        currentLocView = findViewById(R.id.current_location)

        // app bar handlers
        val appBar = findViewById<MaterialToolbar>(R.id.app_bar)
        setSupportActionBar(appBar)
        appBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                }
                R.id.about -> {
                    startActivity(Intent(this, AboutActivity::class.java))
                }
                R.id.distance -> {
                    sortKey = SortKey.DISTANCE
                }
                R.id.price_diesel -> {
                    sortKey = SortKey.PRICE_DIESEL
                }
                R.id.price_e10 -> {
                    sortKey = SortKey.PRICE_E10
                }
                R.id.price_e5 -> {
                    sortKey = SortKey.PRICE_E5
                }
                else -> return@setOnMenuItemClickListener false
            }
            updateSortView(appBar.menu)
            true
        }

        // handle direct links from assistant
        val action = intent?.action
        val data = intent?.data
        if (action != null && data != null)
            handleIntent(action, data)
    }

    override fun onResume() {
        super.onResume()

        firstRun = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.pref_firstRun), true)

        if (firstRun == true)
            startActivity(Intent(this, IntroActivity::class.java))
        else {
            if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
                    getString(R.string.pref_requestCurrentLocationOnResume),
                    false
                ) && !invokedByIntent
            )
                // do not want to annoy the user if location permission is not granted
                requestStationListForCurrentLocation(true)
        }
    }

    override fun onPause() {
        super.onPause()

        invokedByIntent = false
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.app_bar, menu)
        if (menu != null)
            updateSortView(menu)
        return true
    }

    private fun hideKeyboard(view: View) {
        val inputMethodManager: InputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0);
    }

    private fun updateSortView(menu: Menu) {
        val sortMenu = menu.findItem(R.id.sort).subMenu as Menu
        for (i in 0 until sortMenu.size())
            if (sortMenu[i].isCheckable)
                sortMenu[i].isChecked = false
        val id: Int =
            when (sortKey) {
                SortKey.DISTANCE -> R.id.distance
                SortKey.PRICE_DIESEL -> R.id.price_diesel
                SortKey.PRICE_E5 -> R.id.price_e5
                SortKey.PRICE_E10 -> R.id.price_e10
            }
        sortMenu.findItem(id).isChecked = true

        // sort and update station list
        sortByUserPref(stationList)
        this.stationView!!.adapter?.notifyDataSetChanged()
    }

    private fun sortByUserPref(stations: MutableList<Station>): List<Station> {
        when (sortKey) {
            SortKey.DISTANCE -> {
                if (currentLoc != null)
                    // if user has location features enabled, sort by the user's actual location
                    stations.sortBy { it.latLong.distanceTo(currentLoc!!) }
                else
                    // otherwise sort by distance returned from API (i.e. distance from search term)
                    stations.sortBy { it.dist}
            }
            SortKey.PRICE_DIESEL -> stations.sortBy { it.prices[FuelType.Diesel]!!.cost ?: Double.MAX_VALUE }
            SortKey.PRICE_E5 -> stations.sortBy { it.prices[FuelType.E5]!!.cost ?: Double.MAX_VALUE }
            SortKey.PRICE_E10 -> stations.sortBy { it.prices[FuelType.E10]!!.cost ?: Double.MAX_VALUE }
        }
        return stations
    }

    private fun setProgressIndicator(show: Boolean) {
        findViewById<CircularProgressIndicator>(R.id.progress_updating)?.visibility = if (show) VISIBLE else INVISIBLE
        if (!show)
            swipeToRefresh?.isRefreshing = false
    }

    private fun cancelLocationRequest() {
        Log.d("TANKASSIST", "Cancelling locating task")
        locationCancelTokenSource?.cancel()
    }

    private fun handleIntent(action: String, data: Uri) {
        val host = data.host
        if (host == "find") {
            val desc = data.getQueryParameter("desc")

            // only filling stations
            if (desc != "FillingStation")
                return

            val locationName = data.getQueryParameter("locationName")
            val locationAddress = data.getQueryParameter("locationAddress")
            val locationLat = data.getQueryParameter("locationLat")
            val locationLng = data.getQueryParameter("locationLng")

            Log.d("TANKASSIST", "query find: $desc, $locationName, $locationAddress, $locationLat, $locationLat, $locationLng")

            invokedByIntent = true

            if (locationName.isNullOrBlank() && locationAddress.isNullOrBlank() && locationLat.isNullOrBlank() && locationLng.isNullOrBlank()) {
                // no location specified
                return requestStationListForCurrentLocation()
            } else {
                val latLng = if (!locationAddress.isNullOrBlank())
                    GeocodingUtil.latLngForAddress(locationAddress)
                else if (!locationName.isNullOrBlank())
                    GeocodingUtil.latLngForAddress(locationName)
                else
                    LatLng(locationLat!!.toDouble(), locationLng!!.toDouble())

                requestStationList(latLng)

                // update location for distance calculation, but only if user granted location permission
                requestLocationUpdate({
                    stationView!!.adapter?.notifyDataSetChanged()
                }, true)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                requestStationListForCurrentLocation()
            } else {
                // dialog about INFO that location access was denied
                MaterialAlertDialogBuilder(this)
                    .setTitle(getString(R.string.dialog_result_location_title))
                    .setMessage(getString(R.string.dialog_result_location_message))
                    .setNeutralButton(getString(R.string.dialog_result_location_btn_ok)) { dialog, which ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }

    private fun requestStationListForCurrentLocation(ignoreIfNotPermitted: Boolean = false) {
        // cancel any pending request to minimize device load
        cancelLocationRequest()

        requestLocationUpdate({ location ->
            // request station list from server
            requestStationList(LatLng.FromLocation(location))
        }, ignoreIfNotPermitted)
    }

    private fun requestLocationUpdate(gotLocation: (l: Location) -> Unit, ignoreIfNotPermitted: Boolean = false) {
        Log.d("TANKASSIST", "getCurrentLocation")
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) {
                // check that location services are available
                fusedLocationClient.locationAvailability.addOnSuccessListener { locationAvailability ->
                    if (locationAvailability == null || !locationAvailability.isLocationAvailable) {
                        // dialog about INFO that location services are not available
                        MaterialAlertDialogBuilder(this)
                            .setTitle(getString(R.string.dialog_result_location_avail_title))
                            .setMessage(getString(R.string.dialog_result_location_avail_message))
                            .setNeutralButton(getString(R.string.dialog_result_location_avail_btn_ok)) { dialog, which ->
                                dialog.dismiss()
                            }
                            .show()
                    } else {
                        Log.d("TANKASSIST", "Requesting current location")
                        // request actual location
                        setProgressIndicator(true)
                        locationCancelTokenSource = CancellationTokenSource()
                        val currentLocation = fusedLocationClient.getCurrentLocation(
                            LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY,
                            locationCancelTokenSource!!.token
                        )
                        currentLocation.addOnSuccessListener { location ->
                            setProgressIndicator(false)
                            val processLocation: (location: Location) -> Unit = { l ->

                                currentLoc = LatLng(l.latitude, l.longitude)

                                // display current location
                                currentLoc?.let { currentLocView!!.text = it.toString() }

                                // call callback
                                gotLocation(l)
                            }
                            if (location == null) {
                                Log.i("TANKASSIST", "Acquiring fresh failed; using last location");
                                // try getting cached location if active lookup fails
                                fusedLocationClient.lastLocation.addOnSuccessListener { lastLocation ->
                                    if (lastLocation != null)
                                        processLocation(lastLocation)
                                    else
                                        Log.w(
                                            "TANKASSIST",
                                            "Acquiring fresh and last location failed with null"
                                        );
                                }
                            } else
                                processLocation(location)
                        }
                        .addOnFailureListener { exception ->
                            setProgressIndicator(false)
                            Toast.makeText(
                                this,
                                getString(R.string.toast_location_failed) + exception,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
        } else if (!ignoreIfNotPermitted) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                // dialog about WHY we request location permissions
                Permissions.requestLocationPermission(this, requestPermissionLauncher)
            } else {
                // request permission
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun requestStationList(latLng: LatLng?) {
        setProgressIndicator(true)

        // cancel any pending request to avoid the request's callback overwriting results here
        cancelLocationRequest()

        // if no location was supplied, skip the request and display an empty station list
        if (latLng == null) {
            setProgressIndicator(false)

            this.stationList.clear()
            this.stationView!!.adapter?.notifyDataSetChanged()
            this.lastStationUpdateView!!.text = ""
            return
        }

        Log.d("TANKASSIST", "requestStationList: $latLng")

        // request new list of nearby stations
        Log.d("TANKASSIST", ApiEndpoint.list(latLng, this))
        val req = JsonObjectRequest(Request.Method.GET, ApiEndpoint.list(latLng, this), null,
            { response ->
                setProgressIndicator(false)

                val stationList = StationListResponse().parse(response) as List<Station>
                this.stationList.clear()
                this.stationList.addAll(stationList)
                sortByUserPref(this.stationList)
                this.stationView!!.adapter?.notifyDataSetChanged()
                this.lastStationUpdateView!!.text = DateFormat.getDateTimeInstance().format(Date())
            },
            { error ->
                setProgressIndicator(false)
                Toast.makeText(this, getString(R.string.toast_network_error) + error.toString(), Toast.LENGTH_LONG).show()
            }
        )
        queue?.add(req)
    }

    inner class StationListAdapter(private val dataSet: List<Station>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val name: TextView = view.findViewById(R.id.name)
            val location: TextView = view.findViewById(R.id.location)
            val distance: TextView = view.findViewById(R.id.distance)
            val fuelA: View = view.findViewById(R.id.fuel_a)
            val fuelB: View = view.findViewById(R.id.fuel_b)
            val fuelC: View = view.findViewById(R.id.fuel_c)

            val fuelACost: TextView = fuelA.findViewById(R.id.cost)
            val fuelAType: TextView = fuelA.findViewById(R.id.type)

            val fuelBCost: TextView = fuelB.findViewById(R.id.cost)
            val fuelBType: TextView = fuelB.findViewById(R.id.type)

            val fuelCCost: TextView = fuelC.findViewById(R.id.cost)
            val fuelCType: TextView = fuelC.findViewById(R.id.type)
        }

        inner class EmptyViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            if (viewType == EMPTY_VIEW)
                return EmptyViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.station_empty,
                        parent,
                        false
                    )
                )
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.station,
                    parent,
                    false
                )
            )
        }

        override fun getItemViewType(position: Int): Int {
            if (dataSet.isEmpty())
                return EMPTY_VIEW;
            return super.getItemViewType(position)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder as? ViewHolder != null) {
                val station = dataSet[position]
                holder.name.text = station.name
                // TODO display detailed or truncated location depending on current location
                val stationAddress = station.formatAddress(this@MainActivity)
                holder.location.text = stationAddress

                val myLoc = this@MainActivity.currentLoc
                Log.d("TANKASSIST", "myloc: $myLoc")
                if (myLoc != null) {
                    val distance = myLoc.distanceTo(station.latLong)
                    Log.d("TANKASSIST", "Distance: $distance")
                    holder.distance.text = getString(R.string.distance, distance / 1000)
                } else
                    holder.distance.text = ""

                holder.fuelACost.text = station.prices[FuelType.Diesel]?.cost?.toString() ?: "-.---"
                holder.fuelAType.text = FuelType.Diesel.toString()

                holder.fuelBCost.text = station.prices[FuelType.E5]?.cost?.toString() ?: "-.---"
                holder.fuelBType.text = FuelType.E5.toString()

                holder.fuelCCost.text = station.prices[FuelType.E10]?.cost?.toString() ?: "-.---"
                holder.fuelCType.text = FuelType.E10.toString()

                // clicking on item starts map view intent for selected station
                holder.itemView.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse("geo:0,0?q=" + Uri.encode(station.name + ", " + stationAddress))
                    startActivity(intent)
                }
            }
        }

        override fun getItemCount() = max(dataSet.size, 1)

        private val EMPTY_VIEW = -1;
    }
}