package onlymash.flexbooru.ap.common

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import onlymash.flexbooru.ap.crash.CrashHandler
import onlymash.flexbooru.ap.data.api.Api
import onlymash.flexbooru.ap.data.db.MyDatabase
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.erased.bind
import org.kodein.di.erased.instance
import org.kodein.di.erased.provider
import org.kodein.di.erased.singleton

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
        bind() from singleton { instance<MyDatabase>().userDao() }
        bind() from singleton { instance<MyDatabase>().tagFilterDao() }
        bind() from singleton { instance<MyDatabase>().tagBlacklistDao() }
        bind() from singleton { Api.invoke() }
    }

    override fun onCreate() {
        super.onCreate()
        app = this
        CrashHandler.getInstance().init(this)
        AppCompatDelegate.setDefaultNightMode(Settings.nightMode)
    }
}