package onlymash.flexbooru.ap.extension

import android.view.View

fun View.toVisibility(constraint : Boolean) {
    visibility = if (constraint) {
        View.VISIBLE
    } else {
        View.GONE
    }
}