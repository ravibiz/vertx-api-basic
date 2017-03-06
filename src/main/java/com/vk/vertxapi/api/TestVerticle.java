package com.vk.vertxapi.api;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;

public class TestVerticle extends AbstractVerticle {

	@Override
	public void start(Future<Void> fut) 
	{
		HttpServer server = vertx.createHttpServer();
		Router router = Router.router(vertx);

		router.route(HttpMethod.POST, "/catalogue/products/:producttype/:productid").handler(routingContext -> 
		{
			System.out.println("STEP-1");

			String producttype = routingContext.request().getParam("producttype");
			String productid = routingContext.request().getParam("productid");

			System.out.println("PRODUCTTYPE = " + producttype);
			System.out.println("PRODUCTID = " + productid);

			routingContext.response().putHeader("content-type", "text/html")
					.end("<h1>PRODUCTTYPE = " + producttype + "<br/> PRODUCTID = " + productid + "</h1>");
		});
		server.requestHandler(router::accept).listen(8081);
	}

	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(TestVerticle.class.getCanonicalName());
	}
}