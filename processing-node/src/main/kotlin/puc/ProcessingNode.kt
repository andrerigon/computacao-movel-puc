package puc

import ckafka.data.Swap
import ckafka.data.SwapData
import com.espertech.esper.client.Configuration
import com.espertech.esper.client.EPRuntime
import com.espertech.esper.client.EPServiceProviderManager
import com.espertech.esper.client.EventBean
import com.espertech.esper.client.UpdateListener
import com.espertech.esper.event.map.MapEventBean
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import main.java.application.ModelApplication
import org.apache.kafka.clients.consumer.ConsumerRecord
import java.util.Date


object ProcessingNode : ModelApplication(), UpdateListener {

    private val objectMapper = jacksonObjectMapper()
    private val swap = Swap(objectMapper)
    var cepRT: EPRuntime

    init {
        val cepConfig = Configuration()
        cepConfig.addEventType("WaterMeasurement", WaterMeasurement::class.java.name)
        val cep = EPServiceProviderManager.getProvider("myCEPEngine", cepConfig)
        cepRT = cep.epRuntime

        val cepAdm = cep.epAdministrator
        val cepStatement = """
            select 
                id, avg(ph) ph, avg(o2) o2, count(*) total, max(updatedAt) updated_at
            from 
                WaterMeasurement#time(50 seconds) 
            where 
                clean = true group by id having count(*) > 1
        """
        val cepInspector = cepAdm.createEPL(cepStatement)
        cepInspector.addListener(this)
    }

    override fun recordReceived(record: ConsumerRecord<*, *>) {
        try {
            val data = swap.SwapDataDeserialization(record.value() as ByteArray)
            val typeRef = object : TypeReference<Map<String, String>>() {}

            val map: Map<String, String> = objectMapper.readValue(data.message, typeRef)
            if (map["topic"] != "data") return

            val typeRef2 = object : TypeReference<Map<String, Any>>() {}

            val payload = objectMapper.readValue(map["payload"]!!.replace("\\", "").drop(1).dropLast(1), typeRef2)

            val (id, ph, o2, clean) = (payload["sensor_data"] as List<*>).map { it.toString().toDouble() }
            val measurement = WaterMeasurement(
                id.toInt(), ph, o2, clean == 1.0, Date()
            )
            println(measurement)
            cepRT.sendEvent(measurement)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun update(newEvents: Array<out EventBean>, oldEvents: Array<out EventBean>?) {
        val props = (newEvents.first() as MapEventBean).properties
        println("Just got an CEP event: $props}")
    }
}

data class WaterMeasurement(
    val id: Int,
    val ph: Double,
    val o2: Double,
    val clean: Boolean,
    val updatedAt: Date
)

fun main() {
    ProcessingNode
}
