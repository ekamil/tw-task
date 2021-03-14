package pl.essekkat

import pl.essekkat.shifts.Shift
import pl.essekkat.shifts.ShiftsRepo
import pl.essekkat.workers.Worker
import pl.essekkat.workers.WorkersRepo

class Service(private val workersRepo: WorkersRepo, private val shiftsRepo: ShiftsRepo) {
    fun addNewShift(worker: Worker, shift: Shift) {
        val shifts: List<Shift> = shiftsRepo.listByWorker(worker.id)
        // TODO: checks
        shiftsRepo.addForWorker(worker.id, shift)
    }
}

sealed class SchedulingError : Exception()
class TimeOffRequired : SchedulingError()
class Overlap : SchedulingError()