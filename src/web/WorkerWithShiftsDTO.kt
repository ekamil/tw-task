package pl.essekkat

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import java.time.LocalDateTime


data class WorkerDTO(
    val id: String,
    @JsonProperty("time_off")
    val timeOff: String
)

data class ShiftDTO(
    @JsonDeserialize(using = LocalDateTimeDeserializer::class)
    val start: LocalDateTime,
    @JsonDeserialize(using = LocalDateTimeDeserializer::class)
    val end: LocalDateTime
)

data class WorkerWithShiftsDTO(
    val id: String,
    val shifts: List<ShiftDTO>
)