package com.openrouter.chat.ui.screens.settings

import com.openrouter.chat.data.remote.api.OpenRouterApi
import com.openrouter.chat.data.remote.dto.ApiKeyInfo
import com.openrouter.chat.data.remote.dto.ApiKeyResponse
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.junit.Assume.assumeTrue
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class SettingsValidationTest {

    private lateinit var openRouterApi: OpenRouterApi
    private var testApiKey: String? = null

    @Before
    fun setup() {
        testApiKey = System.getenv("OPENROUTER_API_KEY")

        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://openrouter.ai/api/v1/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        openRouterApi = retrofit.create(OpenRouterApi::class.java)
    }

    @Test
    fun `invalid api key returns 401`() = runTest {
        val response = openRouterApi.validateKey("Bearer wrong-key-xyz-123")

        assertEquals(401, response.code())
    }

    @Test
    fun `random string returns 401`() = runTest {
        val response = openRouterApi.validateKey("Bearer not-a-real-api-key")

        assertEquals(401, response.code())
    }

    @Test
    fun `empty key returns 401`() = runTest {
        val response = openRouterApi.validateKey("Bearer ")

        assertEquals(401, response.code())
    }

    @Test
    fun `malformed key without Bearer prefix returns 401`() = runTest {
        val response = openRouterApi.validateKey("sk-or-v1-abcdef")

        assertEquals(401, response.code())
    }

    @Test
    fun `valid user-provided key is accepted`() = runTest {
        assumeTrue("Skipping test - no API key provided. Set OPENROUTER_API_KEY env var", testApiKey != null)

        val response = openRouterApi.validateKey("Bearer $testApiKey")

        assertTrue("Valid key should return 200", response.isSuccessful)
        assertNotNull(response.body())
        assertTrue(response.body()!!.data.label.isNotBlank())
    }
}