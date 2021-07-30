package com.gbksoft.neighbourhood.utils.permission

import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener

class DexterPermissionListener : PermissionListener {
    var onPermissionGranted: ((response: PermissionGrantedResponse) -> Unit)? = null
    var onPermissionDenied: ((response: PermissionDeniedResponse) -> Unit)? = null
    var onPermissionRationaleShouldBeShown: ((request: PermissionRequest, token: PermissionToken) -> Unit)? = null
    var onPermissionToken: ((token: PermissionToken) -> Unit)? = null

    override fun onPermissionGranted(response: PermissionGrantedResponse) {
        onPermissionGranted?.invoke(response)
    }

    override fun onPermissionDenied(response: PermissionDeniedResponse) {
        onPermissionDenied?.invoke(response)
    }

    override fun onPermissionRationaleShouldBeShown(request: PermissionRequest, token: PermissionToken) {
        onPermissionRationaleShouldBeShown?.invoke(request, token)
        onPermissionToken?.invoke(token)
    }
}