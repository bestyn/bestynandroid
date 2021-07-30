package com.gbksoft.neighbourhood.data.repositories

import android.net.Uri
import com.gbksoft.neighbourhood.data.models.request.audio.CreateAudioReq
import com.gbksoft.neighbourhood.data.models.response.audio.AudioModel
import com.gbksoft.neighbourhood.data.network.ApiFactory
import com.gbksoft.neighbourhood.data.repositories.utils.PagingHelper
import com.gbksoft.neighbourhood.domain.paging.Paging
import com.gbksoft.neighbourhood.mappers.audio.AudioMapper
import com.gbksoft.neighbourhood.model.audio.Audio
import com.gbksoft.neighbourhood.utils.Constants
import io.reactivex.Completable
import io.reactivex.Observable

class AudioRepository : BaseRepository() {

    private val expand = "profile,isFavorite"
    private val pagingHelper = PagingHelper<AudioModel, Audio> {
        AudioMapper.toAudio(it)
    }

    fun createAudio(audio: Uri, description: String, startTime: Int, duration: Int): Completable {
        val createAudioReq = CreateAudioReq().apply {
            setAudioFile(audio)
            setDescription(description)
            setStartTime(startTime)
            setDuration(duration)
        }
        return ApiFactory.apiAudio.createAudio(createAudioReq)
    }

    fun loadDiscoverAudio(searchByDescription: String?, paging: Paging<List<Audio>>?): Observable<Paging<List<Audio>>> {
        paging?.let {
            return loadDiscoverAudio(searchByDescription, it.currentPage + 1, paging.itemsPerPage)
        } ?: run {
            return loadDiscoverAudio(searchByDescription, 1, Constants.PER_PAGE)
        }
    }

    private fun loadDiscoverAudio(searchByDescription: String?, page: Int, perPage: Int): Observable<Paging<List<Audio>>> {
        return ApiFactory
                .apiAudio.getAudio(
                        searchByDescription = searchByDescription,
                        sort = "-description",
                        expand = expand,
                        page = page,
                        perPage = perPage)
                .map { pagingHelper.getPagingResult(it) }
    }

    fun loadMyAudio(profileId: Long, searchByDescription: String?, paging: Paging<List<Audio>>?): Observable<Paging<List<Audio>>> {
        paging?.let {
            return loadMyAudio(profileId, searchByDescription, it.currentPage + 1, paging.itemsPerPage)
        } ?: run {
            return loadMyAudio(profileId, searchByDescription, 1, Constants.PER_PAGE)
        }
    }

    private fun loadMyAudio(profileId: Long, searchByDescription: String?, page: Int, perPage: Int): Observable<Paging<List<Audio>>> {
        return ApiFactory
                .apiAudio.getAudio(
                        searchByProfileId = profileId,
                        searchByDescription = searchByDescription,
                        sort = "description,-popularity,-createdAt",
                        expand = expand,
                        page = page,
                        perPage = perPage)
                .map { pagingHelper.getPagingResult(it) }
    }

    fun loadFavoriteAudio(searchByDescription: String?, paging: Paging<List<Audio>>?): Observable<Paging<List<Audio>>> {
        paging?.let {
            return loadFavoriteAudio(searchByDescription, it.currentPage + 1, paging.itemsPerPage)
        } ?: run {
            return loadFavoriteAudio(searchByDescription, 1, Constants.PER_PAGE)
        }
    }

    private fun loadFavoriteAudio(searchByDescription: String?, page: Int, perPage: Int): Observable<Paging<List<Audio>>> {
        return ApiFactory
                .apiAudio.getAudio(
                        searchByDescription = searchByDescription,
                        isFavorite = true,
                        sort = "description,-popularity,-createdAt",
                        expand = expand,
                        page = page,
                        perPage = perPage)
                .map { pagingHelper.getPagingResult(it) }
    }

    fun addAudioToFavorites(id: Long): Completable {
        return ApiFactory
                .apiAudio.addAudioToFavorites(id)
    }

    fun removeAudioFromFavorites(id: Long): Completable {
        return ApiFactory
                .apiAudio.removeAudioFromFavorites(id)
    }
}