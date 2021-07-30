package com.gbksoft.neighbourhood.utils

object PhoneFormatter {
    private const val MAX_LENGTH = Constants.BUSINESS_PHONE_LENGTH + 3
    private val sb = StringBuilder()

    fun format(chars: List<Char>): String {
        sb.clear()
        for (i in chars.indices) {
            when (i) {
                0 -> sb.append("(")
                3 -> sb.append(")")
                6 -> sb.append("-")
            }
            sb.append(chars[i])
        }
        if (sb.length > MAX_LENGTH) sb.setLength(MAX_LENGTH)
        return sb.toString()
    }

    fun format(number: String): String {
        sb.clear()
        number.getOnlyDigits()?.let {
            for (i in it.indices) {
                when (i) {
                    0 -> sb.append("(")
                    3 -> sb.append(")")
                    6 -> sb.append("-")
                }
                sb.append(it[i])
            }
        }

        if (sb.length > MAX_LENGTH) sb.setLength(MAX_LENGTH)
        return sb.toString()
    }


    fun String?.getOnlyDigits(): String? {
        if (this == null) return null
        return this.replace(Regex("[^0-9 ]"), "")
    }
}