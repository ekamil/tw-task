package pl.essekkat

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import pl.essekkat.shifts.InMemoryShiftsRepo
import pl.essekkat.shifts.Shift
import pl.essekkat.shifts.ShiftsRepo
import pl.essekkat.workers.InMemoryWorkersRepo
import pl.essekkat.workers.Worker
import pl.essekkat.workers.WorkersRepo
import java.util.*


fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(
    testing: Boolean = false,
    workersRepo: WorkersRepo = InMemoryWorkersRepo(),
    shiftsRepo: ShiftsRepo = InMemoryShiftsRepo()
) {
    val service = Service(workersRepo, shiftsRepo)

    install(ContentNegotiation) {
        jackson(contentType = ContentType.Application.Json) {
            enable(SerializationFeature.INDENT_OUTPUT)
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            registerModule(JavaTimeModule())
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
                route("/{workerId}") {
                    get {
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
                    route("/shifts") {
                        get {
                            val workerId = call.parameters["workerId"]
                            if (workerId.isNullOrEmpty()) {
                                call.respond(HttpStatusCode.BadRequest)
                                return@get
                            }
                            val worker = workersRepo.get(workerId)
                            if (!worker.isPresent) {
                                call.respond(HttpStatusCode.NotFound)
                                return@get
                            }
                            val shifts: List<ShiftDTO> = shiftsRepo.listByWorker(workerId)
                                .map {
                                    ShiftDTO(
                                        start = it.start,
                                        end = it.end
                                    )
                                }.toList()
                            call.respond(
                                WorkerWithShiftsDTO(
                                    id = workerId,
                                    shifts = shifts
                                )
                            )
                        }
                        post {
                            val workerId = call.parameters["workerId"]
                            if (workerId.isNullOrEmpty()) {
                                call.respond(HttpStatusCode.BadRequest)
                                return@post
                            }
                            val worker = workersRepo.get(workerId)
                            if (!worker.isPresent) {
                                call.respond(HttpStatusCode.NotFound)
                                return@post
                            }
                            val newShift: Shift = call.receive<ShiftDTO>()
                                .let { Shift(start = it.start, end = it.end) }
                            service.addNewShift(worker.get(), newShift)
                            call.respond(HttpStatusCode.NoContent)
                        }
                    }
                }
            }
        }
    }
}

