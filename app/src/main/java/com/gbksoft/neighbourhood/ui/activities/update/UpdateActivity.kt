package com.gbksoft.neighbourhood.ui.activities.update

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.ui.dialogs.YesNoDialog
import com.gbksoft.neighbourhood.utils.ToastUtils

class UpdateActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showPopup()
    }

    private fun showPopup() {
        val appName = getString(R.string.app_name)
        val title = getString(R.string.update_dialog_title, appName)
        val message = getString(R.string.update_dialog_message, appName)
        val dialog = YesNoDialog.Builder()
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.update_dialog_update_btn) { openGooglePlay();close() }
            .setNegativeButton(R.string.update_dialog_close_btn) { close() }
            .setCanceledOnTouchOutside(false)
            .build()
        dialog.isCancelable = false

        dialog.show(supportFragmentManager, "UpdateDialog")
    }

    private fun openGooglePlay() {
        val uri = Uri.parse("market://details?id=$packageName")
        val myAppLinkToMarket = Intent(Intent.ACTION_VIEW, uri)
        try {
            startActivity(myAppLinkToMarket)
        } catch (e: ActivityNotFoundException) {
            ToastUtils.showToastMessage(this, R.string.update_error_not_found_in_market)
        }
    }

    private fun close() {
        finishAffinity()
    }

    override fun onBackPressed() {}
}