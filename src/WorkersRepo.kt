package pl.essekkat

import java.util.*

interface WorkersRepo {
    fun create(worker: Worker)
    fun list(): List<Worker>
    fun get(id: WorkerId): Optional<Worker>
}