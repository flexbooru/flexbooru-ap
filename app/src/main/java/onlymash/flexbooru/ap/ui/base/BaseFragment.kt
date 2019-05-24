package onlymash.flexbooru.ap.ui.base

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*

abstract class BaseFragment : KodeinFragment() {

    @ExperimentalCoroutinesApi
    override fun onDestroy() {
        super.onDestroy()
        lifecycleScope.cancel()
    }
}