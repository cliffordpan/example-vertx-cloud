package me.hchome

import io.vertx.circuitbreaker.CircuitBreaker
import io.vertx.circuitbreaker.CircuitBreakerOptions
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.http.HttpServer
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.kotlin.coroutines.awaitResult
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.servicediscovery.ServiceDiscovery
import io.vertx.servicediscovery.ServiceDiscoveryOptions
import io.vertx.servicediscovery.types.EventBusService
import kotlinx.coroutines.experimental.launch
import me.hchome.service.HelloService

class GateWayVerticle : AbstractVerticle() {

    private lateinit var server: HttpServer
    private lateinit var breaker: CircuitBreaker

    private lateinit var discovery: ServiceDiscovery


    private val service: Future<HelloService>
        get() {
            val future = Future.future<HelloService>()
            EventBusService.getProxy(discovery, HelloService::class.java, future.completer())
            return future
        }


    @Throws(Exception::class)
    override fun start(startFuture: Future<Void>) {
        server = vertx.createHttpServer(HttpServerOptions(config()))
        breaker = CircuitBreaker.create("circuit-breaker", vertx,
                CircuitBreakerOptions(config().getJsonObject("circuit-breaker", JsonObject())))
        discovery = ServiceDiscovery.create(vertx, ServiceDiscoveryOptions().setBackendConfiguration(config()))

        val mainRouter = Router.router(vertx)
        mainRouter.route().handler(BodyHandler.create())

        mainRouter.get("/hello").handler { ctx ->
            val name = ctx.queryParam("name").stream().findFirst().orElse(null)
            val response = ctx.response()
            breaker.execute<Void> { future ->
                launch(ctx.vertx().dispatcher()) {
                    try {
                        val service = awaitResult<HelloService> { EventBusService.getProxy(discovery, HelloService::class.java, it) }
                        response.end(awaitResult<String> { service.sayHello(name, it) })
                        future.complete()
                    } catch (throwable: Throwable) {
                        future.fail(throwable)
                    }
                }
            }.setHandler { ar -> if (ar.failed()) ctx.fail(ar.cause()) }
        }

        server.requestHandler(mainRouter::accept).listen(8080)
    }

    @Throws(Exception::class)
    override fun stop(stopFuture: Future<Void>) {
        this.server.close(stopFuture.completer())
        super.stop(stopFuture)
    }
}