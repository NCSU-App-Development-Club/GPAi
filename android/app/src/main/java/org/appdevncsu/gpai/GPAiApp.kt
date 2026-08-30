package org.appdevncsu.gpai

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.request.crossfade
import com.google.firebase.analytics.FirebaseAnalytics
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import okhttp3.OkHttpClient
import org.appdevncsu.gpai.api.Api
import org.appdevncsu.gpai.api.AuthorizationInterceptor
import org.appdevncsu.gpai.api.repositories.Repository
import org.appdevncsu.gpai.api.repositories.RepositoryImpl
import org.appdevncsu.gpai.room.AppDatabase
import org.appdevncsu.gpai.security.CredentialsStore
import org.appdevncsu.gpai.security.PreferencesManager
import org.appdevncsu.gpai.util.AnalyticsHelper
import org.appdevncsu.gpai.viewmodel.AuthViewModel
import org.appdevncsu.gpai.viewmodel.ChatRepository
import org.appdevncsu.gpai.viewmodel.TranscriptRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class GPAiApp : Application(), SingletonImageLoader.Factory {

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .crossfade(true)
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        PDFBoxResourceLoader.init(this)

        startKoin {
            androidContext(this@GPAiApp)
            modules(module {
                single { AuthorizationInterceptor() }
                single {
                    val interceptor: AuthorizationInterceptor = get()
                    Retrofit.Builder()
                        .baseUrl(BuildConfig.BASE_URL)
                        .client(
                            OkHttpClient.Builder()
                                .addInterceptor(interceptor)
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
                single { AppDatabase.getDatabase(androidContext()) }
                single { ChatRepository(get()) }
                viewModel {
                    TranscriptRepository(get())
                }
                single { CredentialsStore(androidContext()) }
                single { PreferencesManager(androidContext()) }
                single { AnalyticsHelper(FirebaseAnalytics.getInstance(this@GPAiApp)) }
                viewModel {
                    AuthViewModel(get(), get(), get())
                }
            })
        }
    }
}
