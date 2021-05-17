package st.klee.tankassist.api

import org.json.JSONObject

interface ApiResponse {
    fun parseData(data: JSONObject) : Any?
}

fun ApiResponse.parse(data: JSONObject) : Any? {
    val isOk = data.getBoolean("ok")
    if (!isOk)
        return null
    return parseData(data)
}