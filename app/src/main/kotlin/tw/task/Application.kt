package tw.task

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import tw.task.shifts.InMemoryShiftsRepo
import tw.task.shifts.Shift
import tw.task.shifts.ShiftsRepo
import tw.task.web.*
import tw.task.workers.InMemoryWorkersRepo
import tw.task.workers.Worker
import tw.task.workers.WorkersRepo


fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@JvmOverloads
fun Application.module(
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
                    workerInterceptor(workersRepo)
                    get {
                        call.respond(call.attributes[WorkerAttribute])
                    }
                    route("/shifts") {
                        get {
                            val worker = call.attributes[WorkerAttribute]
                            val workerId = worker.id
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
                            val worker = call.attributes[WorkerAttribute]
                            val newShift: Shift = call.receive<ShiftDTO>()
                                .let { Shift(start = it.start, end = it.end) }
                            try {
                                service.addNewShift(worker, newShift)
                                call.respond(HttpStatusCode.NoContent)
                            } catch (e: SchedulingError) {
                                call.respond(HttpStatusCode.Conflict, e.message.orEmpty())
                            }
                        }
                    }
                }
            }
        }
    }
}

