package pl.essekkat.web

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import pl.essekkat.workers.Worker
import pl.essekkat.workers.WorkersRepo

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