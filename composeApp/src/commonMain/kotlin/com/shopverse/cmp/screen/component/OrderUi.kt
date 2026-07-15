package com.shopverse.cmp.screen.component

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime

/** Order bits shared by the history list and the detail screen (Android parity). */

private const val ORDER_ID_VISIBLE_CHARS = 8

/** "#9B3E12AB" — the first 8 chars of the UUID, uppercased, like Android's cells. */
fun orderNumberLabel(orderId: String): String =
    "#${orderId.take(ORDER_ID_VISIBLE_CHARS).uppercase()}"

private val dateFormat = LocalDateTime.Format {
    monthName(MonthNames.ENGLISH_ABBREVIATED)
    char(' ')
    dayOfMonth(padding = Padding.NONE)
    chars(", ")
    year()
}

private val dateTimeFormat = LocalDateTime.Format {
    monthName(MonthNames.ENGLISH_ABBREVIATED)
    char(' ')
    dayOfMonth(padding = Padding.NONE)
    chars(", ")
    year()
    chars(" · ")
    hour()
    char(':')
    minute()
}

/** "May 16, 2026" (or "May 16, 2026 · 11:30"); falls back to the raw date part on parse failure. */
fun orderDateLabel(placedAt: String, withTime: Boolean = false): String = runCatching {
    val local = Instant.parse(placedAt).toLocalDateTime(TimeZone.currentSystemDefault())
    if (withTime) dateTimeFormat.format(local) else dateFormat.format(local)
}.getOrDefault(placedAt.substringBefore('T'))

/** "139.97 USD" / "60 USD" — same rendering as the Android order cells. */
fun orderPriceLabel(value: Double, currency: String): String = "${priceLabel(value)} $currency"
