package com.openrouter.chat.di

import android.content.Context
import androidx.room.Room
import com.openrouter.chat.data.local.AppDatabase
import com.openrouter.chat.data.local.dao.ConversationDao
import com.openrouter.chat.data.local.dao.MessageDao
import com.openrouter.chat.data.local.dao.ModelDao
import com.openrouter.chat.data.remote.api.OpenRouterApi
import com.openrouter.chat.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/** Returns the OkHttp logging level appropriate for the build type. */
internal fun loggingLevel(isDebug: Boolean): HttpLoggingInterceptor.Level =
    if (isDebug) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val BASE_URL = "https://openrouter.ai/api/v1/"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = loggingLevel(BuildConfig.DEBUG)
        }
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                    .header("Content-Type", "application/json")
                chain.proceed(requestBuilder.build())
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideOpenRouterApi(retrofit: Retrofit): OpenRouterApi {
        return retrofit.create(OpenRouterApi::class.java)
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "openrouter_chat.db"
        ).build()
    }

    @Provides
    fun provideModelDao(database: AppDatabase): ModelDao = database.modelDao()

    @Provides
    fun provideConversationDao(database: AppDatabase): ConversationDao = database.conversationDao()

    @Provides
    fun provideMessageDao(database: AppDatabase): MessageDao = database.messageDao()
}