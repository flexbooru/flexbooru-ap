package onlymash.flexbooru.ap.ui.base

import androidx.appcompat.app.AppCompatActivity
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein

abstract class KodeinActivity : AppCompatActivity(), KodeinAware {
    override val kodein: Kodein by kodein()
}
