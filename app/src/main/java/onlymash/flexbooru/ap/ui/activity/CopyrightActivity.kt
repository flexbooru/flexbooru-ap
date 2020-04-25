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
import androidx.core.view.updatePadding
import onlymash.flexbooru.ap.R
import onlymash.flexbooru.ap.databinding.ActivityCopyrightBinding
import onlymash.flexbooru.ap.extension.launchUrl
import onlymash.flexbooru.ap.viewbinding.viewBinding
import onlymash.flexbooru.ap.widget.setupInsets

class CopyrightActivity : AppCompatActivity() {

    private val binding by viewBinding(ActivityCopyrightBinding::inflate)
    private val toolbar get() = binding.layoutAppBar.toolbar
    private val toolbarContainer get() = binding.layoutAppBar.containerToolbar
    private val copyright get() = binding.copyright

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupInsets { insets ->
            toolbarContainer.minimumHeight = toolbar.minimumHeight + insets.systemWindowInsetTop
            copyright.updatePadding(bottom = insets.systemWindowInsetBottom + resources.getDimensionPixelSize(R.dimen.copyright_padding))
        }
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
