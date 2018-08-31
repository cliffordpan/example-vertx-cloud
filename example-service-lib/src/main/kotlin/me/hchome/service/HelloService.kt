package me.hchome.service

import io.vertx.codegen.annotations.Fluent
import io.vertx.codegen.annotations.GenIgnore
import io.vertx.codegen.annotations.ProxyGen
import io.vertx.codegen.annotations.VertxGen
import io.vertx.core.AsyncResult
import io.vertx.core.Handler

@ProxyGen
@VertxGen
interface HelloService {
    @Fluent
    fun sayHello(name: String?, handler: Handler<AsyncResult<String>>): HelloService

    @GenIgnore
    companion object {
        const val SERVICE_NAME = "hello-service"
        const val SERVICE_ADDRESS = "me.hchome.services.$SERVICE_NAME"
    }
}