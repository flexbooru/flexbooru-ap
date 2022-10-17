package onlymash.flexbooru.ap.extension

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

inline fun <reified M : ViewModel> Fragment.getViewModel(viewModelFactory: ViewModelProvider.Factory): M {
    return ViewModelProvider(this, viewModelFactory)[M::class.java]
}

inline fun <reified M : ViewModel> AppCompatActivity.getViewModel(viewModelFactory: ViewModelProvider.Factory): M {
    return ViewModelProvider(this, viewModelFactory)[M::class.java]
}

inline fun <reified M : ViewModel> Fragment.getViewModel(viewModel: ViewModel): M {
    return getViewModel(object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return viewModel as T
        }
    })
}

inline fun <reified M : ViewModel> AppCompatActivity.getViewModel(viewModel: ViewModel): M {
    return getViewModel(object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return viewModel as T
        }
    })
}

inline fun <reified M : ViewModel> Fragment.getViewModel(): M {
    return ViewModelProvider(this)[M::class.java]
}

inline fun <reified M : ViewModel> AppCompatActivity.getViewModel(): M {
    return ViewModelProvider(this)[M::class.java]
}