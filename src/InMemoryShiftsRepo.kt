package pl.essekkat

class InMemoryShiftsRepo : ShiftsRepo {
    private val store = mutableListOf<Shift>()

    override fun listByWorker(workerId: WorkerId): List<Shift> =
        store.filter { it.workerId == workerId }

    override fun addForWorker(shift: Shift) {
        store.add(shift)
    }

}