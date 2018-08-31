package me.hchome.service.impl

import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import me.hchome.service.HelloService

class HelloServiceImpl : HelloService {

    override fun sayHello(name: String?, handler: Handler<AsyncResult<String>>): HelloService = this.apply {
        handler.handle(Future.succeededFuture("Hello, ${name ?: "world"}"))
    }
}