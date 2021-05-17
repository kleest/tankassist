package st.klee.tankassist.api

import org.json.JSONObject
import st.klee.tankassist.data.FuelType
import st.klee.tankassist.data.LatLng
import st.klee.tankassist.data.Price
import st.klee.tankassist.data.Station

class StationListResponse : ApiResponse {
    override fun parseData(data: JSONObject): List<Station> {
        val stationsJson = data.getJSONArray("stations")
        val stations : MutableList<Station> = mutableListOf()

        for (i in 0 until stationsJson.length()) {
            val stationJson = stationsJson.getJSONObject(i)
            if (!(stationJson["isOpen"] as Boolean))
                continue
            stations.add(Station(
                stationJson["id"] as String,
                stationJson["name"] as String,
                stationJson["street"] as String,
                stationJson["houseNumber"] as String,
                stationJson["postCode"] as Int,
                stationJson["place"] as String,
                LatLng(jsonToDouble(stationJson["lat"]), jsonToDouble(stationJson["lng"])),
                mapOf(
                    Pair(FuelType.Diesel, Price(FuelType.Diesel, stationJson["diesel"] as? Double)),
                    Pair(FuelType.E5, Price(FuelType.E5, stationJson["e5"] as? Double)),
                    Pair(FuelType.E10, Price(FuelType.E10, stationJson["e10"] as? Double))
                ),
                jsonToDouble(stationJson["dist"]),
                stationJson.getBoolean("isOpen")
            ))
        }

        return stations
    }

    /**
     * JSON values can be Int or Double even if meant to be a double. This function tries to convert
     * an object to Double. If this fails, converts the object to Int and returns a Double.
     */
    private fun jsonToDouble(obj: Any) : Double {
        return (obj as? Double?: obj as Int).toDouble()
    }
}