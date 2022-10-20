package onlymash.flexbooru.ap.common

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import onlymash.flexbooru.ap.data.api.Api
import onlymash.flexbooru.ap.data.db.MyDatabase
import org.kodein.di.*

fun appModules(application: Application) = DI.Module("AppModules") {
    bind<Context>() with instance(application)
    bind<SharedPreferences>() with provider {
        instance<Context>().getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
    }
    bind { singleton { MyDatabase(instance()) } }
    bind { provider { instance<MyDatabase>().postDao() } }
    bind { provider { instance<MyDatabase>().detailDao() } }
    bind { provider { instance<MyDatabase>().userDao() } }
    bind { provider { instance<MyDatabase>().tagFilterDao() } }
    bind { provider { instance<MyDatabase>().tagBlacklistDao() } }
    bind { singleton { Api.invoke() } }
}