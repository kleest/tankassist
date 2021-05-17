package st.klee.tankassist.api

import android.content.Context
import androidx.preference.PreferenceManager
import st.klee.tankassist.BuildConfig
import st.klee.tankassist.R
import st.klee.tankassist.data.LatLng
import java.util.*

object ApiEndpoint {

    fun list(latLng: LatLng, context: Context): String {
        // format double values as "xx.yy"
        val radius = PreferenceManager.getDefaultSharedPreferences(context).getInt(context.getString(R.string.pref_searchRadius), 10).toDouble()
        return "https://creativecommons.tankerkoenig.de/json/list.php?lat=%f&lng=%f&rad=%f&sort=dist&type=all&apikey=${BuildConfig.API_TANKERKOENIG_KEY}".format(Locale.US, latLng.lat, latLng.lng, radius)
    }
}