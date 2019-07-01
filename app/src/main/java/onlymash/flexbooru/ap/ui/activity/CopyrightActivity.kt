package onlymash.flexbooru.ap.ui.activity

import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.text.HtmlCompat
import androidx.core.text.parseAsHtml
import kotlinx.android.synthetic.main.activity_copyright.*
import kotlinx.android.synthetic.main.app_bar.*
import onlymash.flexbooru.ap.R
import onlymash.flexbooru.ap.extension.launchUrl

class CopyrightActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_copyright)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setTitle(R.string.title_copyright)
        }
        copyright.apply {
            text = SpannableStringBuilder(resources.openRawResource(R.raw.copyright).bufferedReader().readText()
                .parseAsHtml(HtmlCompat.FROM_HTML_SEPARATOR_LINE_BREAK_LIST)).apply {
                for (span in getSpans(0, length, URLSpan::class.java)) {
                    setSpan(object : ClickableSpan() {
                        override fun onClick(view: View) {
                            if (span.url.startsWith("mailto:")) {
                                startActivity(Intent.createChooser(Intent().apply {
                                    action = Intent.ACTION_SENDTO
                                    data = span.url.toUri()
                                }, getString(R.string.share_via)))
                            } else this@CopyrightActivity.launchUrl(span.url)
                        }
                    }, getSpanStart(span), getSpanEnd(span), getSpanFlags(span))
                    removeSpan(span)
                }
            }
            movementMethod = LinkMovementMethod.getInstance()
        }
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
