package com.racobos.manhattan.entity

import com.racobos.manhattan.datasource.Entity
import java.text.SimpleDateFormat
import java.util.*

val DEFAULT_ID = "Unknown"
val DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ"

fun Calendar.formatToAppString(): String {
    val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
    dateFormat.timeZone = this.timeZone
    return dateFormat.format(this.time)
}

fun String.parseToCalendar(): Calendar {
    val timeZoneId = "GMT${this.substring(19, 22)}:${this.substring(22)}"
    val tZ = TimeZone.getTimeZone(timeZoneId)
    val cal = Calendar.getInstance()
    cal.time = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).apply { timeZone = tZ }.parse(this)
    cal.timeZone = tZ
    return cal
}

data class UserEntity(var name: String? = null) : Entity()