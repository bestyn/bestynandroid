package com.gbksoft.neighbourhood.utils.validation

import com.gbksoft.neighbourhood.data.models.response.base.ErrorParams
import com.gbksoft.neighbourhood.data.models.response.base.ErrorResponse

class ErrorsMessageUtils(private var errorsMap: Map<String, String>) {
    private val stringResultInCaseOfError = "Generic error"

    fun setErrorsMap(errorsMap: Map<String, String>) {
        this.errorsMap = errorsMap
    }

    fun getErrorMessageByCode(code: Int, vararg args: String): String? {
        var errorResourceString = getStringByErrorCode(code)

        return if (errorResourceString?.isNotEmpty() == true) {
            if (isStringContainsPatternElements(errorResourceString)) {
                if (args.isNotEmpty()) {
                    errorResourceString = replacePatternWithArgsValues(errorResourceString, *args)
                }
                errorResourceString
            } else {
                errorResourceString
            }
        } else {
            stringResultInCaseOfError
        }
    }

    fun getErrorMessageWithCustomString(errorResourceString: String, vararg args: String): String? {
        return if (errorResourceString.isNotEmpty()) {
            if (isStringContainsPatternElements(errorResourceString)) {
                if (args.isNotEmpty()) replacePatternWithArgsValues(errorResourceString, *args)
                else errorResourceString
            } else {
                errorResourceString
            }
        } else {
            stringResultInCaseOfError
        }
    }

    fun getErrorMessageByErrorResponse(errorResponse: ErrorResponse): String? {
        return getErrorMessageByCodeAndParamsList(errorResponse.code, errorResponse.params)
    }

    fun getErrorMessageByCodeAndParamsList(code: Int, paramsList: List<ErrorParams>?): String? {
        var errorResourceString = getStringByErrorCode(code)

        return if (errorResourceString?.isNotEmpty() == true) {
            if (isStringContainsPatternElements(errorResourceString)) {
                for (errorParam in paramsList!!) {
                    val paramKey = errorParam.name
                    val paramValue = errorParam.value
                    val paramPattern = transformToPatternString(paramKey)
                    if (errorResourceString!!.contains(paramPattern)) {
                        errorResourceString = errorResourceString.replace(paramPattern, paramValue)
                    }
                }
                errorResourceString
            } else {
                errorResourceString
            }
        } else {
            stringResultInCaseOfError
        }
    }

    private fun replacePatternWithArgsValues(sourceString: String, vararg args: String): String? {
        var sourceString = sourceString
        for (arg in args) {
            val indexPatternStart = sourceString.indexOf("{")
            val indexPatterEnd = sourceString.indexOf("}")
            val patternToReplace = sourceString.subSequence(indexPatternStart, indexPatterEnd + 1).toString()
            sourceString = sourceString.replace(patternToReplace, arg)
        }
        return sourceString
    }

    private fun isStringContainsPatternElements(stringToCheck: String): Boolean {
        return stringToCheck.contains("{") && stringToCheck.contains("}")
    }

    private fun transformToPatternString(paramKey: String): String {
        return "{$paramKey}"
    }

    private fun getStringByErrorCode(code: Int): String? {
        return errorsMap[code.toString()]
    }

}