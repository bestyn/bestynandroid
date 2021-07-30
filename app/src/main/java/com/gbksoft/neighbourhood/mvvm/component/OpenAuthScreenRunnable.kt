package com.gbksoft.neighbourhood.mvvm.component

import android.app.Activity
import android.app.ActivityManager
import android.content.Context.ACTIVITY_SERVICE
import android.content.Intent
import android.os.Build
import com.gbksoft.neighbourhood.app.NApplication
import com.gbksoft.neighbourhood.ui.activities.auth.AuthActivity
import com.gbksoft.neighbourhood.utils.ToastUtils


class OpenAuthScreenRunnable : Runnable {
    var toastMessage: String? = null

    override fun run() {
        if (isActivityRunning(AuthActivity::class.java)) return

        val context = NApplication.context
        val intent = Intent(context, AuthActivity::class.java)
        toastMessage?.let {
            if (it.isNotEmpty()) ToastUtils.showToastMessage(NApplication.context, it)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
    }

    private fun isActivityRunning(activityClass: Class<out Activity>): Boolean {
        val activityManager = NApplication.context.getSystemService(ACTIVITY_SERVICE)
            as ActivityManager? ?: return false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (task in activityManager.appTasks) {
                val taskInfo = task.taskInfo
                if (taskInfo.numActivities == 1 && taskInfo.topActivity?.className == activityClass.name) {
                    return true
                }
            }
        } else {
            for (taskInfo in activityManager.getRunningTasks(10)) {
                if (taskInfo.numActivities == 1 && taskInfo.topActivity?.className == activityClass.name) {
                    return true
                }
            }
        }

        return false
    }
}