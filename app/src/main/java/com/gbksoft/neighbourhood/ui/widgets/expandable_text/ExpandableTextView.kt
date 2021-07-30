package com.gbksoft.neighbourhood.ui.widgets.expandable_text

import android.content.Context
import android.graphics.Typeface
import android.text.*
import android.text.style.ClickableSpan
import android.text.util.Linkify
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import com.gbksoft.neighbourhood.R


class ExpandableTextView : AppCompatTextView, View.OnClickListener, View.OnLongClickListener, OnHashtagClickListener, OnMentionClickListener {
    enum class TapArea { ELLIPSIS, TEXT }

    private var initialized = false

    var onExpandListener: (() -> Unit)? = null
    var onCollapseListener: (() -> Unit)? = null
    private var originMaxLines = Int.MAX_VALUE
    private var expandEllipsis = getDefaultEllipsis().toString()
    private var collapseEllipsis: String? = null
    private var ellipsisColor = getDefaultEllipsisColor()
    private var ellipsisTypeface: Typeface? = null
    private var tapArea = TapArea.ELLIPSIS
    private var hashtagTypeface: Typeface? = null

    private lateinit var expandEllipsisSpannable: SpannableString
    private var collapseEllipsisSpannable: SpannableString? = null
    private val spannableStringBuilder = SpannableStringBuilder()

    private var originText: CharSequence? = null
    private var bufferType: BufferType? = null

    private var isExpanded = false
    private val isCollapsed: Boolean
        get() = !isExpanded

    private var canExpand = true

    private var ellipsisClickTime: Long = 0

    private val textMeasurementParamsBuilder = TextMeasurementUtils.TextMeasurementParams.Builder()

    private var canExpanded: Boolean = false

    private var hashtagsEnabled = false
    private val hashtagManager = HashtagManager(this)
    private val mentionManager = MentionManager(this)

    var onHashTagClickListener: ((hashtag: String) -> Unit)? = null
    var onMentionClickListener: ((profileId: Long) -> Unit)? = null

    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    fun setCollapsedText(text: CharSequence?) {
        setExpanded(false)
        setText(text)
    }

    fun setExpandedText(text: CharSequence?) {
        setExpanded(true)
        setText(text)
    }

    private fun setExpanded(isExpanded: Boolean) {
        this.isExpanded = isExpanded
        maxLines = if (isExpanded) Int.MAX_VALUE else originMaxLines
    }

    //Called from parent constructor
    override fun setText(text: CharSequence?, type: BufferType?) {
        this.originText = text
        this.bufferType = type
        if (initialized) onSetOriginText()
    }

    //Need called after this constructor (initialized == true)
    private fun onSetOriginText() {
        if (hashtagsEnabled) hashtagManager.parseOriginText(originText)
        mentionManager.parseOriginText(originText)
        updateText(originText)
    }

    private fun updateText(text: CharSequence?) {
        val mentionText = mentionManager.spanMentions(text, hashtagTypeface)
        val hashtagText = if (hashtagsEnabled) hashtagManager.spanHashtags(mentionText, hashtagTypeface) else mentionText

        super.setText(hashtagText, this.bufferType)
        Linkify.addLinks(this, Linkify.WEB_URLS)
    }

    private fun init(attrs: AttributeSet?) {
        if (attrs != null) extractAttributes(attrs)
        setup()
    }

    private fun extractAttributes(attrs: AttributeSet) {
        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.ExpandableTextView, 0, 0)
        originMaxLines = typedArray.getInteger(R.styleable.ExpandableTextView_etv_maxLines, Int.MAX_VALUE)
        maxLines = originMaxLines
        expandEllipsis = typedArray.getString(R.styleable.ExpandableTextView_etv_expand_ellipsis)
                ?: getDefaultEllipsis().toString()
        collapseEllipsis = typedArray.getString(R.styleable.ExpandableTextView_etv_collapse_ellipsis)
        ellipsisColor = typedArray.getColor(R.styleable.ExpandableTextView_etv_ellipsisColor, getDefaultEllipsisColor())
        tapArea = if (typedArray.getInt(R.styleable.ExpandableTextView_etv_tap_area, 1) == 1) {
            TapArea.ELLIPSIS
        } else {
            TapArea.TEXT
        }
        hashtagsEnabled = typedArray.getBoolean(R.styleable.ExpandableTextView_etv_hashtagsEnabled, hashtagsEnabled)
        if (typedArray.hasValue(R.styleable.ExpandableTextView_etv_hashtagColor)) {
            val color = typedArray.getColor(R.styleable.ExpandableTextView_etv_hashtagColor, hashtagManager.hashtagColor)
            hashtagManager.hashtagColor = color
            mentionManager.mentionColor = color
        }
        hashtagTypeface = if (typedArray.getInt(R.styleable.ExpandableTextView_etv_hashtag_typeface, 1) == 1) {
            null
        } else {
            Typeface.DEFAULT_BOLD
        }
        ellipsisTypeface = if (typedArray.getInt(R.styleable.ExpandableTextView_etv_ellipsis_typeface, 1) == 1) {
            null
        } else {
            Typeface.DEFAULT_BOLD
        }

        canExpand = typedArray.getBoolean(R.styleable.ExpandableTextView_etv_can_expand, true)
        typedArray.recycle()
    }

    private fun setup() {
        expandEllipsisSpannable = SpannableString(expandEllipsis)
        setupExpandEllipsis(expandEllipsisSpannable)
        collapseEllipsisSpannable = if (collapseEllipsis != null) {
            SpannableString(collapseEllipsis)
        } else {
            null
        }
        collapseEllipsisSpannable?.let { setupCollapseEllipsis(it) }

        movementMethod = LinkHandler(false)
        super.setOnClickListener(this)
        super.setOnLongClickListener(this)
        initialized = true
        onSetOriginText()
    }

    private fun setupExpandEllipsis(ellipsis: Spannable) {
        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                if (canExpand) {
                    ellipsisClickTime = System.currentTimeMillis()
                    expand()
                }
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ellipsisColor
                ds.isUnderlineText = false
                if (ellipsisTypeface != null) {
                    ds.typeface = ellipsisTypeface
                }
            }
        }
        ellipsis.setSpan(clickableSpan, 0, ellipsis.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    private fun setupCollapseEllipsis(ellipsis: Spannable) {
        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                if (canExpand) {
                    ellipsisClickTime = System.currentTimeMillis()
                    collapse()
                }
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ellipsisColor
                ds.isUnderlineText = false
                if (ellipsisTypeface != null) {
                    ds.typeface = ellipsisTypeface
                }
            }
        }
        ellipsis.setSpan(clickableSpan, 0, ellipsis.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    private fun expand() {
        setExpanded(true)
        updateText(originText)
        onExpandListener?.invoke()
    }

    private fun collapse() {
        setExpanded(false)
        updateText(originText)
        onCollapseListener?.invoke()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val width = right - left

        val text = originText
        if (text == null) {
            canExpanded = false
            return
        }

        val availableWidth = with(calculateAvailableWidth(width)) {
            if (this < 0) 0.0f else this
        }

        canExpanded = if (isExpanded) {
            collapseEllipsisSpannable?.let { ellipsizeCollapsed(availableWidth, text) }
            false
        } else {
            ellipsize(availableWidth, text)
            true
        }
    }

    private fun calculateAvailableWidth(width: Int): Float {
        return with((width - paddingLeft - paddingRight).toFloat()) {
            if (this < 0) 0.0f else this
        }
    }

    private fun ellipsize(availableWidth: Float, text: CharSequence) {
        textMeasurementParamsBuilder.width(availableWidth.toInt()).textPaint(paint)
        if (hashtagsEnabled) hashtagManager.parseOriginText(originText)
        mentionManager.parseOriginText(originText)

        val mentionText = mentionManager.spanMentions(text, hashtagTypeface)
        val hashtagText = (if (hashtagsEnabled) hashtagManager.spanHashtags(mentionText, hashtagTypeface) else mentionText)
                ?: return
        val strings: List<CharSequence> =
                TextMeasurementUtils.getTextLines(hashtagText, textMeasurementParamsBuilder.build())
        if (strings.size <= maxLines) return

        spannableStringBuilder.clear()
        addPreEllipsisLines(spannableStringBuilder, strings)
        addEllipsisLine(spannableStringBuilder, strings, availableWidth)
        addAfterEllipsisLines(spannableStringBuilder, strings)

        updateText(spannableStringBuilder)
    }

    private fun ellipsizeCollapsed(availableWidth: Float, text: CharSequence) {
        textMeasurementParamsBuilder.width(availableWidth.toInt()).textPaint(paint)
        if (hashtagsEnabled) hashtagManager.parseOriginText(originText)
        mentionManager.parseOriginText(originText)

        val mentionText = mentionManager.spanMentions(text, hashtagTypeface)
        val hashtagText = (if (hashtagsEnabled) hashtagManager.spanHashtags(mentionText, hashtagTypeface) else mentionText)
                ?: return
        val strings: List<CharSequence> =
                TextMeasurementUtils.getTextLines(hashtagText, textMeasurementParamsBuilder.build())

        spannableStringBuilder.clear()
        for (i in 0..strings.size - 2) {
            spannableStringBuilder.append(strings[i])
            if (!strings[i].endsWith('\n')) spannableStringBuilder.append('\n')
        }

        val ellipsisLine = ellipsizeLine(strings[strings.size - 1], paint, availableWidth / 2)

        var defaultEllipsisStart = ellipsisLine.indexOf(getDefaultEllipsis())
        if (defaultEllipsisStart == -1) {
            defaultEllipsisStart = if (ellipsisLine.isEmpty()) 0 else ellipsisLine.length - 1
        }

        spannableStringBuilder.append(ellipsisLine, 0, defaultEllipsisStart)
        spannableStringBuilder.append(collapseEllipsisSpannable)
        updateText(spannableStringBuilder)
    }

    private fun addPreEllipsisLines(ssb: SpannableStringBuilder, lines: List<CharSequence>) {
        for (i in 0..maxLines - 2) {
            ssb.append(lines[i])
            if (!lines[i].endsWith('\n')) ssb.append('\n')
        }
    }

    private fun addEllipsisLine(ssb: SpannableStringBuilder, lines: List<CharSequence>, availableWidth: Float) {
        val index = maxLines - 1
        val ellipsisLine = ellipsizeLine(lines[index], paint, availableWidth / 2)

        var defaultEllipsisStart = ellipsisLine.indexOf(getDefaultEllipsis())
        if (defaultEllipsisStart == -1) {
            defaultEllipsisStart = if (ellipsisLine.isEmpty()) 0 else ellipsisLine.length - 1
        }

        ssb.append(ellipsisLine, 0, defaultEllipsisStart)
        ssb.append(expandEllipsisSpannable)

        if (index < lines.size - 1) ssb.append('\n')
    }

    private fun ellipsizeLine(text: CharSequence, paint: TextPaint, maxWidth: Float): CharSequence {
        return TextUtils.ellipsize(text, paint, maxWidth, TextUtils.TruncateAt.END)
    }

    private fun addAfterEllipsisLines(ssb: SpannableStringBuilder, lines: List<CharSequence>) {
        for (i in maxLines until lines.size) {
            ssb.append(lines[i])
            if (!lines[i].endsWith('\n') && i < lines.size - 1) ssb.append('\n')
        }
    }

    private fun getDefaultEllipsis(): Char {
        return Typography.ellipsis
    }

    private fun getDefaultEllipsisColor(): Int {
        return textColors.defaultColor
    }

    private var clickListener: OnClickListener? = null
    override fun setOnClickListener(l: OnClickListener?) {
        clickListener = l
    }

    override fun onClick(v: View?) {
        if (System.currentTimeMillis() - ellipsisClickTime <= 100) return
        if (hashtagsEnabled && hashtagManager.wasHashtagClick()) return
        if (mentionManager.wasMentionClick()) return
        if (tapArea == TapArea.TEXT && canExpanded && canExpand) {
            expand()
        } else {
            clickListener?.onClick(v)
        }
    }

    private var longClickListener: OnLongClickListener? = null
    override fun setOnLongClickListener(l: OnLongClickListener?) {
        longClickListener = l
    }

    override fun onLongClick(v: View?): Boolean {
        longClickListener?.onLongClick(v)
        return true
    }

    override fun scrollTo(x: Int, y: Int) {
        //do nothing
    }

    override fun onHashTagClick(hashtag: String) {
        onHashTagClickListener?.invoke(hashtag)
    }

    override fun onMentionClick(profileId: Long) {
        onMentionClickListener?.invoke(profileId)
    }
}