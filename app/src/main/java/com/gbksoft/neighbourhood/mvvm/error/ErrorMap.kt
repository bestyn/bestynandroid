package com.gbksoft.neighbourhood.mvvm.error

import com.gbksoft.neighbourhood.data.utils.ResponseStatusCodes
import io.reactivex.functions.Consumer
import java.util.*

class ErrorMap(private val errorListener: ErrorListener) {
    val errors = HashMap<Int, Consumer<String>>().apply {
        put(ResponseStatusCodes.CODE_CONNECTION_ERROR, Consumer { errorListener.onConnectionError() })
        put(ResponseStatusCodes.CODE_VERSION_INCOMPATIBILITY, Consumer { errorListener.onVersionIncompatibilityError() })
        put(ResponseStatusCodes.CODE_401_UNAUTHORIZED, Consumer { errorListener.on401(it) })
        put(ResponseStatusCodes.CODE_403_FORBIDDEN, Consumer { errorListener.on403(it) })
        put(ResponseStatusCodes.CODE_404_NOT_FOUND, Consumer { errorListener.on404(it) })
        put(ResponseStatusCodes.CODE_410_GONE, Consumer { errorListener.on410(it) })
        put(ResponseStatusCodes.CODE_422_VALIDATION_FAILED, Consumer { errorListener.on422(it) })
        put(ResponseStatusCodes.CODE_500_SERVER_ERROR, Consumer { errorListener.on500(it) })
        put(ResponseStatusCodes.CODE_502_BAD_GATEWAY, Consumer { errorListener.on502(it) })
        put(ResponseStatusCodes.CODE_503_SERVICE_UNAVAILABLE, Consumer { errorListener.on503(it) })
        put(ResponseStatusCodes.CODE_OTHER_ERROR, Consumer { errorListener.onOtherError(it) })
    }
}