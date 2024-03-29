package tw.task.shifts

import tw.task.workers.WorkerId

class InMemoryShiftsRepo : ShiftsRepo {
    private val store = mutableMapOf<WorkerId, MutableList<Shift>>()

    override fun listByWorker(workerId: WorkerId): List<Shift> {
        return store[workerId] ?: emptyList()
    }

    override fun addForWorker(workerId: WorkerId, shift: Shift) {
        store[workerId]?.add(shift)
            ?: run {
                store[workerId] = mutableListOf(shift)
            }
    }

}
