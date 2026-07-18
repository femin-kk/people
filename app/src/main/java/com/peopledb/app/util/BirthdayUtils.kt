package com.peopledb.app.util

import java.time.LocalDate
import java.time.MonthDay
import java.time.format.DateTimeFormatter

object BirthdayUtils {

    private val displayFormatterWithYear = DateTimeFormatter.ofPattern("MMM d, yyyy")
    private val displayFormatterNoYear = DateTimeFormatter.ofPattern("MMM d")

    fun format(epochDay: Long?, yearKnown: Boolean): String? {
        if (epochDay == null) return null
        val date = LocalDate.ofEpochDay(epochDay)
        return if (yearKnown) date.format(displayFormatterWithYear) else date.format(displayFormatterNoYear)
    }

    /** Returns age in years, or null if birthday unknown or year unknown. */
    fun age(epochDay: Long?, yearKnown: Boolean, today: LocalDate = LocalDate.now()): Int? {
        if (epochDay == null || !yearKnown) return null
        val date = LocalDate.ofEpochDay(epochDay)
        var age = today.year - date.year
        if (today.monthValue < date.monthValue || (today.monthValue == date.monthValue && today.dayOfMonth < date.dayOfMonth)) {
            age--
        }
        return age
    }

    /** Days until the next occurrence of this birthday (0 = today). Null if unknown. */
    fun daysUntilNextBirthday(epochDay: Long?, today: LocalDate = LocalDate.now()): Long? {
        if (epochDay == null) return null
        val date = LocalDate.ofEpochDay(epochDay)
        val monthDay = MonthDay.of(date.month, minOf(date.dayOfMonth, date.month.length(today.isLeapYear)))
        var next = monthDay.atYear(today.year)
        if (next.isBefore(today)) {
            next = monthDay.atYear(today.year + 1)
        }
        return java.time.temporal.ChronoUnit.DAYS.between(today, next)
    }
}
