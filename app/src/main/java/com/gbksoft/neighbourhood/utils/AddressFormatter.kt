package com.gbksoft.neighbourhood.utils

import android.location.Address
import com.google.android.libraries.places.api.model.AddressComponents
import com.google.gson.Gson
import timber.log.Timber

/*
Street number, Street Name, City Name,  State 2-letter code, Postal Code
Example: 213 Derrick Street, Boston, NY, 12345
 */
object AddressFormatter {
    private const val STREET_NUMBER_KEY = 0
    private const val STREET_NAME_KEY = 1
    private const val CITY_KEY = 2
    private const val COUNTRY_KEY = 3
    private const val POSTAL_CODE_KEY = 4
    private const val STATE_KEY = 5

    @JvmStatic
    fun format(addressComponents: AddressComponents?): String {
        if (addressComponents == null) return ""
        Timber.tag("AddressTag").d(Gson().toJson(addressComponents))
        val addressBuilder = StringBuilder()
        val map = addressComponents.toMap()
        val list = toComponentsList(map)
        for (i in list.indices) {
            addressBuilder.append(list[i])
            if (i < list.size - 1) addressBuilder.append(", ")
        }
        return addressBuilder.toString()
    }

    private fun toComponentsList(map: Map<Int, String>): List<String> {
        val list = mutableListOf<String>()
        if (map.contains(STREET_NUMBER_KEY) && map.contains(STREET_NAME_KEY)) {
            list.add("${map[STREET_NUMBER_KEY]} ${map[STREET_NAME_KEY]}")
        } else if (map.contains(STREET_NAME_KEY)) {
            list.add("${map[STREET_NAME_KEY]}")
        }

        if (map.contains(CITY_KEY)) {
            list.add("${map[CITY_KEY]}")
        }
        if (map.contains(STATE_KEY)) {
            list.add("${map[STATE_KEY]}")
        }
        if (map.contains(COUNTRY_KEY)) {
            list.add("${map[COUNTRY_KEY]}")
        }
        if (map.contains(POSTAL_CODE_KEY)) {
            list.add("${map[POSTAL_CODE_KEY]}")
        }
        return list
    }


    private fun AddressComponents.toMap(): MutableMap<Int, String> {
        val map = mutableMapOf<Int, String>()
        for (component in asList()) {
            types@ for (type in component.types) {
                when (type) {
                    "street_number" -> {
                        map[STREET_NUMBER_KEY] = component.name
                        break@types
                    }
                    "route" -> {
                        map[STREET_NAME_KEY] = component.name
                        break@types
                    }
                    "locality" -> {
                        map[CITY_KEY] = component.name
                        break@types
                    }
                    "country" -> {
                        map[COUNTRY_KEY] = component.name
                        break@types
                    }
                    "postal_code" -> {
                        map[POSTAL_CODE_KEY] = component.name
                        break@types
                    }
                    "administrative_area_level_1" -> {
                        map[STATE_KEY] = component.shortName ?: component.name
                        break@types
                    }
                }
            }
        }
        return map
    }

    private fun Address.toMap(): MutableMap<Int, String> {
        val map = mutableMapOf<Int, String>()
        featureName?.let { map[STREET_NUMBER_KEY] = it }
        thoroughfare?.let { map[STREET_NAME_KEY] = it }
        locality?.let { map[CITY_KEY] = it }
        countryName?.let { map[COUNTRY_KEY] = it }
        postalCode?.let { map[POSTAL_CODE_KEY] = it }
        adminArea?.let { map[STATE_KEY] = it }
        return map
    }
}