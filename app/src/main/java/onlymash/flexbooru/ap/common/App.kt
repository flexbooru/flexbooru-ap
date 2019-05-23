package onlymash.flexbooru.ap.common

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import com.squareup.picasso.Picasso
import onlymash.flexbooru.ap.data.api.Api
import onlymash.flexbooru.ap.data.db.MyDatabase
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton
import java.util.concurrent.Executors

class App : Application(), KodeinAware {

    companion object {
        lateinit var app: App
    }

    override val kodein: Kodein by Kodein.lazy {
        bind<Context>() with instance(this@App)
        bind<SharedPreferences>() with provider {
            instance<Context>().getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        }
        bind() from singleton { MyDatabase(instance()) }
        bind() from singleton { instance<MyDatabase>().postDao() }
        bind() from singleton { instance<MyDatabase>().detailDao() }
        bind() from singleton { Api() }
        bind() from singleton { Executors.newSingleThreadExecutor() }
        bind() from singleton { Picasso.Builder(instance()).build() }
    }

    override fun onCreate() {
        super.onCreate()
        app = this
        AppCompatDelegate.setDefaultNightMode(Settings.nightMode)
    }
}