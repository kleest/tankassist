package st.klee.tankassist.data

import android.content.Context
import st.klee.tankassist.R

data class Station(
    val id: String,
    val name: String,
    val street: String,
    val houseNumber: String,
    val postCode: Int,
    val place: String,
    val latLong: LatLng,
    val prices: Map<FuelType, Price>,
    val dist: Double,
    val isOpen: Boolean
) {
    fun formatAddress(context: Context): String {
        return context.getString(R.string.address_full, this.street, this.houseNumber, this.postCode, this.place)
    }
}