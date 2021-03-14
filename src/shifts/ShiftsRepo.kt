package pl.essekkat.shifts

import pl.essekkat.workers.WorkerId

interface ShiftsRepo {
    fun listByWorker(workerId: WorkerId): List<Shift>
    fun addForWorker(workerId: WorkerId, shift: Shift)
}
