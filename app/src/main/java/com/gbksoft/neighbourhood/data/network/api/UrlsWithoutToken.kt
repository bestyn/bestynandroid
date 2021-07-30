package com.gbksoft.neighbourhood.data.network.api

object UrlsWithoutToken {
    const val CONFIG = "v1/config"
    const val PAGE_ABOUT = "v1/pages/about"
    const val PAGE_PRIVACY = "v1/pages/privacy-policy"
    const val PAGE_TERMS = "v1/pages/terms-and-conditions"
    const val LOGIN = "v1/user/login"
    const val REFRESH_TOKEN = "v1/user/refresh"
    const val SIGN_UP = "v1/user/register"
    const val RECOVERY_PASSWORD = "v1/user/recovery-password"
    const val NEW_PASSWORD = "v1/user/new-password"
    const val VALIDATE_RESET_PASSWORD_TOKEN = "v1/user/validate-new-password"

    fun isWithoutAccessToken(url: String): Boolean {
        return url.contains(CONFIG) ||
            url.contains(PAGE_ABOUT) ||
            url.contains(PAGE_PRIVACY) ||
            url.contains(PAGE_TERMS) ||
            url.contains(LOGIN) ||
            url.contains(REFRESH_TOKEN) ||
            url.contains(SIGN_UP) ||
            url.contains(RECOVERY_PASSWORD) ||
            url.contains(NEW_PASSWORD)
    }
}