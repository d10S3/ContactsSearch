package com.d10s3.test.contactssearch

import android.app.Activity
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener

class Permissions (private val activity: Activity) {
    interface OnPermissionCheckCallback {
        fun onPermissionResult(result: Boolean)
    }

    fun check(permissions: String, onPermissionCheckCallback: OnPermissionCheckCallback) {
        Dexter.withActivity(activity).withPermission(permissions).withListener(object : PermissionListener {
            override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                onPermissionCheckCallback.onPermissionResult(true)
            }

            override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                onPermissionCheckCallback.onPermissionResult(false)
            }

            override fun onPermissionRationaleShouldBeShown(
                permission: PermissionRequest?,
                token: PermissionToken?
            ) {
                token?.continuePermissionRequest()
            }
        }).check()
    }
}