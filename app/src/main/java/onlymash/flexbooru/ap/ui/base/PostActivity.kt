package onlymash.flexbooru.ap.ui.base

import android.text.InputType
import android.view.Gravity
import android.view.MenuItem
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import onlymash.flexbooru.ap.R
import onlymash.flexbooru.ap.common.*
import onlymash.flexbooru.ap.worker.MuzeiArtWorker

abstract class PostActivity : BaseActivity() {

    private var currentAspectRatio: String = ""
    private var isCheckedJpg = true
    private var isCheckedPng = true
    private var isCheckedGif = true

    abstract var query: String

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

            R.id.action_date_range_anytime -> queryListener?.onDateRangeChange(DATE_RANGE_ANYTIME)
            R.id.action_date_range_past_day -> queryListener?.onDateRangeChange(DATE_RANGE_PAST_DAY)
            R.id.action_date_range_past_week -> queryListener?.onDateRangeChange(DATE_RANGE_PAST_WEEK)
            R.id.action_date_range_past_month -> queryListener?.onDateRangeChange(DATE_RANGE_PAST_MONTH)

            R.id.action_input_aspect_ratio -> changeAspectRatio()

            R.id.action_extension_jpg -> {
                isCheckedJpg = !item.isChecked
                item.isChecked = isCheckedJpg
                queryListener?.onExtensionChange(isCheckedJpg, isCheckedPng, isCheckedGif)
            }
            R.id.action_extension_png -> {
                isCheckedPng = !item.isChecked
                item.isChecked = isCheckedPng
                queryListener?.onExtensionChange(isCheckedJpg, isCheckedPng, isCheckedGif)
            }
            R.id.action_extension_gif -> {
                isCheckedGif = !item.isChecked
                item.isChecked = isCheckedGif
                queryListener?.onExtensionChange(isCheckedJpg, isCheckedPng, isCheckedGif)
            }
            R.id.action_muzei_set -> {
                Settings.muzeiQuery = query
                MuzeiArtWorker.enqueueLoad()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun changeAspectRatio() {
        val layout = FrameLayout(this)
        val editText = EditText(this)
        layout.addView(editText)
        val margin = resources.getDimensionPixelSize(R.dimen.margin_horizontal_edit_text_dialog)
        editText.apply {
            setText(currentAspectRatio)
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT).apply {
                marginStart = margin
                marginEnd = margin
            }
            maxLines = 1
            inputType = InputType.TYPE_CLASS_TEXT
            gravity = Gravity.CENTER_HORIZONTAL
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.posts_aspect_ratio)
            .setView(layout)
            .setPositiveButton(R.string.dialog_ok) { _, _ ->
                currentAspectRatio = editText.text?.toString() ?: ""
                queryListener?.onAspectRatioChange(currentAspectRatio)
            }
            .setNegativeButton(R.string.dialog_cancel, null)
            .create()
            .show()
    }
}