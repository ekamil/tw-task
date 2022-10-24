package tw.task

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.*
import io.ktor.server.testing.*
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Before
import tw.task.shifts.InMemoryShiftsRepo
import tw.task.shifts.Shift
import tw.task.shifts.ShiftsRepo
import tw.task.web.WorkerWithShiftsDTO
import tw.task.workers.Worker
import tw.task.workers.WorkersRepo
import java.time.Duration
import java.time.LocalDateTime
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ShiftsTest {
    @MockK
    lateinit var workersRepo: WorkersRepo

    @MockK
    lateinit var shiftsRepo: ShiftsRepo

    @Before
    fun setUp() = MockKAnnotations.init(this)

    @Test
    fun `can set shift`() {
        val workerId = "worker1"
        every { workersRepo.get(workerId) }.answers { Optional.of(Worker(id = workerId)) }
        shiftsRepo = InMemoryShiftsRepo()
        withTestApplication({
            module(
                workersRepo = workersRepo,
                shiftsRepo = shiftsRepo
            )
        }) {
            handleRequest(HttpMethod.Post, "/v1/workers/$workerId/shifts") {
                setBody(
                    """
  {
    "start": "2021-03-10T12:00:00",
    "end": "2021-03-10T16:00:00"
  }
"""
                )
                addHeader("Content-Type", ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.NoContent, response.status())
            }
        }
    }

    @Test
    fun `can set and get shift`() {
        val workerId = "worker1"
        every { workersRepo.get(workerId) }.returns(
            Optional.of(
                Worker(
                    id = workerId,
                )
            )
        )
        shiftsRepo = InMemoryShiftsRepo()
        withTestApplication({
            module(
                workersRepo = workersRepo,
                shiftsRepo = shiftsRepo
            )
        }) {
            handleRequest(HttpMethod.Post, "/v1/workers/$workerId/shifts") {
                setBody(
                    """
{
"start": "2021-03-10T12:00:00.000",
"end": "2021-03-10T16:00:00.000"
}
"""
                )
                addHeader("Content-Type", ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.NoContent, response.status())
            }
            handleRequest(HttpMethod.Get, "/v1/workers/$workerId/shifts") {}.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                val mapper: ObjectMapper = jacksonObjectMapper()
                val wws: WorkerWithShiftsDTO = mapper.readValue(response.content ?: "[]")
                assertEquals(1, wws.shifts.size)
            }
        }
    }

    @Test
    fun `can't set shifts on non-existent worker`() {
        val workerId = "worker1"
        every { workersRepo.get(workerId) }.returns(Optional.empty())
        withTestApplication({
            module(
                workersRepo = workersRepo,
                shiftsRepo = shiftsRepo
            )
        }) {
            handleRequest(HttpMethod.Post, "/v1/workers/$workerId/shifts") {
                setBody(
                    """[
  {
    "start": "2021-03-10T12:00:00",
    "end": "2021-03-10T16:00:00"
  }
]
"""
                )
                addHeader("Content-Type", ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.NotFound, response.status())
            }
        }
    }

    @Test
    fun `can't set consecutive shift`() {
        val workerId = "worker1"
        every { workersRepo.get(workerId) }.returns(
            Optional.of(
                Worker(
                    id = workerId,
                    mandatoryTimeOff = Duration.ofHours(2)
                )
            )
        )
        every { shiftsRepo.listByWorker(workerId) } answers {
            listOf(
                Shift(
                    start = LocalDateTime.parse("2021-03-10T12:00:00"),
                    end = LocalDateTime.parse("2021-03-10T16:00:00")
                )
            )
        }
        withTestApplication({
            module(
                workersRepo = workersRepo,
                shiftsRepo = shiftsRepo
            )
        }) {
            handleRequest(HttpMethod.Post, "/v1/workers/$workerId/shifts") {
                setBody(
                    """
  {
    "start": "2021-03-10T17:00:00",
    "end": "2021-03-10T21:00:00"
  }
"""
                )
                addHeader("Content-Type", ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.Conflict, response.status())
            }
        }
    }

    @Test
    fun `can't set overlapping shifts`() {
        val workerId = "worker1"
        every { workersRepo.get(workerId) }.returns(
            Optional.of(
                Worker(
                    id = workerId,
                )
            )
        )
        every { shiftsRepo.listByWorker(workerId) } answers {
            listOf(
                Shift(
                    start = LocalDateTime.parse("2021-03-10T12:00:00"),
                    end = LocalDateTime.parse("2021-03-10T16:00:00")
                )
            )
        }
        withTestApplication({
            module(
                workersRepo = workersRepo,
                shiftsRepo = shiftsRepo
            )
        }) {
            handleRequest(HttpMethod.Post, "/v1/workers/$workerId/shifts") {
                setBody(
                    """
  {
    "start": "2021-03-10T15:00:00",
    "end": "2021-03-10T19:00:00"
  }
"""
                )
                addHeader("Content-Type", ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.Conflict, response.status())
            }
        }
    }

    @Test
    fun `shift has to move forward in time`() {
        assertFailsWith<AssertionError>(
            message = "Shift end has to be after shift's start"
        ) {
            Shift(
                start = LocalDateTime.parse("2021-03-11T12:00:00"),
                end = LocalDateTime.parse("2021-03-10T16:00:00")
            )
        }
    }
}
