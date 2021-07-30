package com.gbksoft.neighbourhood.mvvm.error

import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.data.models.response.base.BaseResponse
import com.gbksoft.neighbourhood.data.models.response.base.ErrorResponse
import com.gbksoft.neighbourhood.mvvm.ErrorJson
import com.gbksoft.neighbourhood.utils.ToastUtils
import com.gbksoft.neighbourhood.utils.validation.ErrorsMessageUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SimpleErrorHandler(
    private val errorsMessageUtils: ErrorsMessageUtils
) : ErrorListener {

    //If callbacks return true than toast will be shown
    var onConnectionErrorCallback: (() -> Unit)? = null
    var onVersionIncompatibilityCallback: (() -> Unit)? = null
    var on401Callback: ((errorMessage: String?) -> Unit)? = null
    var on403Callback: ((errorResponse: BaseResponse<*>) -> Boolean)? = null
    var on404Callback: ((errorResponse: BaseResponse<*>) -> Boolean)? = null
    var on410Callback: (() -> Boolean)? = null
    var on422Callback: ((errors: List<ErrorResponse>) -> Boolean)? = null
    var on500Callback: (() -> Boolean)? = null
    var on502Callback: (() -> Boolean)? = null
    var on503Callback: (() -> Boolean)? = null
    var onOtherErrorCallback: (() -> Boolean)? = null

    private val gson = Gson()

    override fun onConnectionError() {
        onConnectionErrorCallback?.invoke()
    }

    override fun onVersionIncompatibilityError() {
        onVersionIncompatibilityCallback?.invoke()
    }

    override fun on401(error: String) {
        val errorMessage = try {
            val errorJson: ErrorJson = gson.fromJson(error, ErrorJson::class.java)
            errorJson.message
        } catch (e: Exception) {
            e.message
        }

        on401Callback?.invoke(errorMessage)
    }

    override fun on403(error: String) {
        val collectionType = object : TypeToken<BaseResponse<*>?>() {}.type
        val errorResponse: BaseResponse<*> = gson.fromJson(error, collectionType) ?: return

        val showToast = on403Callback?.invoke(errorResponse) ?: true
        if (showToast) {
            showToastMessage(errorResponse.message)
        }
    }

    override fun on404(error: String) {
        val collectionType = object : TypeToken<BaseResponse<*>?>() {}.type
        val errorResponse: BaseResponse<*> = gson.fromJson(error, collectionType) ?: return

        val showToast = on404Callback?.invoke(errorResponse) ?: true
        if (showToast) {
            showToastMessage(errorResponse.message)
        }
    }

    override fun on410(error: String) {
        val showToast = on410Callback?.invoke() ?: true
        if (showToast) {
            showSomethingWentWrong()
        }
    }

    override fun on422(error: String) {
        val collectionType = object : TypeToken<BaseResponse<List<ErrorResponse>?>?>() {}.type
        val errorResponse: BaseResponse<List<ErrorResponse>> = gson.fromJson(error, collectionType)
        val errors = errorResponse.result
        if (errors == null) {
            showToastMessage(errorResponse.message)
            return
        }

        val showToast = on422Callback?.invoke(errors) ?: true
        if (showToast) {
            show422Toast(errors)
        }
    }

    private fun show422Toast(errors: List<ErrorResponse>) {
        val sb = StringBuilder()
        for (errorResponse in errors) {
            if (sb.toString().isNotEmpty()) {
                sb.append("\n")
            }
            sb.append(errorsMessageUtils.getErrorMessageByErrorResponse(errorResponse))
        }

        if (sb.toString().isNotEmpty()) {
            showToastMessage(sb.toString())
        }
    }

    override fun on500(error: String) {
        val showToast = on500Callback?.invoke() ?: true
        if (showToast) {
            showSomethingWentWrong()
        }
    }

    override fun on502(error: String) {
        val showToast = on502Callback?.invoke() ?: true
        if (showToast) {
            showSomethingWentWrong()
        }
    }

    override fun on503(error: String) {
        val showToast = on503Callback?.invoke() ?: true
        if (showToast) {
            showSomethingWentWrong()
        }
    }

    override fun onOtherError(error: String) {
        val showToast = onOtherErrorCallback?.invoke() ?: true
        if (showToast) {
            showSomethingWentWrong()
        }
    }

    private fun showToastMessage(message: String?) {
        ToastUtils.showToastMessage(message)
    }

    private fun showSomethingWentWrong() {
        ToastUtils.showToastMessage(R.string.error_something_went_wrong)
    }
}