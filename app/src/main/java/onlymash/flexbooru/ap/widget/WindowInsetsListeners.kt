package onlymash.flexbooru.ap.widget

import android.view.View
import android.view.WindowInsets
import androidx.core.view.updatePadding

object ListListener : View.OnApplyWindowInsetsListener {
    override fun onApplyWindowInsets(view: View, insets: WindowInsets) = insets.apply {
        view.updatePadding(bottom = systemWindowInsetBottom)
    }
}
