package me.hchome

import io.vertx.core.Context
import io.vertx.core.Vertx
import io.vertx.core.eventbus.MessageConsumer
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.awaitResult
import io.vertx.servicediscovery.Record
import io.vertx.servicediscovery.ServiceDiscovery
import io.vertx.servicediscovery.ServiceDiscoveryOptions
import io.vertx.servicediscovery.types.EventBusService
import io.vertx.serviceproxy.ServiceBinder
import me.hchome.service.HelloService
import me.hchome.service.impl.HelloServiceImpl
import java.util.concurrent.ConcurrentHashMap

class MainVerticle : CoroutineVerticle() {
    private lateinit var discovery: ServiceDiscovery
    private val records: ConcurrentHashMap<Record, MessageConsumer<JsonObject>> = ConcurrentHashMap()


    override fun init(vertx: Vertx, context: Context) {
        super.init(vertx, context)
        discovery = ServiceDiscovery.create(vertx, ServiceDiscoveryOptions().setBackendConfiguration(config))
    }

    override suspend fun start() {
        val record = EventBusService.createRecord(HelloService.SERVICE_NAME,
                HelloService.SERVICE_ADDRESS, HelloService::class.java, JsonObject())
        records[awaitResult { discovery.publish(record, it) }] = ServiceBinder(vertx)
                .setAddress(HelloService.SERVICE_ADDRESS)
                .register(HelloService::class.java, HelloServiceImpl(vertx))
    }


    override suspend fun stop() {
        for ((record, consumer) in records) {
            ServiceBinder(vertx).unregister(consumer)
            awaitResult<Void> { discovery.unpublish(record.registration, it) }
        }
    }
}