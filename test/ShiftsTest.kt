package pl.essekkat

import io.ktor.http.*
import io.ktor.server.testing.*
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Before
import java.time.Duration
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ShiftsTest {
    @MockK
    lateinit var workersRepo: WorkersRepo

    @Before
    fun setUp() = MockKAnnotations.init(this)

    @Test
    fun `can't set shifts on non-existent worker`() {
        val workerId = "worker1"
        every { workersRepo.get(workerId) }.returns(Optional.empty())
        withTestApplication({ module(testing = true, workersRepo = workersRepo) }) {
            handleRequest(HttpMethod.Post, "/v1/workers/$workerId/shifts") {
                setBody(
                    """[
  {
    "start": "2021-03-10T12:00:00+00",
    "end": "2021-03-10T16:00:00+00"
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
    fun `can set shifts`() {
        val workerId = "worker1"
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Post, "/v1/workers/$workerId/shifts") {
                setBody(
                    """[
  {
    "start": "2021-03-10T12:00:00+00",
    "end": "2021-03-10T16:00:00+00"
  },
  {
    "start": "2021-03-11T12:00:00+00",
    "end": "2021-03-11T16:00:00+00"
  }
]
"""
                )
                addHeader("Content-Type", ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

    @Test
    fun `cant set consecutive shifts`() {
        val workerId = "worker1"
        every { workersRepo.get(workerId) }.returns(
            Optional.of(
                Worker(
                    id = workerId,
                    mandatoryTimeOff = Duration.ofHours(2)
                )
            )
        )
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Post, "/v1/workers/$workerId/shifts") {
                setBody(
                    """[
  {
    "start": "2021-03-10T12:00:00+00",
    "end": "2021-03-10T16:00:00+00"
  },
  {
    "start": "2021-03-10T17:00:00+00",
    "end": "2021-03-10T21:00:00+00"
  }
]
"""
                )
                addHeader("Content-Type", ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.Conflict, response.status())
            }
        }
    }

    @Test
    fun `cant set overlapping shifts`() {
        val workerId = "worker1"
        every { workersRepo.get(workerId) }.returns(
            Optional.of(
                Worker(
                    id = workerId,
                )
            )
        )
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Post, "/v1/workers/$workerId/shifts") {
                setBody(
                    """[
  {
    "start": "2021-03-10T12:00:00+00",
    "end": "2021-03-10T16:00:00+00"
  },
  {
    "start": "2021-03-10T15:00:00+00",
    "end": "2021-03-10T19:00:00+00"
  }
]
"""
                )
                addHeader("Content-Type", ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.Conflict, response.status())
            }
        }
    }
}
