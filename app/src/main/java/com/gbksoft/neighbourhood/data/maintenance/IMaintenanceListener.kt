package com.gbksoft.neighbourhood.data.maintenance

interface IMaintenanceListener {
    fun onMaintenanceStateChanged(isMaintenance: Boolean)
}