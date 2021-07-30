package com.gbksoft.neighbourhood.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.text.format.DateFormat
import com.gbksoft.neighbourhood.app.NApplication
import java.text.SimpleDateFormat
import java.util.*

object DateTimeUtils : BroadcastReceiver() {
    val localeChangedIntentFilter = IntentFilter().apply {
        addAction(Intent.ACTION_LOCALE_CHANGED)
        addAction(Intent.ACTION_TIME_CHANGED)
    }

    private val context = NApplication.context
    private val calendar = Calendar.getInstance()
    private const val DATE_TIME_STRINGS_FORMAT = "%s, %s"
    private val TEMP_FILE_DATE_FORMAT = SimpleDateFormat("yyyy_MM_dd_HH-mm-ss-SSS", Locale.US)

    private lateinit var PROFILE_DATE_OF_BIRTH_FORMAT: SimpleDateFormat
    private lateinit var DATE_FORMAT: SimpleDateFormat
    private lateinit var DATE_WITHOUT_YEAR_FORMAT: SimpleDateFormat
    private lateinit var TIME_FORMAT: SimpleDateFormat
    private lateinit var DATE_TIME_FORMAT: SimpleDateFormat
    private lateinit var DATE_TIME_WITHOUT_YEAR_FORMAT: SimpleDateFormat
    private lateinit var POST_PUBLISH_TIME_FORMAT: SimpleDateFormat
    private lateinit var POST_PUBLISH_TIME_WITHOUT_YEAR_FORMAT: SimpleDateFormat

    private var isCurrent24HourFormat: Boolean

    val timeForTempFile: String
        get() = TEMP_FILE_DATE_FORMAT.format(Date())

    init {
        isCurrent24HourFormat = DateFormat.is24HourFormat(context)
        initSimpleDateFormats(isCurrent24HourFormat)
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_LOCALE_CHANGED -> initSimpleDateFormats(isCurrent24HourFormat)
            Intent.ACTION_TIME_CHANGED -> {
                val is24Hour = DateFormat.is24HourFormat(DateTimeUtils.context)
                if (isCurrent24HourFormat != is24Hour) {
                    isCurrent24HourFormat = is24Hour
                    initSimpleDateFormats(is24Hour)
                }
            }
        }
    }

    private fun initSimpleDateFormats(is24Hour: Boolean) {
        val locale = Locale.getDefault()
        val time = if (is24Hour) "HH:mm" else "hh:mm a"
        PROFILE_DATE_OF_BIRTH_FORMAT = SimpleDateFormat("MMM d, yyyy", locale).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        DATE_FORMAT = SimpleDateFormat("MMM dd, yyyy", locale)
        DATE_WITHOUT_YEAR_FORMAT = SimpleDateFormat("MMM dd", locale)
        TIME_FORMAT = SimpleDateFormat(time, locale)
        DATE_TIME_FORMAT = SimpleDateFormat("MMM dd, yyyy, $time", locale)
        DATE_TIME_WITHOUT_YEAR_FORMAT = SimpleDateFormat("MMM dd, $time", locale)
        POST_PUBLISH_TIME_FORMAT = SimpleDateFormat("MMM dd, yyyy 'at' $time", locale)
        POST_PUBLISH_TIME_WITHOUT_YEAR_FORMAT = SimpleDateFormat("MMM dd 'at' $time", locale)
    }

    @JvmStatic
    fun formatProfileDateOfBirth(timeInMillis: Long): String {
        return PROFILE_DATE_OF_BIRTH_FORMAT.format(Date(timeInMillis))
    }

    fun getPostPublishTime(millis: Long): String {
        val date = Date()
        date.time = millis
        calendar.timeInMillis = millis
        val year = calendar[Calendar.YEAR]
        calendar.timeInMillis = System.currentTimeMillis()
        val currentYear = calendar[Calendar.YEAR]
        return if (year == currentYear) {
            POST_PUBLISH_TIME_WITHOUT_YEAR_FORMAT.format(date)
        } else {
            POST_PUBLISH_TIME_FORMAT.format(date)
        }
    }

    @JvmStatic
    fun getDateTime(millis: Long): String {
        val date = Date()
        date.time = millis
        calendar.timeInMillis = millis
        val year = calendar[Calendar.YEAR]
        calendar.timeInMillis = System.currentTimeMillis()
        val currentYear = calendar[Calendar.YEAR]
        return if (year == currentYear) {
            DATE_TIME_WITHOUT_YEAR_FORMAT.format(date)
        } else {
            DATE_TIME_FORMAT.format(date)
        }
    }

    fun formatChatDate(millis: Long): String {
        val date = Date()
        date.time = millis
        calendar.timeInMillis = millis
        val year = calendar[Calendar.YEAR]
        calendar.timeInMillis = System.currentTimeMillis()
        val currentYear = calendar[Calendar.YEAR]
        return if (year == currentYear) {
            DATE_WITHOUT_YEAR_FORMAT.format(date)
        } else {
            DATE_FORMAT.format(date)
        }
    }

    fun formatConversationTime(calendar: Calendar, millis: Long): String {
        val date = Date()
        date.time = millis
        calendar.timeInMillis = millis
        val year = calendar[Calendar.YEAR]
        calendar.timeInMillis = System.currentTimeMillis()
        val currentYear = calendar[Calendar.YEAR]
        var formattedDate: String
        if (year == currentYear) {
            formattedDate = DATE_WITHOUT_YEAR_FORMAT.format(date)
            val formattedToday = DATE_WITHOUT_YEAR_FORMAT.format(calendar.time)
            if (formattedDate == formattedToday) formattedDate = ""
        } else {
            formattedDate = DATE_FORMAT.format(date)
        }
        val formattedTime = TIME_FORMAT.format(date)
        return if (formattedDate.isEmpty()) {
            formattedTime
        } else {
            "$formattedDate, $formattedTime"
        }
    }

    fun formatChatDate(calendar: Calendar, millis: Long, todayTitle: String, yesterdayTitle: String): String {
        val date = Date()
        date.time = millis
        calendar.timeInMillis = millis
        val year = calendar[Calendar.YEAR]
        calendar.timeInMillis = System.currentTimeMillis()
        val currentYear = calendar[Calendar.YEAR]
        return if (year == currentYear) {
            val formattedDate = DATE_WITHOUT_YEAR_FORMAT.format(date)
            val formattedToday = DATE_WITHOUT_YEAR_FORMAT.format(calendar.time)
            if (formattedDate == formattedToday) return todayTitle
            calendar.add(Calendar.DATE, -1)
            val formattedYesterday = DATE_WITHOUT_YEAR_FORMAT.format(calendar.time)
            if (formattedDate == formattedYesterday) yesterdayTitle else formattedDate
        } else {
            DATE_FORMAT.format(date)
        }
    }

    fun getTime(millis: Long): String {
        val date = Date()
        date.time = millis
        return TIME_FORMAT.format(date)
    }

    fun formatDateTime(date: String?, time: String?): String {
        return String.format(Locale.US, DATE_TIME_STRINGS_FORMAT, date, time)
    }
}