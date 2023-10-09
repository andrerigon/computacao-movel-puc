package puc

import ckafka.data.SwapData
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import lac.cnclib.net.NodeConnection
import lac.cnclib.sddl.message.Message
import main.java.ckafka.mobile.CKMobileNode
import main.java.ckafka.mobile.tasks.SendLocationTask
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.UUID
import java.util.concurrent.TimeUnit

object MobileNode : CKMobileNode() {
    override fun connected(p0: NodeConnection?) {
        try {
            logger.debug("Connected")
            val sendlocationtask = SendLocationTask(this)
            scheduledFutureLocationTask = threadPool.scheduleWithFixedDelay(
                sendlocationtask, 5000, 60000, TimeUnit.MILLISECONDS
            )
        } catch (e: Exception) {
            logger.error("Error scheduling SendLocationTask", e)
        }
    }

    override fun disconnected(p0: NodeConnection?) {

    }

    override fun newMessageReceived(conn: NodeConnection?, message: Message) {
        try {
            val swp = fromMessageToSwapData(message)
            if (swp.topic == "Ping") {
                message.senderID = this.mnID
                sendMessageToGateway(message)
            } else {
                val str = String(swp.message, StandardCharsets.UTF_8)
                logger.info("Got message: $str")
            }
        } catch (e: Exception) {
            logger.error("Error reading new message received")
        }
    }

    override fun unsentMessages(p0: NodeConnection?, p1: MutableList<Message>?) {
        println(p1)
    }

    override fun internalException(p0: NodeConnection?, p1: Exception?) {
        TODO("Not yet implemented")
    }

    fun sendUnicastMessage(text: String) {
        val privateData = SwapData().apply {
            message = text.toByteArray(StandardCharsets.UTF_8)
            topic = "PrivateMessageTopic"
            recipient = UUID.randomUUID().toString()
        }
        val message = createDefaultApplicationMessage().apply {
            contentObject = privateData
        }
        sendMessageToGateway(message)
    }

    fun sendMessageToPN(text: String) {
        val data = SwapData().apply {
            message = text.toByteArray(StandardCharsets.UTF_8)
            topic = "AppModel"
        }
        sendMessageToGateway(
            createDefaultApplicationMessage().apply {
                contentObject = data
            }
        )
    }
}

data class WaterMeasurement(val id: Int, val ph: Double, val o2: Double, val clean: Boolean, val updatedAt: Date)


fun main() {
//    val mapper = jacksonObjectMapper()
//    MobileNode.apply {
//        listOf(
//            WaterMeasurement(1, 0.23, 0.34, true, Date.from(Instant.now().minus(5, ChronoUnit.DAYS))),
//            WaterMeasurement(2, 0.23, 0.34, false, Date.from(Instant.now().minus(50, ChronoUnit.DAYS))),
//            WaterMeasurement(1, 0.23, 0.34, true, Date.from(Instant.now().minus(14, ChronoUnit.DAYS)))
//        ).forEach { sendMessageToPN(mapper.writeValueAsString(it)) }
//    }
    MobileNode
        //.sendUnicastMessage("mas pqp")

}

