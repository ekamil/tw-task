package pl.essekkat.shifts

import java.time.LocalDateTime

data class Shift(
    val start: LocalDateTime,
    val end: LocalDateTime,
)
