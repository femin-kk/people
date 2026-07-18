package com.peopledb.app.util

import java.time.Instant
import java.time.Period
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object RelativeTime {

    /**
     * Formats [epochMillis] as a human relative string like:
     * "just now", "5 minutes ago", "3 hours ago", "yesterday",
     * "4 days ago", "2 months ago", "1 year and 4 months ago".
     */
    fun format(epochMillis: Long, now: Long = System.currentTimeMillis()): String {
        val diffMs = now - epochMillis
        if (diffMs < 0) return "just now"

        val seconds = diffMs / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            seconds < 60 -> "just now"
            minutes < 60 -> pluralize(minutes, "minute")
            hours < 24 -> pluralize(hours, "hour")
            days == 1L -> "yesterday"
            days < 30 -> pluralize(days, "day")
            else -> {
                val zone = ZoneId.systemDefault()
                val start = Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate()
                val end = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
                val period = Period.between(start, end)
                val totalMonths = period.years * 12 + period.months

                when {
                    period.years >= 1 && period.months > 0 ->
                        "${pluralize(period.years.toLong(), "year")} and ${pluralize(period.months.toLong(), "month")} ago"
                    period.years >= 1 -> pluralize(period.years.toLong(), "year")
                    totalMonths >= 1 -> pluralize(totalMonths.toLong(), "month")
                    else -> pluralize(days, "day")
                }
            }
        }
    }

    private fun pluralize(value: Long, unit: String): String {
        val word = if (value == 1L) unit else "${unit}s"
        return "$value $word ago"
    }

    private val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
    private val dateFormatterNoYear = DateTimeFormatter.ofPattern("MMM d")

    fun formatAbsoluteDate(epochMillis: Long): String {
        val zone = ZoneId.systemDefault()
        val date = Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate()
        return date.format(dateFormatter)
    }
}
