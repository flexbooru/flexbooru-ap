package onlymash.flexbooru.ap.ui.base

import android.view.MenuItem
import onlymash.flexbooru.ap.R
import onlymash.flexbooru.ap.common.*

abstract class PostActivity : BaseActivity() {

    private var queryListener: QueryListener? = null

    internal fun setQueryListener(listener: QueryListener?) {
        queryListener = listener
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_order_date -> queryListener?.onOrderChange(ORDER_DATE)
            R.id.action_order_date_revers -> queryListener?.onOrderChange(ORDER_DATE_REVERS)
            R.id.action_order_rating -> queryListener?.onOrderChange(ORDER_RATING)
            R.id.action_order_downloads -> queryListener?.onOrderChange(ORDER_DOWNLOADS)
            R.id.action_order_size -> queryListener?.onOrderChange(ORDER_SIZE)
            R.id.action_order_tags_count -> queryListener?.onOrderChange(ORDER_TAGS_COUNT)
        }
        return super.onOptionsItemSelected(item)
    }
}