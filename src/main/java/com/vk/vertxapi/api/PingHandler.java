package com.vk.vertxapi.api;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class PingHandler extends AbstractRouteHandler {

    public PingHandler(Vertx vertx) {
        super(vertx);
        this.route().handler(BodyHandler.create());
        this.get("/").handler(this::pong);
        this.post("/").handler(this::pong);
    }

    private void pong(RoutingContext context) 
    {
    	String pongMessage = new JsonObject().put("status", "success").put("message", "pong").encode();
    	sendJsonResponse(context, pongMessage);
    }

}
