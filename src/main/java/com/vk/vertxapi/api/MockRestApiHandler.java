package com.vk.vertxapi.api;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;

public class MockRestApiHandler extends AbstractRouteHandler 
{
	private final static Logger LOG = LogManager.getLogger(MockRestApiHandler.class);
	
	public MockRestApiHandler(Vertx vertx) 
	{
		super(vertx);
		this.post("/testjson").handler(this::testJson);
	}

	final static String testjson = "test.json";  
	private void testJson(RoutingContext context) 
    {
		sendJsonResponseFromFile(context, testjson);
    }
	
}
