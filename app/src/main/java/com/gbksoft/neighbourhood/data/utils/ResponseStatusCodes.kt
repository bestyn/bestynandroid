package com.gbksoft.neighbourhood.data.utils

object ResponseStatusCodes {
    //===== Response Code ==========================================================================
    const val CODE_400_INVALID_LOGIN_PASS = 400
    const val CODE_401_UNAUTHORIZED = 401
    const val CODE_402_PAYMENT_REQUIRED = 402
    const val CODE_403_FORBIDDEN = 403
    const val CODE_404_NOT_FOUND = 404
    const val CODE_410_GONE = 410
    const val CODE_422_VALIDATION_FAILED = 422
    const val CODE_429_TOO_MANY_REQUESTS = 429
    const val CODE_500_SERVER_ERROR = 500
    const val CODE_502_BAD_GATEWAY = 502
    const val CODE_503_SERVICE_UNAVAILABLE = 503

    //===== Other Code ==========================================================================
    const val CODE_OTHER_ERROR = -1
    const val CODE_CONNECTION_ERROR = -500
    const val CODE_VERSION_INCOMPATIBILITY = -403
}