package com.gbksoft.neighbourhood.ui.widgets.avatar

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DimenRes
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.gbksoft.neighbourhood.R
import timber.log.Timber
import java.io.File
import java.util.*

class AvatarView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private lateinit var ivAvatar: ImageView
    private lateinit var tvEmpty: TextView

    private var initials: String? = null
    private var imageUrl: String? = null
    private var imageBitmap: Bitmap? = null
    private var borderEnabled: Boolean = false
    private var isEmptyStateEnabled: Boolean = false
    private var borderThickness: Int = 0
    private var borderPadding: Int = 0
    private val borderZeroPadding = 1
    private var initialsSize: Int = 0
    private var isBusiness: Boolean = false

    var isBitmapDrawingMode = false

    @ColorInt
    private val basicBorderColor = ContextCompat.getColor(context, R.color.basic_avatar_border_color)

    @ColorInt
    private val businessBorderColor = ContextCompat.getColor(context, R.color.business_avatar_border_color)

    @ColorInt
    private var customBorderColor: Int? = null

    private var paint = Paint().apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
    }

    init {
        attrs?.let { extractAttrs(it) }
        LayoutInflater.from(context).inflate(R.layout.layout_avatar, this, true)
    }

    private fun extractAttrs(attrs: AttributeSet) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.AvatarView)
        try {
            val fullName = a.getString(R.styleable.AvatarView_av_fullName)
            fullName?.let { initials = getFirstLetter(it) }
            isBitmapDrawingMode = a.getBoolean(R.styleable.AvatarView_av_isBitmapDrawingMode, isBitmapDrawingMode)
            imageUrl = a.getString(R.styleable.AvatarView_av_imageUrl)
            borderEnabled = a.getBoolean(R.styleable.AvatarView_av_borderEnabled, borderEnabled)
            borderThickness = a.getDimensionPixelSize(R.styleable.AvatarView_av_borderThickness, borderThickness)
            borderPadding = a.getDimensionPixelSize(R.styleable.AvatarView_av_borderPadding, borderPadding)
            initialsSize = a.getDimensionPixelSize(R.styleable.AvatarView_av_initialsSize, initialsSize)
            isBusiness = a.getBoolean(R.styleable.AvatarView_av_isBusiness, isBusiness)
            isEmptyStateEnabled = a.getBoolean(R.styleable.AvatarView_av_isEmptyStateEnabled, isEmptyStateEnabled)
            if (a.hasValue(R.styleable.AvatarView_av_borderColor)) {
                customBorderColor = a.getColor(R.styleable.AvatarView_av_borderColor, basicBorderColor)
            }
        } finally {
            a.recycle()
        }
    }

    fun setFullName(fullName: String?) {
        initials = if (fullName != null) {
            getFirstLetter(fullName)
        } else {
            ""
        }
        setupImage()
    }

    fun setImage(url: String?) {
        if (url != null) imageBitmap = null
        imageUrl = url
        setupImage()
    }

    fun setImage(bitmap: Bitmap?) {
        if (imageBitmap != null) imageUrl = null
        imageBitmap = bitmap
        setupImage()
    }

    fun setImage(file: File?) {
        if (file != null) imageUrl = null
        imageBitmap = if (file == null) null else BitmapFactory.decodeFile(file.absolutePath)
        setupImage()
    }

    private fun getFirstLetter(fullName: String): String? {
        val sb = StringBuilder()
        val arr: Array<String> = fullName.trim().split(Regex("\\s++")).toTypedArray()
        if (arr.isNotEmpty()) {
            if (arr[0].isNotEmpty()) sb.append(arr[0][0])
        }
        return sb.toString().toUpperCase(Locale.ROOT)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        findViews()
        setup()
    }

    private fun findViews() {
        ivAvatar = findViewById(R.id.av_ivAvatar)
        tvEmpty = findViewById(R.id.av_tvAVInitials)
    }

    private fun setup() {
        setupBorder()
        setupEmptyAvatar()
        setupTextView()
        setupImage()
    }

    private fun setupTextView() {
        if (initialsSize > 0) {
            tvEmpty.setTextSize(TypedValue.COMPLEX_UNIT_PX, initialsSize.toFloat())
        }
    }

    private fun setupBorder() {
        if (borderEnabled) {
            val padding = borderThickness + borderPadding
            setPadding(padding, padding, padding, padding)
        } else {
            setPadding(0, 0, 0, 0)
        }
    }

    private fun setupImage() {
        if (TextUtils.isEmpty(imageUrl) && imageBitmap == null) {
            Glide.with(ivAvatar).clear(ivAvatar)
            ivAvatar.visibility = View.INVISIBLE
            tvEmpty.text = initials
            tvEmpty.visibility = View.VISIBLE
        } else if (imageBitmap == null) {
            tvEmpty.visibility = View.INVISIBLE
            ivAvatar.visibility = View.VISIBLE
            Glide.with(ivAvatar)
                .load(imageUrl)
                .listener(GlideResourceReadyListener(::onImageLoadingFailed))
                .into(ivAvatar)
        } else {
            tvEmpty.visibility = View.INVISIBLE
            ivAvatar.visibility = View.VISIBLE
            Glide.with(ivAvatar).clear(ivAvatar)
            ivAvatar.setImageBitmap(imageBitmap)
        }
    }

    private fun onImageLoadingFailed() {
        ivAvatar.visibility = View.INVISIBLE
        tvEmpty.text = initials
        tvEmpty.visibility = View.VISIBLE
    }

    private fun setupEmptyAvatar() {
        when {
            isEmptyStateEnabled -> tvEmpty.background = null
            isBusiness -> tvEmpty.setBackgroundResource(R.drawable.bg_empty_avatar_business)
            else -> tvEmpty.setBackgroundResource(R.drawable.bg_empty_avatar_basic)
        }
    }

    override fun dispatchDraw(canvas: Canvas?) {
        super.dispatchDraw(canvas)
        if (!borderEnabled && !isBitmapDrawingMode) return

        if (canvas == null) return

        preparePaint()
        val cx = measuredWidth.toFloat() / 2
        val cy = measuredHeight.toFloat() / 2
        val borderThickness = borderThickness.toFloat()
        val radiusSubtracting = borderThickness + if (borderPadding > 0) -borderPadding / 2 else borderZeroPadding
        val radius = if (measuredWidth < measuredHeight) cx - radiusSubtracting else cy - radiusSubtracting

        if (isBitmapDrawingMode) {
            Timber.tag("MapTag2").d("dispatchDraw: ${canvas.width} x ${canvas.height}")
            Timber.tag("MapTag2").d("measured: $measuredWidth x $measuredHeight")
            canvas.save()
            canvas.translate(radiusSubtracting, radiusSubtracting)
            if (ivAvatar.drawable != null) {
                ivAvatar.draw(canvas)
            } else {
                tvEmpty.draw(canvas)
            }
            canvas.restore()
        }
        canvas.drawCircle(cx, cy, radius, paint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var widthSpec = widthMeasureSpec
        var heightSpec = heightMeasureSpec
        if (isBitmapDrawingMode) {
            widthSpec = MeasureSpec.makeMeasureSpec(
                MeasureSpec.getSize(layoutParams.width),
                MeasureSpec.EXACTLY)
            heightSpec = MeasureSpec.makeMeasureSpec(
                MeasureSpec.getSize(layoutParams.height),
                MeasureSpec.EXACTLY)
        }
        super.onMeasure(widthSpec, heightSpec)
    }

    private fun preparePaint() {
        val customColor = customBorderColor
        paint.color = customColor ?: if (isBusiness) businessBorderColor else basicBorderColor

        if (borderThickness > 0 && borderPadding == 0) {
            paint.strokeWidth = borderThickness.toFloat() + borderZeroPadding
        } else {
            paint.strokeWidth = borderThickness.toFloat()
        }
        Timber.tag("AvatarTag").d("borderThickness: ${borderThickness}   borderPadding: $borderPadding paint.strokeWidth: ${paint.strokeWidth}")
    }

    fun isNotEmpty(): Boolean = !isEmpty()

    fun isEmpty(): Boolean = TextUtils.isEmpty(imageUrl) && imageBitmap == null

    fun setBorderThickness(@DimenRes thickness: Int) {
        borderThickness = resources.getDimensionPixelSize(thickness)
        setupBorder()
        postInvalidate()
    }

    fun setBusiness(isBusiness: Boolean) {
        this.isBusiness = isBusiness
        setupEmptyAvatar()
        postInvalidate()
    }

    fun reset() {
        ivAvatar.setImageDrawable(null)
        ivAvatar.setImageBitmap(null)
    }

    companion object {
        @JvmStatic
        @BindingAdapter("app:av_fullName")
        fun setInitials(avatarView: AvatarView, fullName: String?) {
            avatarView.setFullName(fullName)
        }

        @JvmStatic
        @BindingAdapter("app:av_imageUrl")
        fun setImage(avatarView: AvatarView, url: String?) {
            avatarView.setImage(url)
        }

        @JvmStatic
        @BindingAdapter("app:av_imageBitmap")
        fun setImage(avatarView: AvatarView, bitmap: Bitmap?) {
            avatarView.setImage(bitmap)
        }

        @JvmStatic
        @BindingAdapter("app:av_imageFile")
        fun setImage(avatarView: AvatarView, file: File?) {
            avatarView.setImage(file)
        }

        @JvmStatic
        @BindingAdapter("app:av_isBusiness")
        fun setBusiness(avatarView: AvatarView, isBusiness: Boolean) {
            avatarView.setBusiness(isBusiness)
        }
    }

    private class GlideResourceReadyListener(
        private val onLoadFailed: (() -> Unit)? = null
    ) : RequestListener<Drawable> {

        override fun onResourceReady(resource: Drawable?,
                                     model: Any?,
                                     target: Target<Drawable>?,
                                     dataSource: DataSource?,
                                     isFirstResource: Boolean): Boolean {
            return false
        }

        override fun onLoadFailed(e: GlideException?,
                                  model: Any?,
                                  target: Target<Drawable>?,
                                  isFirstResource: Boolean): Boolean {

            onLoadFailed?.invoke()
            return false
        }

    }

}