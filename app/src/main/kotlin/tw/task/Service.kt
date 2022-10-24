package tw.task

import tw.task.shifts.Shift
import tw.task.shifts.ShiftsRepo
import tw.task.workers.Worker
import tw.task.workers.WorkersRepo
import java.time.Duration

class Service(private val workersRepo: WorkersRepo, private val shiftsRepo: ShiftsRepo) {
    fun addNewShift(worker: Worker, newShift: Shift) {
        val shifts: List<Shift> = shiftsRepo.listByWorker(worker.id)
            .sortedBy(Shift::start)
        for (shift in shifts) {
            val (start, end) = shift
            val (newStart, newEnd) = newShift
            val doesOverlap = !(newStart.isAfter(end) || newEnd.isBefore(start))
            if (doesOverlap) throw Overlap()
            val noTimeOff = if (newStart.isAfter(end)) {
                // appending a shift{
                Duration.between(end, newStart) < worker.mandatoryTimeOff
            } else {
                // prepending a shift
                Duration.between(newEnd, start) < worker.mandatoryTimeOff
            }
            if (noTimeOff) throw TimeOffRequired()
        }
        shiftsRepo.addForWorker(worker.id, newShift)
    }
}

sealed class SchedulingError(override val message: String?) : Exception(message)
class TimeOffRequired : SchedulingError("Overlap detected")
class Overlap : SchedulingError("Too little time between shifts")