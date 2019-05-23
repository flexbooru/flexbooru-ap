package onlymash.flexbooru.ap.decoder

import android.content.Context
import android.graphics.*
import android.net.Uri
import androidx.core.net.toFile
import com.davemorrissey.labs.subscaleview.decoder.ImageRegionDecoder
import java.io.FileInputStream

class CustomRegionDecoder : ImageRegionDecoder {

    private val decoderLock = Any()

    private var decoder: BitmapRegionDecoder? = null

    override fun isReady(): Boolean = !(decoder?.isRecycled ?: true)

    override fun init(context: Context?, uri: Uri): Point {
        val inputStream = FileInputStream(uri.toFile())
        decoder = BitmapRegionDecoder.newInstance(inputStream, false)
        inputStream.close()
        return Point(decoder!!.width, decoder!!.height)
    }

    override fun decodeRegion(sRect: Rect, sampleSize: Int): Bitmap {
        synchronized(decoderLock) {
            val options = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            return decoder?.decodeRegion(sRect, options)
                ?: throw RuntimeException("Region decoder returned null bitmap - image format may not be supported")
        }
    }

    override fun recycle() {
        decoder?.recycle()
    }

}