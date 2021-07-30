package com.gbksoft.neighbourhood.data.models.request

object RequestUtils {

    @JvmStatic
    fun formatHashtagIds(list: List<Long>): String {
        if (list.isEmpty()) return ""
        val sb = StringBuilder()
        for (i in list.indices) {
            val item = list[i]
            sb.append(item.toString())
            if (i < list.size - 1) sb.append(",")
        }
        return sb.toString()
    }
}