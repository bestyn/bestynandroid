package com.gbksoft.neighbourhood.ui.fragments.base.posts_feed

import androidx.lifecycle.MutableLiveData
import com.gbksoft.neighbourhood.model.post.FeedPost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PostsManager {
    val liveData = MutableLiveData<List<FeedPost>>()

    private val postList = mutableListOf<FeedPost>()
    private val postMap = linkedMapOf<Long, FeedPost>()
    fun count(): Int = postList.size

    suspend fun onFirstPostsLoaded(data: List<FeedPost>) {
        withContext(Dispatchers.Default) {
            postMap.clear()
            postMap.addAll(data)
            return@withContext postMap
        }.let {
            refreshList()
        }
    }

    suspend fun onNextPostsLoaded(data: List<FeedPost>) {
        withContext(Dispatchers.Default) {
            postMap.addAll(data)
            return@withContext postMap
        }.let {
            refreshList()
        }

    }

    private fun refreshList() {
        postList.clear()
        postList.addAll(postMap.values)
        refreshLiveData()
    }

    private fun refreshLiveData() {
        liveData.value = postList
    }

    fun refresh() {
        refreshLiveData()
    }

    fun remove(feedPost: FeedPost) {
        postList.remove(feedPost)
        postMap.remove(feedPost.post.id)
        refreshLiveData()
    }

    fun remove(position: Int) {
        postList.removeAt(position).let {
            postMap.remove(it.post.id)
        }
        refreshLiveData()
    }

    fun removeById(id: Long) {
        postMap[id]?.let { feedPost ->
            remove(feedPost)
        }
    }

    fun update(feedPost: FeedPost) {
        postMap[feedPost.post.id]?.let { currentPost ->
            val position = postList.indexOf(currentPost)
            postList[position] = feedPost
            postMap[feedPost.post.id] = feedPost
            refreshLiveData()
        }
    }

    fun findById(id: Long): FeedPost? {
        return postMap[id]
    }

    private fun LinkedHashMap<Long, FeedPost>.addAll(list: List<FeedPost>) {
        for (feedPost in list) {
            put(feedPost.post.id, feedPost)
        }
    }
}