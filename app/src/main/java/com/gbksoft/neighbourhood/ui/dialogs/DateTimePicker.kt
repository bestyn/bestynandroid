package com.gbksoft.neighbourhood.ui.dialogs

import android.app.TimePickerDialog
import android.content.Context
import android.widget.TimePicker
import androidx.fragment.app.FragmentManager
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import java.util.*

class DateTimePicker(
    private val context: Context,
    private val mode: Mode = Mode.DATE_AND_TIME
) : TimePickerDialog.OnTimeSetListener {
    enum class Mode { DATE_AND_TIME, ONLY_DATE }

    private val utcCalendar = Calendar.getInstance().apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    private val calendar = Calendar.getInstance().apply {
        if (mode == Mode.ONLY_DATE) {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }
    var onDateTimePicked: ((Long) -> Unit)? = null
    private var hourOfDay = 0

    fun show(fragmentManager: FragmentManager, dateTime: Long?) {
        utcCalendar.timeInMillis = dateTime ?: System.currentTimeMillis()
        hourOfDay = utcCalendar.get(Calendar.HOUR_OF_DAY)
        utcCalendar.set(Calendar.HOUR_OF_DAY, 0)
        utcCalendar.set(Calendar.SECOND, 0)
        utcCalendar.set(Calendar.MILLISECOND, 0)

        val calendarConstraints = CalendarConstraints.Builder()
            .setOpenAt(utcCalendar.timeInMillis)
            .build()
        val datePickerDialog = MaterialDatePicker.Builder.datePicker()
            .setCalendarConstraints(calendarConstraints)
            .setSelection(utcCalendar.timeInMillis)
            .build()
        datePickerDialog.addOnPositiveButtonClickListener { selection: Long ->
            onDateSet(selection)
        }
        datePickerDialog.show(fragmentManager, "DatePickerDialog")
    }

    private fun onDateSet(selection: Long) {
        utcCalendar.timeInMillis = selection
        if (mode == Mode.DATE_AND_TIME) {
            utcCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            showTimeDialog()
        } else {
            onDateTimePicked()
        }
    }

    private fun showTimeDialog() {
        val timePickerDialog = TimePickerDialog(context, this,
            utcCalendar[Calendar.HOUR_OF_DAY],
            utcCalendar[Calendar.MINUTE], true)
        timePickerDialog.show()
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        utcCalendar[Calendar.HOUR_OF_DAY] = hourOfDay
        utcCalendar[Calendar.MINUTE] = minute
        onDateTimePicked()
    }

    private fun onDateTimePicked() {
        calendar.set(Calendar.YEAR, utcCalendar.get(Calendar.YEAR))
        calendar.set(Calendar.DAY_OF_YEAR, utcCalendar.get(Calendar.DAY_OF_YEAR))
        calendar.set(Calendar.HOUR_OF_DAY, utcCalendar.get(Calendar.HOUR_OF_DAY))
        calendar.set(Calendar.MINUTE, utcCalendar.get(Calendar.MINUTE))
        calendar.set(Calendar.SECOND, utcCalendar.get(Calendar.SECOND))
        onDateTimePicked?.invoke(calendar.timeInMillis)
    }
}