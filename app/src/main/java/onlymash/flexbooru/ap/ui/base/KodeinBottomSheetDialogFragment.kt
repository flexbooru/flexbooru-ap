package onlymash.flexbooru.ap.ui.base

import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI

abstract class KodeinBottomSheetDialogFragment : BottomSheetDialogFragment(), DIAware {
    override val di: DI by closestDI()
}