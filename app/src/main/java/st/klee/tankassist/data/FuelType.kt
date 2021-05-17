package st.klee.tankassist.data

enum class FuelType(val value: String) {
    Diesel("diesel"), E5("e5"), E10("e10");

    companion object {
        fun from(findValue: String): FuelType = FuelType.values().first { it.value == findValue }
    }
}