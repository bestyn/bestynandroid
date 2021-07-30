package com.gbksoft.neighbourhood.ui.widgets.stories.timeline

class ThumbPositionConverter(
    private val timelineAreaProvider: TimelineAreaProvider
) {
    var videoLengthInMs: Int = 0

    fun getPosition(timeMillis: Int): Float {
        checkState()

        val leftX = timelineAreaProvider.getThumbLeftBarrier()
        val rightX = timelineAreaProvider.getThumbRightBarrier()
        val viewWidth = rightX - leftX
        val pxInMillis = videoLengthInMs.toFloat() / viewWidth
        return timeMillis.toFloat() / pxInMillis + leftX
    }

    fun getTime(xPos: Float): Int {
        checkState()

        val leftX = timelineAreaProvider.getThumbLeftBarrier()
        val rightX = timelineAreaProvider.getThumbRightBarrier()
        val viewWidth = rightX - leftX
        val millisInPx = viewWidth / videoLengthInMs.toFloat()
        return ((xPos - leftX) / millisInPx).toInt()
    }

    fun timeToWidth(timeMillis: Int): Float {
        checkState()

        val leftX = timelineAreaProvider.getThumbLeftBarrier()
        val rightX = timelineAreaProvider.getThumbRightBarrier()
        val viewWidth = rightX - leftX
        val millisInPx = viewWidth / videoLengthInMs.toFloat()
        return timeMillis * millisInPx
    }

    private fun checkState() {
        if (videoLengthInMs <= 0) {
           // throw IllegalStateException("Wrong video length: $videoLengthInMs")
        }
    }
}