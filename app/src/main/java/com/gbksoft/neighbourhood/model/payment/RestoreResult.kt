package com.gbksoft.neighbourhood.model.payment

class RestoreResult(val state: State, data: String? = null) {
    enum class State {
        RESTORED, BOUGHT_ON_ANOTHER_PLATFORM, DELETED, NOT_FOUND
    }
}