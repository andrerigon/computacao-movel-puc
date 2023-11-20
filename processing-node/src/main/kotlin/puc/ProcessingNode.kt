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
        // adiciona o tipo de evento que será enviado pro cep processar
        cepConfig.addEventType("WaterMeasurement", WaterMeasurement::class.java.name)
        // configura a engine e guarda referencia pro runtime
        val cep = EPServiceProviderManager.getProvider("myCEPEngine", cepConfig)
        cepRT = cep.epRuntime

        val cepAdm = cep.epAdministrator
        // configura uma query de cep
        val cepStatement = """
            select 
                id, avg(ph) ph, avg(o2) o2, count(*) total, max(updatedAt) updated_at
            from 
                WaterMeasurement#time(50 seconds) 
            where 
                clean = true group by id having count(*) > 1
        """
        // cria um inspetor e adiciona a propria query como listener
        val cepInspector = cepAdm.createEPL(cepStatement)
        cepInspector.addListener(this)
    }

    /**
     * Recebe eventos do mobile hub
     */
    override fun recordReceived(record: ConsumerRecord<*, *>) {
        try {
            // deserializa o record (bytearray) para um mapa
            val data = swap.SwapDataDeserialization(record.value() as ByteArray)
            val typeRef = object : TypeReference<Map<String, String>>() {}

            val map: Map<String, String> = objectMapper.readValue(data.message, typeRef)
            // ignora se não for um tópico de dados
            if (map["topic"] != "data") return

            // extrai os dados do sensor e cria um um objeto de input pro cep
            val typeRef2 = object : TypeReference<Map<String, Any>>() {}
            val payload = objectMapper.readValue(map["payload"]!!.replace("\\", "").drop(1).dropLast(1), typeRef2)

            val (id, ph, o2, clean) = (payload["sensor_data"] as List<*>).map { it.toString().toDouble() }
            val measurement = WaterMeasurement(
                id.toInt(), ph, o2, clean == 1.0, Date()
            )
            println(measurement)
            // envia o arquivo pro cep analisar
            cepRT.sendEvent(measurement)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    // recebe os eventos do cep que passaram na query de test
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
