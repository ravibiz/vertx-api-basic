package com.vk.vertxapi.api.handler;

import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.vk.vertxapi.api.AbstractRouteHandler;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class TestHandler extends AbstractRouteHandler
{
	private final static Logger LOG = LogManager.getLogger(TestHandler.class);
	
	public TestHandler(Vertx vertx) 
	{
		super(vertx);
        this.route().handler(BodyHandler.create());
        this.post("/").handler(this::fetchPostData);
        this.get("/").handler(this::fetchGetData);
	}

	public void fetchPostData(RoutingContext context)
	{
		LOG.info("Inside POST method");
		JsonObject requestParams = context.getBodyAsJson();
		LOG.info(requestParams.toString());
		sendSuccess(context, "Success");
	}
	
	public void fetchGetData(RoutingContext context)
	{
		LOG.info("Inside GET method");
		String b = context.request().getParam("b");
		LOG.info(b);
		sendSuccess(context, "Success");
	}

}
