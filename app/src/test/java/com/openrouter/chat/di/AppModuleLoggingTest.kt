package com.openrouter.chat.di

import okhttp3.logging.HttpLoggingInterceptor
import org.junit.Assert.assertEquals
import org.junit.Test

class AppModuleLoggingTest {

    @Test
    fun `loggingLevel returns BODY for debug builds`() {
        assertEquals(HttpLoggingInterceptor.Level.BODY, loggingLevel(isDebug = true))
    }

    @Test
    fun `loggingLevel returns NONE for release builds`() {
        assertEquals(HttpLoggingInterceptor.Level.NONE, loggingLevel(isDebug = false))
    }
}
