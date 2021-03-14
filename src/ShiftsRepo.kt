package pl.essekkat

interface ShiftsRepo {
    fun listByWorker(workerId: WorkerId): List<Shift>
    fun addForWorker(shift: Shift)
}

class ConflictingShift : Exception()
class TimeOffRequired : Exception()