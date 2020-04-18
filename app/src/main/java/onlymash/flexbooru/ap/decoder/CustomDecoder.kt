package onlymash.flexbooru.ap.decoder

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.davemorrissey.labs.subscaleview.decoder.ImageDecoder
import onlymash.flexbooru.ap.glide.GlideRequests

class CustomDecoder(private val glide: GlideRequests) : ImageDecoder {
    override fun decode(context: Context?, uri: Uri): Bitmap {
        return glide.asBitmap()
            .load(uri)
            .submit()
            .get()
    }
}