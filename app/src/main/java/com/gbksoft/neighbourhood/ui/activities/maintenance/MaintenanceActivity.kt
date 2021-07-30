package com.gbksoft.neighbourhood.ui.activities.maintenance

import android.os.Bundle
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.ui.activities.base.BaseActivity

class MaintenanceActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maintenance)
    }

    override fun onNetworkStateChanged(isOnline: Boolean) {
    }

    override fun onMaintenanceStateChanged(isMaintenance: Boolean) {
        if (!isMaintenance) {
            routeToMain(null, null)
        }
    }
}