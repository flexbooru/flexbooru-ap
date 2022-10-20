package onlymash.flexbooru.ap.extension

import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView

fun AppCompatActivity.setupInsets(insetsCallback: (insets: Insets) -> Unit) {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
        val systemBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.updatePadding(left = systemBarInsets.left, systemBarInsets.right)
        insetsCallback.invoke(systemBarInsets)
        WindowInsetsCompat.Builder()
            .setInsets(WindowInsetsCompat.Type.systemBars(), Insets.of(0, systemBarInsets.top, 0, systemBarInsets.bottom))
            .build()
    }
    window.showSystemBars()
}

fun RecyclerView.setupBottomPadding() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
        updatePadding(bottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom)
        insets
    }
}

fun RecyclerView.setupBottomPaddingWithProgressBar(progressBar: ProgressBar) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
        val bottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
        updatePadding(bottom = bottom)
        progressBar.updatePadding(bottom = bottom)
        insets
    }
}
