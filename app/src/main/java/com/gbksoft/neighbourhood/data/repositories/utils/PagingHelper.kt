package com.gbksoft.neighbourhood.data.repositories.utils

import com.gbksoft.neighbourhood.data.models.response.base.BasePagination
import com.gbksoft.neighbourhood.data.models.response.base.BaseResponse
import com.gbksoft.neighbourhood.domain.paging.Paging

class PagingHelper<IN, OUT>(
    private val itemMapper: (IN) -> OUT
) {
    fun getPagingResult(resp: BaseResponse<List<IN>>): Paging<List<OUT>> {
        val list = mutableListOf<OUT>()
        val serverPagination: BasePagination = resp.meta?.pagination
            ?: return Paging(0, 0, 0, 0, list)

        val totalCount: Int = serverPagination.totalCount
        val pageCount: Int = serverPagination.pageCount
        val currentPage: Int = serverPagination.currentPage
        val perPage: Int = serverPagination.perPage
        for (model in resp.requireResult()) {
            list.add(itemMapper.invoke(model))
        }
        return Paging(totalCount, pageCount, currentPage, perPage, list)
    }
}