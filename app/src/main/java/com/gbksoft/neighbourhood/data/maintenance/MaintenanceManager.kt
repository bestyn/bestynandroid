package com.gbksoft.neighbourhood.data.maintenance

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import timber.log.Timber

class MaintenanceManager {
    private val MAINTENANCE_VARIABLE = "maintenance"
    private val maintenanceModeLiveData = MutableLiveData<Boolean>()

    init {
        subscribeToMaintenanceVariable()
    }

    private fun subscribeToMaintenanceVariable() {
        val maintenance = Firebase.database.getReference(MAINTENANCE_VARIABLE)
        maintenance.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Timber.tag("Maintenance").d(error.message)
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                Timber.tag("Maintenance").d(snapshot.value.toString())
                maintenanceModeLiveData.value = snapshot.value as? Boolean
            }
        })
    }

    fun setMaintenanceListener(lifecycleOwner: LifecycleOwner, maintenanceListener: IMaintenanceListener) {
        maintenanceModeLiveData.observe(lifecycleOwner, Observer { maintenanceListener.onMaintenanceStateChanged(it) })
    }
}