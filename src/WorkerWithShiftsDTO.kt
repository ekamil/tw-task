package pl.essekkat

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.ZonedDateTime

data class WorkerDTO(
    val id: String,
    @JsonProperty("time_off")
    val timeOff: String
)

data class ShiftDTO(val start: ZonedDateTime, val end: ZonedDateTime)

data class WorkerWithShiftsDTO(
    val id: String,
    val shifts: List<ShiftDTO>
)