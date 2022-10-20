package onlymash.flexbooru.ap.ui.base

import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.*

abstract class BaseFragment<VB: ViewBinding> : BindingFragment<VB>() {

    @ExperimentalCoroutinesApi
    override fun onDestroy() {
        super.onDestroy()
        lifecycleScope.cancel()
    }
}