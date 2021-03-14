package pl.essekkat

interface ShiftsRepo {
    fun listByWorker(workerId: WorkerId): List<Shift>
    fun addForWorker(workerId: WorkerId, shift: Shift)
}
