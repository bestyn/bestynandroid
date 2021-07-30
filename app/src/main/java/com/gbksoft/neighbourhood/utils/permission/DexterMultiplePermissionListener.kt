package com.gbksoft.neighbourhood.utils.permission

import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

class DexterMultiplePermissionListener : MultiplePermissionsListener {
    var onPermissionsChecked: ((report: MultiplePermissionsReport) -> Unit)? = null
    var onPermissionRationaleShouldBeShown: ((list: MutableList<PermissionRequest>, token: PermissionToken) -> Unit)? = null
    var onPermissionToken: ((token: PermissionToken) -> Unit)? = null

    override fun onPermissionsChecked(report: MultiplePermissionsReport) {
        onPermissionsChecked?.invoke(report)
    }

    override fun onPermissionRationaleShouldBeShown(list: MutableList<PermissionRequest>, token: PermissionToken) {
        onPermissionRationaleShouldBeShown?.invoke(list, token)
        onPermissionToken?.invoke(token)
    }
}