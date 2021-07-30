package com.gbksoft.neighbourhood.ui.activities.player

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.gbksoft.neighbourhood.R

class VideoPlayerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_host_fragment)

        findNavController(R.id.navHostFragment).setGraph(R.navigation.player_graph, intent.extras)

    }
}