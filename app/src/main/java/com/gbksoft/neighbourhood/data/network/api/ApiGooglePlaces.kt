package com.gbksoft.neighbourhood.data.network.api

import com.gbksoft.neighbourhood.data.models.response.geocode.GeocodeResponse
import com.gbksoft.neighbourhood.model.map.Coordinates
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiGooglePlaces {
    @GET("https://maps.googleapis.com/maps/api/geocode/json")
    fun getAddress(
        @Query("latlng") coordinates: Coordinates,
        @Query("key") googleApiKey: String): Call<GeocodeResponse>
}