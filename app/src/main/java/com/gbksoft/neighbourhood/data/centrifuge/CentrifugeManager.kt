package com.gbksoft.neighbourhood.data.centrifuge

import com.gbksoft.neighbourhood.BuildConfig
import com.gbksoft.neighbourhood.data.models.request.centrifuge.ChannelBody
import com.gbksoft.neighbourhood.data.network.ApiFactory
import io.github.centrifugal.centrifuge.*
import io.reactivex.Observable
import retrofit2.HttpException
import retrofit2.Response
import timber.log.Timber


class CentrifugeManager private constructor() {
    companion object {
        private var instance: CentrifugeManager? = null
        fun getInstance(): CentrifugeManager {
            if (instance == null) {
                instance = CentrifugeManager()
            }
            return instance!!
        }

        private const val POST_CHANNEL_PREFIX = "\$postMessage_"
        private const val CHATS_CHANNEL_PREFIX = "usersMessage#"
        private const val CONVERSATION_CHANNEL_PREFIX = "\$profilesMessage_"
    }

    private val centrifugeLogger = CentrifugeLogger(BuildConfig.DEBUG)
    private var token: String? = null
    private val baseUrl = BuildConfig.WS_BASE_URL

    private var currentConversationsChannelClient: Client? = null

    fun connectToPostChannel(postId: Long): Observable<String> {
        val channelId = POST_CHANNEL_PREFIX + postId
        var centrifugeClient: Client? = null
        val observable = Observable.create<String> { emitter ->
            val signInToken = token ?: signIn()
            val client = initClient(signInToken)
            centrifugeClient = client
            client.connect()
            subscribeToChannel(client, channelId, ChannelEventListener { data ->
                emitter.onNext(data)
            })
        }
        return observable.doOnDispose {
            centrifugeClient?.disconnect()
        }
    }

    fun connectToConversationChannel(vararg profileIds: Long): Observable<String> {
        profileIds.sort()
        val channelId = CONVERSATION_CHANNEL_PREFIX + profileIds.joinToString(separator = ",")//"profilesMessage#6,7"
        val observable = Observable.create<String> { emitter ->
            val signInToken = token ?: signIn()
            val client = initClient(signInToken)
            currentConversationsChannelClient = client
            client.connect()
            subscribeToChannel(client, channelId, ChannelEventListener { data ->
                emitter.onNext(data)
            })
        }
        return observable.doOnDispose {
            currentConversationsChannelClient?.disconnect()
            currentConversationsChannelClient = null
        }
    }

    fun connectToChatsChannel(currentUserId: Long): Observable<String> {
        val channelId = CHATS_CHANNEL_PREFIX + currentUserId
        var centrifugeClient: Client? = null
        val observable = Observable.create<String> { emitter ->
            val signInToken = token ?: signIn()
            val client = initClient(signInToken)
            centrifugeClient = client
            client.connect()
            subscribeToChannel(client, channelId, ChannelEventListener { data ->
                emitter.onNext(data)
            })
        }
        return observable.doOnDispose {
            centrifugeClient?.disconnect()
        }
    }

    fun sendToConversationsChannel(profileIds: List<Long>, data: String) {
        val channelId = CONVERSATION_CHANNEL_PREFIX + profileIds.sorted().joinToString(separator = ",")
        if (currentConversationsChannelClient == null) {
            connectToConversationChannel(*profileIds.toLongArray())
            return
        }
        currentConversationsChannelClient?.publish(channelId, data.toByteArray(), object : ReplyCallback<PublishResult> {
            override fun onFailure(e: Throwable?) {
                Timber.tag("Centrifuge").d("onFailure: $e")
            }

            override fun onDone(error: ReplyError?, result: PublishResult?) {
                Timber.tag("Centrifuge").d("onDone: $result")
            }
        })
    }

    private fun signIn(): String {
        val call = ApiFactory.apiCentrifuge.sign()
        val resp = call.execute()
        if (resp.isNotSuccessful()) {
            throw HttpException(resp)
        }

        resp.body()?.result?.token?.let {
            token = it
            return it
        } ?: throw NullPointerException("${call.request().url} returned null token")
    }

    private fun initClient(singInToken: String): Client {
        val listener = CentrifugeEventListener(
            onPrivateSubCallback = { _, event, callback ->
                signToPrivateChannel(event.client, event.channel, callback)
            }
        )
        val options = Options()
        options.timeout = 20_000
        val url = "${baseUrl}/connection/websocket?format=protobuf"
        centrifugeLogger.log("Connecting to", url)
        val client = Client(
            url,
            options,
            listener
        )
        client.setToken(singInToken)
        return client
    }

    private fun signToPrivateChannel(clientId: String, channelId: String, tokenCallback: TokenCallback) {
        val req = ChannelBody(clientId, channelId)
        val call = ApiFactory.apiCentrifuge.channelAuth(req)
        val resp = call.execute()
        if (resp.isNotSuccessful()) {
            tokenCallback.Fail(HttpException(resp))
        }

        resp.body()?.result?.channelToken?.let { token ->
            tokenCallback.Done(token)
        } ?: run {
            tokenCallback.Fail(NullPointerException("${call.request().url} returned null token for $channelId"))
        }
    }

    private fun subscribeToChannel(client: Client, channelId: String, channelListener: SubscriptionEventListener) {
        try {
            client.newSubscription(channelId, channelListener)
        } catch (e: DuplicateSubscriptionException) {
            e.printStackTrace()
        }
    }

    private fun <T> Response<T>.isNotSuccessful(): Boolean {
        return !isSuccessful
    }

}