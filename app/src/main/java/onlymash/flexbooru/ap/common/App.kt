package onlymash.flexbooru.ap.common

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import org.kodein.di.DI
import org.kodein.di.DIAware

class App : Application(), DIAware {

    companion object {
        lateinit var app: App
    }

    override val di: DI by DI.lazy { import(appModules(this@App)) }

    override fun onCreate() {
        super.onCreate()
        app = this
        AppCompatDelegate.setDefaultNightMode(Settings.nightMode)
    }
}