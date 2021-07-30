package com.gbksoft.neighbourhood

import com.gbksoft.neighbourhood.ui.widgets.avatar.GradientAngle
import org.junit.Assert
import org.junit.Test

/**
 * Tests for [GradientAngle]
 */
class GradientAngleUnitTest {

    @Test
    fun calcSuitableAngle_isCorrect() {
        Assert.assertEquals(0, GradientAngle(0).angle)
        Assert.assertEquals(0, GradientAngle(15).angle)
        Assert.assertEquals(45, GradientAngle(30).angle)
        Assert.assertEquals(45, GradientAngle(46).angle)
        Assert.assertEquals(0, GradientAngle(-20).angle)
        Assert.assertEquals(315, GradientAngle(-45).angle)
        Assert.assertEquals(315, GradientAngle(-46).angle)
        Assert.assertEquals(270, GradientAngle(640).angle)
        Assert.assertEquals(90, GradientAngle(-640).angle)
    }

}