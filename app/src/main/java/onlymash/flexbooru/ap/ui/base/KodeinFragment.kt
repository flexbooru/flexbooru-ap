package onlymash.flexbooru.ap.ui.base

import androidx.fragment.app.Fragment
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein

abstract class KodeinFragment : Fragment(), KodeinAware {
    override val kodein: Kodein by kodein()
}