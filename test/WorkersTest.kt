package pl.essekkat

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.*
import io.ktor.server.testing.*
import io.mockk.every
import io.mockk.mockk
import pl.essekkat.workers.Worker
import pl.essekkat.workers.WorkersRepo
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class WorkersTest {
    @Test
    fun `created worker can be retrieved`() {
        val workerId = "worker1"
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Post, "/v1/workers") {
                setBody(
                    "{\n  \"id\": \"$workerId\"\n}"
                )
                addHeader("Content-Type", ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.Created, response.status())
            }
            handleRequest(HttpMethod.Get, "/v1/workers/$workerId").apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }
            handleRequest(HttpMethod.Get, "/v1/workers/non-existent-worker").apply {
                assertEquals(HttpStatusCode.NotFound, response.status())
            }
        }
    }

    @Test
    fun `create method is idempotent`() {
        val workerId = "worker1"
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Post, "/v1/workers") {
                setBody(
                    "{\n  \"id\": \"$workerId\"\n}"
                )
                addHeader("Content-Type", ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.Created, response.status())
            }
            handleRequest(HttpMethod.Post, "/v1/workers") {
                setBody(
                    "{\n  \"id\": \"$workerId\"\n}"
                )
                addHeader("Content-Type", ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.Created, response.status())
            }
        }
    }

    @Test
    fun `non existent worker isn't returned`() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/v1/workers/non-existent-worker").apply {
                assertEquals(HttpStatusCode.NotFound, response.status())
            }
        }
    }

    @Test
    fun `can list workers (empty)`() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/v1/workers").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("[ ]", response.content)
            }
        }
    }

    @Test
    fun `can list workers`() {
        val workersRepo = mockk<WorkersRepo>()
        every { workersRepo.list() }.answers {
            listOf(
                Worker(id = "worker-1"),
                Worker(id = "worker-2", mandatoryTimeOff = Duration.ofHours(24)),
            )
        }
        withTestApplication({ module(testing = true, workersRepo = workersRepo) }) {
            handleRequest(HttpMethod.Get, "/v1/workers").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertNotEquals("[ ]", response.content)
                val mapper = jacksonObjectMapper()
                val workers: List<WorkerDTO> = mapper.readValue(response.content ?: "[]")
                assertEquals(2, workers.size)
                assertEquals("PT12H", workers[0].timeOff)
                assertEquals("PT24H", workers[1].timeOff)
            }
        }
    }
}
