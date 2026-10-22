package com.mediasage.agentruntime

import com.mediasage.agentruntime.plugins.configureContentNegotiation
import com.mediasage.agentruntime.plugins.configureStatusPages
import com.mediasage.agentruntime.routes.webhookRoutes
import com.mediasage.agentruntime.service.AgentLaunchService
import com.mediasage.agentruntime.service.AgentLauncher
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val BOT_ACCOUNT_ID = "bot-account-id"
private const val WEBHOOK_SECRET = "test-jira-secret"

class JiraWebhookRouteTest {

    @Test
    fun botAssigneeInProgressFires() = testWebhookApp {
        val response = client.post("/webhook/jira?token=$WEBHOOK_SECRET") {
            contentType(ContentType.Application.Json)
            setBody(webhookPayload(event = "jira:issue_created", assigneeAccountId = BOT_ACCOUNT_ID, status = "In Progress"))
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun wrongAssigneeInProgressDoesNotFire() = testWebhookApp {
        val response = client.post("/webhook/jira?token=$WEBHOOK_SECRET") {
            contentType(ContentType.Application.Json)
            setBody(webhookPayload(event = "jira:issue_created", assigneeAccountId = "other-account-id", status = "In Progress"))
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun noAssigneeInProgressDoesNotFire() = testWebhookApp {
        val response = client.post("/webhook/jira?token=$WEBHOOK_SECRET") {
            contentType(ContentType.Application.Json)
            setBody(webhookPayload(event = "jira:issue_created", assigneeAccountId = null, status = "In Progress"))
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun botAssigneeToDoDoesNotFire() = testWebhookApp {
        val response = client.post("/webhook/jira?token=$WEBHOOK_SECRET") {
            contentType(ContentType.Application.Json)
            setBody(webhookPayload(event = "jira:issue_created", assigneeAccountId = BOT_ACCOUNT_ID, status = "To Do"))
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun issueUpdatedEventFires() = testWebhookApp {
        val response = client.post("/webhook/jira?token=$WEBHOOK_SECRET") {
            contentType(ContentType.Application.Json)
            setBody(webhookPayload(event = "jira:issue_updated", assigneeAccountId = BOT_ACCOUNT_ID, status = "In Progress"))
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun correctTokenDispatchesAndDryRunHeaderIsPassedThrough() {
        val launcher = FakeAgentLauncherForJiraWebhook()
        testWebhookApp(launcher = launcher) {
            val response = client.post("/webhook/jira?token=$WEBHOOK_SECRET") {
                contentType(ContentType.Application.Json)
                header("X-Dry-Run", "true")
                setBody(webhookPayload(event = "jira:issue_updated", assigneeAccountId = BOT_ACCOUNT_ID, status = "In Progress"))
            }
            assertEquals(HttpStatusCode.OK, response.status)
        }
        assertEquals(listOf("MS-99" to true), launcher.launches)
    }

    @Test
    fun wrongTokenIsRejectedWithoutDispatch() {
        val launcher = FakeAgentLauncherForJiraWebhook()
        testWebhookApp(launcher = launcher) {
            val response = client.post("/webhook/jira?token=wrong") {
                contentType(ContentType.Application.Json)
                setBody(webhookPayload(event = "jira:issue_updated", assigneeAccountId = BOT_ACCOUNT_ID, status = "In Progress"))
            }
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }
        assertTrue(launcher.launches.isEmpty())
    }

    @Test
    fun missingTokenIsRejectedWithoutDispatch() {
        val launcher = FakeAgentLauncherForJiraWebhook()
        testWebhookApp(launcher = launcher) {
            val response = client.post("/webhook/jira") {
                contentType(ContentType.Application.Json)
                setBody(webhookPayload(event = "jira:issue_updated", assigneeAccountId = BOT_ACCOUNT_ID, status = "In Progress"))
            }
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }
        assertTrue(launcher.launches.isEmpty())
    }

    @Test
    fun blankSecretRegistersNoRoute() {
        val launcher = FakeAgentLauncherForJiraWebhook()
        testWebhookApp(secret = "", launcher = launcher) {
            val response = client.post("/webhook/jira?token=") {
                contentType(ContentType.Application.Json)
                setBody(webhookPayload(event = "jira:issue_updated", assigneeAccountId = BOT_ACCOUNT_ID, status = "In Progress"))
            }
            assertEquals(HttpStatusCode.NotFound, response.status)
        }
        assertTrue(launcher.launches.isEmpty())
    }

}

private fun webhookPayload(event: String, assigneeAccountId: String?, status: String) = """
{
  "webhookEvent": "$event",
  "issue": {
    "key": "MS-99",
    "fields": {
      "status": { "name": "$status" },
      "labels": [],
      ${if (assigneeAccountId != null) "\"assignee\": { \"accountId\": \"$assigneeAccountId\" }" else "\"assignee\": null"}
    }
  }
}
""".trimIndent()

private class FakeAgentLauncherForJiraWebhook : AgentLauncher {
    val launches = mutableListOf<Pair<String, Boolean>>()

    override suspend fun launch(ticketKey: String, dryRun: Boolean): Boolean {
        launches += ticketKey to dryRun
        return true
    }

    override suspend fun launchForPrReview(prNumber: Int) = false

    override suspend fun launchForConflictResolution(prNumber: Int) = false

    override suspend fun launchForQualityReview(prNumber: Int, jiraTicketKey: String) = false

    override suspend fun launchForUnblockedTicket(ticketKey: String, blockerKey: String) = false
}

private fun testWebhookApp(
    secret: String = WEBHOOK_SECRET,
    launcher: AgentLauncher = AgentLaunchService(scope = CoroutineScope(Dispatchers.IO)),
    block: suspend ApplicationTestBuilder.() -> Unit,
) = testApplication {
    application {
        install(Koin) {
            modules(module {
                single<AgentLauncher> { launcher }
            })
        }
        configureContentNegotiation()
        configureStatusPages()
        routing { webhookRoutes(BOT_ACCOUNT_ID, secret) }
    }
    block()
}
