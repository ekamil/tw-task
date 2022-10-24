package tw.task.shifts

import tw.task.workers.WorkerId

class InMemoryShiftsRepo : ShiftsRepo {
    private val store = mutableMapOf<WorkerId, MutableList<Shift>>()

    override fun listByWorker(workerId: WorkerId): List<Shift> {
        return store[workerId] ?: emptyList()
    }

    override fun addForWorker(workerId: WorkerId, shift: Shift) {
        if (workerId in store) {
            store[workerId]
                ?.apply { this.add(shift) }
        } else {
            store[workerId] = mutableListOf(shift)
        }
    }

}