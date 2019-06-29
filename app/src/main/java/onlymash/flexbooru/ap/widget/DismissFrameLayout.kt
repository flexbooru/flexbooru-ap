package onlymash.flexbooru.ap.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.customview.widget.ViewDragHelper
import kotlin.math.max

class DismissFrameLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val dragHelper: ViewDragHelper
    private val minimumFlingVelocity: Int
    private var dismissListener: OnDismissListener? = null

    init {
        dragHelper = ViewDragHelper.create(this, 1f / 8f, ViewDragCallback())
        minimumFlingVelocity = ViewConfiguration.get(context).scaledMinimumFlingVelocity
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean =
        dragHelper.shouldInterceptTouchEvent(ev)

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.run {
            dragHelper.processTouchEvent(event)
        }
        return true
    }

    override fun computeScroll() {
        if (dragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    fun setDismissListener(dismissListener: OnDismissListener) {
        this.dismissListener = dismissListener
    }

    interface OnDismissListener {
        fun onStart()
        fun onProgress(progress: Float)
        fun onDismiss()
        fun onCancel()
    }

    private inner class ViewDragCallback : ViewDragHelper.Callback() {

        override fun tryCaptureView(child: View, pointerId: Int): Boolean = true

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int = 0

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int = max(0, top)

        override fun getViewHorizontalDragRange(child: View): Int = 0

        override fun getViewVerticalDragRange(child: View): Int = height

        override fun onViewCaptured(capturedChild: View, activePointerId: Int) {
            dismissListener?.onStart()
        }

        override fun onViewPositionChanged(changedView: View, left: Int, top: Int, dx: Int, dy: Int) {
            dismissListener?.onProgress(top.toFloat() / height.toFloat())
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            val slop = if (yvel > minimumFlingVelocity) height / 6 else height / 3
            if (releasedChild.top > slop) {
                dismissListener?.onDismiss()
            } else {
                dismissListener?.onCancel()
                dragHelper.settleCapturedViewAt(0, 0)
                invalidate()
            }
        }
    }
}