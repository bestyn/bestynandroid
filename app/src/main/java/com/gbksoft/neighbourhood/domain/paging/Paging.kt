package com.gbksoft.neighbourhood.domain.paging

data class Paging<R>(
    val totalCount: Int,
    val pageCount: Int,
    val currentPage: Int,
    val itemsPerPage: Int,
    val content: R
)