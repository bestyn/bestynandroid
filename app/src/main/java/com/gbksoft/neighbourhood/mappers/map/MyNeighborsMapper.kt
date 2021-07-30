package com.gbksoft.neighbourhood.mappers.map

import com.gbksoft.neighbourhood.data.models.response.neighbors.NeighborModel
import com.gbksoft.neighbourhood.mappers.profile.ProfileTypeMapper
import com.gbksoft.neighbourhood.model.map.Coordinates
import com.gbksoft.neighbourhood.model.map.MyNeighbor

object MyNeighborsMapper {

    fun map(models: List<NeighborModel>): List<MyNeighbor> {
        val myNeighbors = mutableListOf<MyNeighbor>()
        for (model in models) {
            myNeighbors.add(map(model))
        }
        return myNeighbors
    }

    fun map(model: NeighborModel): MyNeighbor {
        return MyNeighbor(
            model.id,
            model.fullName,
            model.avatar,
            Coordinates(model.latitude, model.longitude),
            ProfileTypeMapper.isBusiness(model.type)
        )
    }
}