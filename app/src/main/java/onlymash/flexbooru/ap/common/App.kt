package onlymash.flexbooru.ap.common

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import com.android.billingclient.api.*
import com.squareup.picasso.Picasso
import onlymash.flexbooru.ap.data.api.Api
import onlymash.flexbooru.ap.data.db.MyDatabase
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.erased.bind
import org.kodein.di.erased.instance
import org.kodein.di.erased.provider
import org.kodein.di.erased.singleton
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
        bind() from singleton { instance<MyDatabase>().userDao() }
        bind() from singleton { Api() }
        bind() from singleton { Executors.newSingleThreadExecutor() }
        bind() from singleton { Picasso.Builder(instance()).build() }
    }

    override fun onCreate() {
        super.onCreate()
        app = this
        AppCompatDelegate.setDefaultNightMode(Settings.nightMode)
        checkOrder()
    }

    private fun checkOrder() {
        val billingClient = BillingClient
            .newBuilder(this)
            .enablePendingPurchases()
            .setListener { _, purchases ->
                val index = purchases?.indexOfFirst {
                    it.sku == INAPP_SKU && it.purchaseState == Purchase.PurchaseState.PURCHASED
                }
                if (index !== null && index >= 0) {
                    Settings.isPro = true
                }
            }
            .build()
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult?) {
                if (billingClient.isReady) {
                    val purchases = billingClient.queryPurchases(BillingClient.SkuType.INAPP)?.purchasesList
                    if (purchases != null) {
                        val index = purchases.indexOfFirst {
                            it.sku == INAPP_SKU && it.purchaseState == Purchase.PurchaseState.PURCHASED
                        }
                        if (index >= 0) {
                            val purchase = purchases[index]
                            if (!purchase.isAcknowledged) {
                                billingClient.acknowledgePurchase(
                                    AcknowledgePurchaseParams.newBuilder()
                                        .setPurchaseToken(purchase.purchaseToken)
                                        .build()
                                ) {

                                }
                            }
                            Settings.isPro = true
                        } else Settings.isPro = false
                    } else {
                        Settings.isPro = false
                    }
                }
                billingClient.endConnection()
            }
            override fun onBillingServiceDisconnected() {}
        })
    }
}