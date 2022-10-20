package onlymash.flexbooru.ap.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding

abstract class BindingFragment<VB: ViewBinding> : KodeinFragment() {

    private var _binding: VB? = null
    val binding get() = _binding!!

    val isViewCreated
        get() = _binding != null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = onCreateBinding(inflater, container)
        return binding.root
    }

    abstract fun onCreateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): VB

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}