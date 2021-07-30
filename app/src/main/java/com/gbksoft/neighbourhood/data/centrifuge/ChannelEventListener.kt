package com.gbksoft.neighbourhood.data.centrifuge

import com.gbksoft.neighbourhood.BuildConfig
import io.github.centrifugal.centrifuge.*
import kotlin.text.Charsets.UTF_8

class ChannelEventListener(
    private val onPublishCallback: ((String) -> Unit)? = null
) : SubscriptionEventListener() {
    private val centrifugeLogger = CentrifugeLogger(BuildConfig.DEBUG)

    override fun onPublish(sub: Subscription?, event: PublishEvent?) {
        if (event != null) {
            val data = String(event.data, UTF_8)
            centrifugeLogger.log("onPublish", sub, data)
            onPublishCallback?.invoke(data)
        } else {
            centrifugeLogger.log("onPublish", sub, event)
        }
    }

    override fun onJoin(sub: Subscription?, event: JoinEvent?) {
        centrifugeLogger.log("onJoin", sub, event)
    }

    override fun onLeave(sub: Subscription?, event: LeaveEvent?) {
        centrifugeLogger.log("onLeave", sub, event)
    }

    override fun onSubscribeSuccess(sub: Subscription?, event: SubscribeSuccessEvent?) {
        centrifugeLogger.log("onSubscribeSuccess", sub, event)
    }

    override fun onSubscribeError(sub: Subscription?, event: SubscribeErrorEvent?) {
        centrifugeLogger.log("onSubscribeError", sub, event)
    }

    override fun onUnsubscribe(sub: Subscription?, event: UnsubscribeEvent?) {
        centrifugeLogger.log("onUnsubscribe", sub, event)
    }

}