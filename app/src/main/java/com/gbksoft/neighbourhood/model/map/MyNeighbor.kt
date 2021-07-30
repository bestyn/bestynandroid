package com.gbksoft.neighbourhood.model.map


data class MyNeighbor(
    val id: Long,
    val title: String,
    val avatarUrl: String?,
    val location: Coordinates,
    val isBusiness: Boolean
)