package onlymash.flexbooru.ap.ui.base

import android.os.Bundle
import onlymash.flexbooru.ap.R

abstract class BaseBottomSheetDialogFragment : KodeinBottomSheetDialogFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.TransparentBottomSheetDialogTheme)
    }
}