package com.gbksoft.neighbourhood.mvvm.error

interface ErrorListener {
    fun onConnectionError()
    fun onVersionIncompatibilityError()
    fun on401(error: String)
    fun on403(error: String)
    fun on404(error: String)
    fun on410(error: String)
    fun on422(error: String)
    fun on500(error: String)
    fun on502(error: String)
    fun on503(error: String)
    fun onOtherError(error: String)
}