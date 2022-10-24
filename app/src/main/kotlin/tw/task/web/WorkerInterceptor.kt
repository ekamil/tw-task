package tw.task.web

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import tw.task.workers.Worker
import tw.task.workers.WorkersRepo

val WorkerAttribute = AttributeKey<Worker>("Worker")

fun Route.workerInterceptor(workersRepo: WorkersRepo) {
    intercept(ApplicationCallPipeline.Call) {
        val workerId = call.parameters["workerId"]
        if (workerId.isNullOrEmpty()) {
            call.respond(HttpStatusCode.BadRequest)
            return@intercept finish()
        }
        val worker = workerId.let { id ->
            workersRepo.get(id)
        }
        if (worker.isPresent) {
            call.attributes.put(WorkerAttribute, worker.get())
        } else {
            call.respond(HttpStatusCode.NotFound)
            return@intercept finish()
        }
    }
}