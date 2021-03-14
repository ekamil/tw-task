package pl.essekkat

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(
    testing: Boolean = false,
    workersRepo: WorkersRepo = InMemoryWorkersRepo(),
    shiftsRepo: ShiftsRepo = InMemoryShiftsRepo()
) {

    install(ContentNegotiation) {
        jackson(contentType = ContentType.Application.Json) {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }

    routing {
        get("/") {
            call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
        }

        route("/v1") {
            route("/workers") {
                post {
                    val worker = call.receive<Worker>()
                    workersRepo.create(worker)
                    call.respond(HttpStatusCode.Created)
                }
                get {
                    val workers = workersRepo.list()
                        .map {
                            WorkerDTO(id = it.id, timeOff = it.mandatoryTimeOff.toString())
                        }
                    call.respond(workers)
                }
                get("/{workerId}") {
                    val worker = call.parameters["workerId"]
                        ?.let {
                            workersRepo.get(it)
                        }
                    if (worker?.isPresent == true) {
                        call.respond(worker.get())
                    } else {
                        call.respond(HttpStatusCode.NotFound)
                    }
                }
            }
        }
    }
}

