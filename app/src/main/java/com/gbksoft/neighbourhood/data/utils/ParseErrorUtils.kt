package com.gbksoft.neighbourhood.data.utils

import com.gbksoft.neighbourhood.BuildConfig
import com.gbksoft.neighbourhood.data.connectivity.NoConnectivityException
import com.gbksoft.neighbourhood.data.utils.ResponseStatusCodes.CODE_CONNECTION_ERROR
import com.gbksoft.neighbourhood.data.utils.ResponseStatusCodes.CODE_OTHER_ERROR
import com.gbksoft.neighbourhood.data.utils.ResponseStatusCodes.CODE_VERSION_INCOMPATIBILITY
import com.gbksoft.neighbourhood.domain.exceptions.VersionIncompatibilityException
import com.google.gson.JsonSyntaxException
import io.reactivex.functions.Consumer
import retrofit2.HttpException
import java.util.*

object ParseErrorUtils {
    @JvmStatic
    @Throws(Exception::class)
    fun parseError(error: Throwable?, funcMap: HashMap<Int, Consumer<String>>) {
        if (BuildConfig.DEBUG) error?.printStackTrace()
        when {
            error is HttpException -> {
                parseHttpException(error, funcMap)
            }
            error is JsonSyntaxException -> {
                funcMap[CODE_OTHER_ERROR]?.accept("Response model error")
            }
            error is NoConnectivityException -> {
                funcMap[CODE_CONNECTION_ERROR]?.accept("")
            }
            error is VersionIncompatibilityException -> {
                funcMap[CODE_VERSION_INCOMPATIBILITY]?.accept("")
            }
            error != null -> {
                funcMap[CODE_OTHER_ERROR]?.accept(error.message)
            }
        }
    }

    private fun parseHttpException(error: HttpException, funcMap: HashMap<Int, Consumer<String>>) {
        var st = ""
        var code: Int? = null
        val resp = error.response()
        resp?.errorBody()?.let { errorBody ->
            st = errorBody.string()
            code = resp.raw().code
        }
        if (code != null) {
            for ((key, value) in funcMap) {
                if (key == code) {
                    value.accept(st)
                    break
                }
            }
        }
    }
}