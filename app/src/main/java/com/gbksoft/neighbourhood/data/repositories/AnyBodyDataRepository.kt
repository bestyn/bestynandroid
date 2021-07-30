package com.gbksoft.neighbourhood.data.repositories

import com.gbksoft.neighbourhood.data.models.response.ConfigModel
import com.gbksoft.neighbourhood.data.network.api.ApiAnyBody
import io.reactivex.Observable

class AnyBodyDataRepository(
    private val apiAnyBody: ApiAnyBody
) : BaseRepository() {
    fun getConfig(): Observable<ConfigModel> {
        return apiAnyBody
            .getConfig()
            .map { it.result }
    }
}