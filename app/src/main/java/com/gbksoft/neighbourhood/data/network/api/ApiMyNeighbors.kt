package com.gbksoft.neighbourhood.data.network.api

import com.gbksoft.neighbourhood.data.models.response.base.BaseResponse
import com.gbksoft.neighbourhood.data.models.response.neighbors.NeighborModel
import io.reactivex.Observable
import retrofit2.http.GET

interface ApiMyNeighbors {
    @GET("v1/my-neighbors")
    fun getMyNeighbors(): Observable<BaseResponse<List<NeighborModel>>>
}