package tw.task.workers

import java.util.*

class InMemoryWorkersRepo : WorkersRepo {
    private val store: MutableMap<WorkerId, Worker> = mutableMapOf()
    override fun create(worker: Worker) {
        assert(worker.id.isNotEmpty()) { "Worker ID can't be empty" }
        assert(worker.id.isNotBlank()) { "Worker ID can't be blank" }
        if (!store.containsKey(worker.id)) {
            store[worker.id] = worker
        }
    }

    override fun list(): List<Worker> = store.values.toList()

    override fun get(id: WorkerId): Optional<Worker> = Optional.ofNullable(store[id])

}
