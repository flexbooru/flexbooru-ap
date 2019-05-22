package onlymash.flexbooru.ap.ui

import android.os.Bundle
import onlymash.flexbooru.ap.R
import onlymash.flexbooru.ap.ui.base.KodeinActivity

class DetailActivity : KodeinActivity() {

    companion object {
        fun startDetailActivity() {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
    }
}
