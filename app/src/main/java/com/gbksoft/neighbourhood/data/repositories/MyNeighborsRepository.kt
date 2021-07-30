package com.gbksoft.neighbourhood.data.repositories

import com.gbksoft.neighbourhood.data.network.ApiFactory
import com.gbksoft.neighbourhood.mappers.map.MyNeighborsMapper
import com.gbksoft.neighbourhood.model.map.MyNeighbor
import io.reactivex.Observable

class MyNeighborsRepository : BaseRepository() {

    fun loadMyNeighbors(): Observable<List<MyNeighbor>> {
        return ApiFactory.apiMyNeighbors.getMyNeighbors()
            .map { MyNeighborsMapper.map(it.requireResult()) }
    }
}