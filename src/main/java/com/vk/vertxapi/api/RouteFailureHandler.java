package com.vk.vertxapi.api;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

public class RouteFailureHandler implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext context) {

        int statusCode = context.statusCode();
        HttpServerResponse response = context.response();
        String uri = context.request().uri();

        if (statusCode == 404 && !uri.contains(".")) 
        {
            RouteUtil.getInstance().sendResponseFromFile(context, "webroot/index.html", RouteUtil.TEXT_HTML_TYPE);
        } 
        else 
        {
            response.setStatusCode(statusCode).end("Unauthorized access, Please login");
        }

    }
}
