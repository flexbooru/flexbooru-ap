package onlymash.flexbooru.ap.decoder

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.net.toFile
import com.davemorrissey.labs.subscaleview.decoder.ImageDecoder
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso

class CustomDecoder(private val picasso: Picasso) : ImageDecoder {
    override fun decode(context: Context?, uri: Uri): Bitmap {
        return picasso.load(uri.toFile())
            .config(Bitmap.Config.ARGB_8888)
            .memoryPolicy(MemoryPolicy.NO_CACHE)
            .get()
    }
}