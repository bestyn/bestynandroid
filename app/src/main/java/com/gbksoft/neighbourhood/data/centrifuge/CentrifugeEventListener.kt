package com.gbksoft.neighbourhood.data.centrifuge

import com.gbksoft.neighbourhood.BuildConfig
import io.github.centrifugal.centrifuge.*

class CentrifugeEventListener(
    private val onConnectCallback: ((Client) -> Unit)? = null,
    private val onPrivateSubCallback: ((Client, PrivateSubEvent, TokenCallback) -> Unit)? = null
) : EventListener() {
    private val centrifugeLogger = CentrifugeLogger(BuildConfig.DEBUG)

    override fun onConnect(client: Client, event: ConnectEvent) {
        centrifugeLogger.log("onConnect", event)
        onConnectCallback?.invoke(client)
    }

    override fun onDisconnect(client: Client, event: DisconnectEvent) {
        centrifugeLogger.log("onDisconnect", event)
    }

    override fun onMessage(client: Client, event: MessageEvent) {
        centrifugeLogger.log("onMessage", event)
    }

    override fun onError(client: Client, event: ErrorEvent) {
        centrifugeLogger.log("onError", event)
    }

    override fun onRefresh(client: Client, event: RefreshEvent, cb: TokenCallback) {
        centrifugeLogger.log("onRefresh", event, cb)
    }

    override fun onPrivateSub(client: Client, event: PrivateSubEvent, cb: TokenCallback) {
        centrifugeLogger.log("onPrivateSub", event, cb)
        onPrivateSubCallback?.invoke(client, event, cb)
    }

}