package pl.essekkat

import java.time.ZonedDateTime

data class Shift(
    val workerId: WorkerId,
    val start: ZonedDateTime,
    val end: ZonedDateTime,
    val isWorking: Boolean = true
)
