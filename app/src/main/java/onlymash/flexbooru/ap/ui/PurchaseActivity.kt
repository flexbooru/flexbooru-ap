package onlymash.flexbooru.ap.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.android.billingclient.api.*
import kotlinx.android.synthetic.main.activity_purchase.*
import kotlinx.android.synthetic.main.app_bar.*
import onlymash.flexbooru.ap.R
import onlymash.flexbooru.ap.common.INAPP_SKU
import onlymash.flexbooru.ap.common.Settings

class PurchaseActivity : AppCompatActivity() {

    private lateinit var billingClient: BillingClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_purchase)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setTitle(R.string.action_remove_ads)
        }
        val params = SkuDetailsParams
            .newBuilder()
            .setSkusList(listOf(INAPP_SKU))
            .setType(BillingClient.SkuType.INAPP)
            .build()
        billingClient = BillingClient
            .newBuilder(this)
            .enablePendingPurchases()
            .setListener { _, purchases ->
                val index = purchases?.indexOfFirst {
                    it.sku == INAPP_SKU && it.purchaseState == Purchase.PurchaseState.PURCHASED
                }
                if (index !== null && index >= 0) {
                    Settings.isPro = true
                    finish()
                }
            }
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult?) {
                if (billingClient.isReady) {
                    billingClient.querySkuDetailsAsync(params) { _, skuDetailsList ->
                        if (skuDetailsList != null) {
                            val index = skuDetailsList.indexOfFirst {
                                it.sku == INAPP_SKU
                            }
                            if (index >= 0) {
                                val detail = skuDetailsList[index]
                                pay.text = getString(
                                    R.string.placeholder_action_pay_by_google_play,
                                    detail.price
                                )
                            }
                        }
                    }
                }
            }
            override fun onBillingServiceDisconnected() {}
        })

        pay.setOnClickListener {
            payNow(billingClient, params)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun payNow(client: BillingClient, params: SkuDetailsParams) {
        if (client.isReady) {
            client.querySkuDetailsAsync(params) { billingResult, skuDetailsList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK &&
                    skuDetailsList != null) {
                    val index = skuDetailsList.indexOfFirst {
                        it.sku == INAPP_SKU
                    }
                    if (index >= 0) {
                        val billingFlowParams = BillingFlowParams
                            .newBuilder()
                            .setSkuDetails(skuDetailsList[index])
                            .build()
                        val result = client.launchBillingFlow(this, billingFlowParams)
                        if (result.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                            Settings.isPro = true
                            finish()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        billingClient.endConnection()
    }
}
