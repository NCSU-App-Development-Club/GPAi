package org.appdevncsu.gpai

import android.app.Application
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import org.appdevncsu.gpai.api.Api
import org.appdevncsu.gpai.api.AuthorizationInterceptor
import org.appdevncsu.gpai.api.repositories.Repository
import org.appdevncsu.gpai.api.repositories.RepositoryImpl
import org.appdevncsu.gpai.room.AppDatabase
import org.appdevncsu.gpai.onboarding.TranscriptRepository
import org.appdevncsu.gpai.viewmodel.AuthViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class GPAiApp : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@GPAiApp)
            modules(module {
                single {
                    Retrofit.Builder()
                        .baseUrl("https://gpai-backend.ncsuappdevelopmentclub.workers.dev/")
                        .client(
                            OkHttpClient.Builder()
                                .addInterceptor(AuthorizationInterceptor)
                                .build()
                        )
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                }
                single {
                    val retrofit: Retrofit = get()
                    retrofit.create(Api::class.java)
                }
                single {
                    val api: Api = get()
                    RepositoryImpl(api = api)
                } bind Repository::class
                viewModel {
                    TranscriptRepository(AppDatabase.getDatabase(androidContext()))
                }
                viewModel {
                    AuthViewModel(AppDatabase.getDatabase(androidContext()))
                }
            })
        }
    }
}
