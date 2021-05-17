package st.klee.tankassist.data

import android.location.Location

data class LatLng(val lat: Double, val lng: Double) {
    companion object {
        fun FromLocation(location: Location) = LatLng(location.latitude, location.longitude)
    }

    override fun toString(): String {
        return "$lat $lng"
    }

    fun distanceTo(latLng: LatLng): Float {
        val distance = FloatArray(1)
        Location.distanceBetween(
            this.lat,
            this.lng,
            latLng.lat,
            latLng.lng,
            distance
        )
        return distance[0]
    }
}