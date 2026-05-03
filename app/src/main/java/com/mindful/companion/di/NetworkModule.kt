package com.mindful.companion.di

import com.mindful.companion.BuildConfig
import com.mindful.companion.data.api.AuthApiService
import com.mindful.companion.data.api.MindfulApiService
import com.mindful.companion.data.api.PostApiService
import com.mindful.companion.data.api.RAGApiService
import com.mindful.companion.data.api.WeeklyReportApiService
import com.mindful.companion.data.api.ZhipuAIService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    @Named("default")
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @Named("default")
    fun provideRetrofit(@Named("default") okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.MINDFUL_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideMindfulApiService(@Named("default") retrofit: Retrofit): MindfulApiService {
        return retrofit.create(MindfulApiService::class.java)
    }

    // ZhipuAIService 现在通过后端代理 (/api/chat/completions)，不再直连 DeepSeek
    // API Key 由后端环境变量管理，不再存在于客户端
    @Provides
    @Singleton
    fun provideZhipuAIService(@Named("default") retrofit: Retrofit): ZhipuAIService {
        return retrofit.create(ZhipuAIService::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthApiService(@Named("default") retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }

    @Provides
    @Singleton
    fun providePostApiService(@Named("default") retrofit: Retrofit): PostApiService {
        return retrofit.create(PostApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideChatApiService(@Named("default") retrofit: Retrofit): com.mindful.companion.data.api.ChatApiService {
        return retrofit.create(com.mindful.companion.data.api.ChatApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideAdminApiService(@Named("default") retrofit: Retrofit): com.mindful.companion.data.api.AdminApiService {
        return retrofit.create(com.mindful.companion.data.api.AdminApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideRAGApiService(@Named("default") retrofit: Retrofit): RAGApiService {
        return retrofit.create(RAGApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideWeeklyReportApiService(@Named("default") retrofit: Retrofit): WeeklyReportApiService {
        return retrofit.create(WeeklyReportApiService::class.java)
    }
}
