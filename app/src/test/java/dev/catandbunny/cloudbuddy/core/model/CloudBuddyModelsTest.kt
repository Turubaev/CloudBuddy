package dev.catandbunny.cloudbuddy.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class CloudBuddyModelsTest {
    @Test
    fun anxiousMoodCreatesRainWithoutPunishingUser() {
        assertEquals(CloudWeather.RAINY, weatherFor(UserMood.ANXIOUS))
    }

    @Test
    fun goodMoodCreatesSoftWeather() {
        assertEquals(CloudWeather.SOFT, weatherFor(UserMood.GOOD))
    }
}
