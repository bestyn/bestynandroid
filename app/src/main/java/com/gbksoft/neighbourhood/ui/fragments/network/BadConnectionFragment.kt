package com.gbksoft.neighbourhood.ui.fragments.network

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.app.NApplication
import com.gbksoft.neighbourhood.databinding.FragmentBadConnectionBinding
import com.gbksoft.neighbourhood.ui.fragments.base.BaseFragment

class BadConnectionFragment : BaseFragment() {
    private lateinit var layout: FragmentBadConnectionBinding
    private val handler = Handler()
    private val checkConnectionRunnable = Runnable {
        checkConnection()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        hideNavigateBar()
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_bad_connection,
            container, false)
        return layout.root
    }

    override fun onStart() {
        super.onStart()
        handler.postDelayed(checkConnectionRunnable, 500)
    }

    private fun checkConnection() {
        (activity?.application as? NApplication)?.getConnectivityManager()?.let {
            if (it.isOnline()) findNavController().popBackStack()
        }
    }

    override fun onStop() {
        handler.removeCallbacks(checkConnectionRunnable)
        super.onStop()
    }
}