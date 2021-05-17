package st.klee.tankassist.service

import android.app.Application
import android.location.Geocoder
import android.util.Log
import android.widget.Toast
import st.klee.tankassist.MyApplication
import st.klee.tankassist.R
import st.klee.tankassist.data.LatLng
import java.util.*

object GeocodingUtil {
    // FIXME Forcing Germany to always receive addresses in Germany. This is ok, since we are currently limited to Germany anyways.
    private val geocoder: Geocoder = Geocoder(MyApplication.instance, Locale.GERMANY)

    fun latLngForAddress(address: String) : LatLng? {
        Log.d("GeocodingUtil", "LatLngForAddress: $address")

        // show error of Geocoder is not present
        if (!Geocoder.isPresent()) {
            Toast.makeText(
                MyApplication.instance,
                R.string.toast_geocoder_missing,
                Toast.LENGTH_LONG
            ).show()
            return null
        }

        // TODO more results?
        val locations = geocoder.getFromLocationName(address, 1)
        // no LatLng found for given address
        if (locations == null || locations.size == 0)
            return null

        return LatLng(locations[0].latitude, locations[0].longitude)
    }
}