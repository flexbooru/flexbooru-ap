package onlymash.flexbooru.ap.ui.base

import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein

abstract class KodeinBottomSheetDialogFragment : BottomSheetDialogFragment(), KodeinAware {
    override val kodein: Kodein by kodein()
}