package com.gbksoft.neighbourhood.domain.exceptions

/**
 * Application and Server versions are incompatibility
 */
class VersionIncompatibilityException : Exception {
    companion object {
        private const val serialVersionUID = 6020916865744527698L
    }

    constructor() : super()


    constructor(message: String?) : super(message)


    constructor(message: String?, cause: Throwable?) : super(message, cause)


    constructor(cause: Throwable?) : super(cause)

}