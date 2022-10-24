package tw.task.workers

import java.time.Duration


typealias WorkerId = String

data class Worker(
    val id: String,
    var mandatoryTimeOff: Duration = Duration.ofHours(12)
)
