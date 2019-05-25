package onlymash.flexbooru.ap.extension

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders

inline fun <reified M : ViewModel> Fragment.getViewModel(viewModelFactory: ViewModelProvider.Factory): M {
    return ViewModelProvider(this, viewModelFactory).get(M::class.java)
}

inline fun <reified M : ViewModel> AppCompatActivity.getViewModel(viewModelFactory: ViewModelProvider.Factory): M {
    return ViewModelProvider(this, viewModelFactory).get(M::class.java)
}

inline fun <reified M : ViewModel> Fragment.getViewModel(): M {
    return ViewModelProviders.of(this).get(M::class.java)
}

inline fun <reified M : ViewModel> AppCompatActivity.getViewModel(): M {
    return ViewModelProviders.of(this).get(M::class.java)
}