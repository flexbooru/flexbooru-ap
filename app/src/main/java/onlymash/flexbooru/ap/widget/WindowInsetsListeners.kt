package onlymash.flexbooru.ap.widget

import android.view.View
import android.view.WindowInsets
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updatePadding

fun AppCompatActivity.setupInsets(insetsCallback: (insets: WindowInsets) -> Unit) {
    findViewById<View>(android.R.id.content).apply {
        setOnApplyWindowInsetsListener { v, insets ->
            v.updatePadding(left = insets.systemWindowInsetLeft, right = insets.systemWindowInsetRight)
            insetsCallback(insets)
            @Suppress("DEPRECATION")
            insets.replaceSystemWindowInsets(0, insets.systemWindowInsetTop, 0, insets.systemWindowInsetBottom)
        }
        systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    }
}

object ListListener : View.OnApplyWindowInsetsListener {
    override fun onApplyWindowInsets(view: View, insets: WindowInsets) = insets.apply {
        view.updatePadding(bottom = systemWindowInsetBottom)
    }
}
