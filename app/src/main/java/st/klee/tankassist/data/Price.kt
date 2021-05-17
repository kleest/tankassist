package st.klee.tankassist.data

/**
 * If cost is null, there is no price associated with this fuel type
 */
data class Price(val type: FuelType, val cost: Double?) {

}
