package pl.essekkat.shifts

import java.time.LocalDateTime

data class Shift(
    val start: LocalDateTime,
    val end: LocalDateTime,
){
    init {
        assert(end.isAfter(start)) {"Shift end has to be after shift's start"}
    }
}
