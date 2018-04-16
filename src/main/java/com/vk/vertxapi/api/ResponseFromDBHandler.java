package com.vk.vertxapi.api;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.vk.vertxapi.config.ConfigVerticle;
import com.vk.vertxapi.db.DatabaseVerticle;
import com.vk.vertxapi.db.QueryData;

import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class ResponseFromDBHandler extends AbstractRouteHandler
{
    private final static Logger LOG = LogManager.getLogger(ResponseFromDBHandler.class);

    private String query;
    private String[] params = null;
    private String[] resultFields = null;
    private String defaultContent = "{\"values\":[]}";
    private String key = null;
    private boolean secureApi = false;
    
    public ResponseFromDBHandler(String query, JsonArray params, JsonArray resultFields, String defaultContent, String key)
    {
        super(VertxInstance.get());
        this.query = query;
        this.defaultContent = defaultContent;
        this.params = this.getStringArray(params);
        this.resultFields = this.getStringArray(resultFields);
        this.key = key;
        this.secureApi = ConfigVerticle.getInstance().getBoolean("secure_api", false);
        this.route().handler(BodyHandler.create());
        this.get("/").handler(this::serveResponse);
        this.post("/").handler(this::servePostResponse);
    }

    private String[] getStringArray(JsonArray jsonArray)
    {
        if (jsonArray.isEmpty()) return null;
        String[] values = new String[jsonArray.size()];
        for (int i=0; i < jsonArray.size(); i++)
        {
            values[i] = jsonArray.getString(i);
        }
        return values;
    }

    private void serveResponse(RoutingContext context)
    {
        JsonObject paramsJson = this.getRequestParams(context);
        this.fetchDataAndSend(context, paramsJson);
    }

    private void servePostResponse(RoutingContext context)
    {
        JsonObject paramsJson = context.getBodyAsJson();
        this.fetchDataAndSend(context, paramsJson);
    }
    
    private JsonObject getRequestParams(RoutingContext context)
    {
        JsonObject paramsJson = new JsonObject();
        if (this.params != null)
        {
            for (int i=0; i < this.params.length; i++)
            {
                paramsJson.put(this.params[i], context.request().getParam(this.params[i]));
            }
        }
        return paramsJson;
    }

    private void fetchDataAndSend(RoutingContext context, JsonObject paramsData)
    {
        QueryData queryData = new QueryData(this.query, paramsData);
    	
        if ( secureApi )
	    	{
        			JsonObject payloadJson = this.getPayloadJson(context);
	            String role = null;
	            if ( null != payloadJson )
	            {
	            		role = payloadJson.getString("role");
	                overrideSecureParams(payloadJson, paramsData);
	            }
	            
	            if ( ! queryData.isAuthorized(role) )
	            {
	            		context.fail(401);
	            		return;
	            }
	    	}
        
        Integer id = LocalCache.getInstance().store(queryData);
        
        LOG.info("Executing query:" + this.query + " | " + paramsData);
        
        VertxInstance.get().eventBus().send(DatabaseVerticle.DB_QUERY, id,
                (AsyncResult<Message<Integer>> selectResult) -> 
        {
                QueryData selectData = (QueryData) LocalCache.getInstance().remove(selectResult.result().body());
                if (selectData == null || selectData.rows == null)
                { 
                    sendError(context, this.defaultContent);
                }
                else
                {
                    if (this.resultFields == null)
                    {
                    	if (this.key == null)
                    		sendJsonResponse(context, selectData.rows.toString());
                    	else
                    		sendJsonResponse(context, selectData.getJson(this.key));
                    }
                    else
                    {
                        sendJsonResponse(context, selectData.getJsonDataRows(this.resultFields[0]).toString());
                    }
                }
         });

    }

	private void overrideSecureParams(JsonObject tokenPayloadJson, JsonObject paramsData) 
	{
		paramsData.put("uid", tokenPayloadJson.getString("uid"));
	}
	
	private JsonObject getPayloadJson (RoutingContext context)
	{
		AuthUser user = (AuthUser) context.user();
		if ( null == user ) return null;
		JsonObject tokenPayloadJson = user.principal();
		context.clearUser();
		return tokenPayloadJson;
	}
}
