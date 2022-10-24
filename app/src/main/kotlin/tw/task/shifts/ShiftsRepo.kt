package tw.task.shifts

import tw.task.workers.WorkerId

interface ShiftsRepo {
    fun listByWorker(workerId: WorkerId): List<Shift>
    fun addForWorker(workerId: WorkerId, shift: Shift)
}
