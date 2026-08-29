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

    @Test
    fun freeTierUsesWelcomePackBeforeDailyQuota() {
        val allowance = CloudBuddyState(welcomeAiMessagesUsed = 12).hostedChatAllowance(todayEpochDay = 100)

        assertEquals(3, allowance.remaining)
        assertEquals(true, allowance.isWelcomePack)
    }

    @Test
    fun freeTierResetsDailyQuotaOnNewDay() {
        val allowance = CloudBuddyState(
            welcomeAiMessagesUsed = TariffPolicy.FREE_WELCOME_MESSAGES,
            dailyAiMessagesUsed = TariffPolicy.FREE_DAILY_MESSAGES,
            dailyAiEpochDay = 99,
        ).hostedChatAllowance(todayEpochDay = 100)

        assertEquals(TariffPolicy.FREE_DAILY_MESSAGES, allowance.remaining)
    }

    @Test
    fun plusHasSeparateDailyLimit() {
        val allowance = CloudBuddyState(
            subscriptionTier = SubscriptionTier.PLUS,
            dailyAiMessagesUsed = 7,
            dailyAiEpochDay = 100,
        ).hostedChatAllowance(todayEpochDay = 100)

        assertEquals(23, allowance.remaining)
        assertEquals(false, allowance.isWelcomePack)
    }
}
