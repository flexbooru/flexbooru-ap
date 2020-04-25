package onlymash.flexbooru.ap.extension

import androidx.annotation.IdRes
import androidx.fragment.app.FragmentActivity
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment

fun FragmentActivity.findNavController(@IdRes viewId: Int) =
    (supportFragmentManager.findFragmentById(viewId) as? NavHostFragment)?.navController
        ?: Navigation.findNavController(this, viewId)