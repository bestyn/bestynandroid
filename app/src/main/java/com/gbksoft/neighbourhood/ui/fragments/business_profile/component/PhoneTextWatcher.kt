package com.gbksoft.neighbourhood.ui.fragments.business_profile.component

import android.text.Editable
import android.text.TextWatcher
import com.gbksoft.neighbourhood.utils.PhoneFormatter


class PhoneTextWatcher : TextWatcher {
    private var isRunning = false
    private var isDeleting = false
    private var enteredNumbers = mutableListOf<Char>()
    override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
        isDeleting = count > after
    }

    override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
        if (isRunning) {
            return
        }

        prepareEnteredNumbers(charSequence)
    }

    private fun prepareEnteredNumbers(charSequence: CharSequence) {
        enteredNumbers.clear()
        for (i in charSequence.indices) {
            val ch = charSequence[i]
            if (ch.isDigit()) enteredNumbers.add(ch)
        }
    }

    override fun afterTextChanged(editable: Editable) {
        if (isRunning || isDeleting) {
            return
        }
        isRunning = true

        val formatted = PhoneFormatter.format(enteredNumbers)
        editable.replace(0, editable.length, formatted, 0, formatted.length)

        isRunning = false
    }
}