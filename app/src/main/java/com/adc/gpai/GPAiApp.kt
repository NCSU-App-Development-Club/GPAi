package com.adc.gpai

import android.app.Application
import com.adc.gpai.api.Api
import com.adc.gpai.api.repositories.Repository
import com.adc.gpai.api.repositories.RepositoryImpl
import org.koin.core.context.startKoin
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class GPAiApp : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            modules(module {
                single {
                    Retrofit.Builder()
                        .baseUrl("https://api.openai.com/v1/chat/")
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
            })
        }
    }
}
