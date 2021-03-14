package pl.essekkat

import java.time.Duration


typealias WorkerId = String

data class Worker(
    val id: String,
    var mandatoryTimeOff: Duration = Duration.ofHours(12)
)
