package com.vk.vertxapi.message;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.vk.vertxapi.api.LocalCache;
import com.vk.vertxapi.api.VertxInstance;
import com.vk.vertxapi.db.DatabaseVerticle;
import com.vk.vertxapi.db.QueryData;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

public class MessageVerticle extends AbstractVerticle
{
    private final static Logger LOG = LogManager.getLogger(MessageVerticle.class);
    private static MessageVerticle thisInstace = null;
    
    private final Map<String, String> messageMap = new HashMap<String, String>();
    
    public MessageVerticle()
    {
    }

    public static MessageVerticle getInstance()
    {
    	return thisInstace;
    }
    
    public String getMessage(Enum key)
    {
    	String message = messageMap.get(key.toString());
    	return null == message ? "" : message;
    }

    public String getMessage(Enum key, Object[] args)
    {
    	String message = messageMap.get(key.toString());
    	message = null == message ? "" : message;
    	return String.format(message, args);
    }

    
    @Override
    public void start(Future<Void> startFuture) throws Exception
    {
        this.loadMessages(startFuture);
    }

    @Override
    public void stop() throws Exception
    {
        super.stop();
    }

    private void loadMessages(Future<Void> startFuture)
    {
		JsonObject queryParams = new JsonObject();
        Integer id = LocalCache.getInstance().store(new QueryData("select.messages", queryParams));
        VertxInstance.get().eventBus().send(DatabaseVerticle.DB_QUERY, id,
                (AsyncResult<Message<Integer>> res) -> 
        {
            QueryData resultData = (QueryData) LocalCache.getInstance().remove(res.result().body());
            if (resultData.errorFlag)
            {
                LOG.error("Error in fetching server messages " + resultData.errorMessage, resultData.error);
                startFuture.fail("Error in starting message service");
    		}
            else
            {
            	LOG.info("Successfully started message service");
            	List<JsonObject> rows = resultData.rows;
            	for ( JsonObject row : rows)
            	{
            		String messageCode = row.getString("messageCode");
            		String message = row.getString("message");
            		messageMap.put(messageCode, message);
            	}
            	thisInstace = this;
            	startFuture.complete();
            }
        });
    }
}
