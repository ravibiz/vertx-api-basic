package com.vk.vertxapi.api;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.vk.vertxapi.db.QueryData;

import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.impl.RouterImpl;

public abstract class AbstractRouteHandler extends RouterImpl 
{

	private final static Logger LOG = LogManager.getLogger(AbstractRouteHandler.class);

    private RouteUtil routeUtil = RouteUtil.getInstance();
    protected Vertx vertx;

    public AbstractRouteHandler(Vertx vertx)
    {
        super(vertx);
        this.vertx = vertx;
    }

    protected void sendError(RoutingContext context, String message)
    {
        routeUtil.sendError(context, message);
    }

    protected void sendJsonResponseFromFile(RoutingContext context, String filePath)
    {
        routeUtil.sendJsonResponseFromFile(context, filePath);
    }

    protected void sendJsonResponseFromFile(RoutingContext context, String filePath, String defaultContent)
    {
        routeUtil.sendJsonResponseFromFile(context, filePath, defaultContent);
    }

    protected void sendJsonResponse(RoutingContext context, String json)
    {
        routeUtil.sendJsonResponse(context, json);
    }

    protected void sendQueryResponse(RoutingContext context, QueryData qData)
    {
    	if (null == qData) sendUknownError(context);
    	else sendJsonResponse(context, qData.getJson());
    }

    final static String UNKNOWN_ERROR = "Unknown error occured";
    protected void sendUknownError(RoutingContext context)
    {
        routeUtil.sendError(context, UNKNOWN_ERROR);
    }

    protected void sendSuccess(RoutingContext context, String message)
    {
        routeUtil.sendSuccess(context, message);
    }
    

    protected void sendFile(RoutingContext context, String message)
    {
        routeUtil.sendSuccess(context, message);
    }

}
