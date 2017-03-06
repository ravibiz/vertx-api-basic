package com.vk.vertxapi.db;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.vk.vertxapi.api.LocalCache;
import com.vk.vertxapi.config.ConfigVerticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;

public class DatabaseVerticle extends AbstractVerticle
{
	private final static Logger LOG = LogManager.getLogger(DatabaseVerticle.class);
	public static final String DB_QUERY = "db.query";
	private JDBCClient client; 
	
	@Override
	public void start(Future<Void> startFuture) throws Exception
	{
		try
		{
			this.initClient(startFuture);
		}
		catch (Exception e)
		{
			LOG.error("Error in starting database service.", e);
			startFuture.fail(e);
		}
	}	

	private void initClient(Future<Void> startFuture) throws Exception
	{
		JsonObject config = (JsonObject) ConfigVerticle.getInstance().getConfigValue("jdbc");
		if (config == null) 
		{
			throw new RuntimeException("Could not find config with key 'jdbc'");
		}
		this.client = JDBCClient.createShared(vertx, config);
		this.client.getConnection(handler -> {
			if (handler.succeeded())
			{
				LOG.info("Connection to DB established.");
				handler.result().close();
				setupMessageHandler();
				startFuture.complete();
				LOG.info("Database service started.");
			}
			else
			{
				throw new RuntimeException(handler.cause());
			}
		});
	}

	private void setupMessageHandler()
	{
		EventBus eb = vertx.eventBus();
		eb.localConsumer(DB_QUERY, (Message<Integer> message) -> {

			QueryData qData = (QueryData) LocalCache.getInstance().remove(message.body());
			
			
			QueryPrepareService.getInstance().prepareQueryData(qData);
			

			if (qData.errorFlag)
			{
				message.reply(LocalCache.getInstance().store(qData));
				return;
			}
			
			if ( ! qData.queryDef.isAuthorized )
			{
				qData.errorFlag = true;
				qData.errorMessage = "Query unauthorized";
				message.reply(LocalCache.getInstance().store(qData));
				return;
			}

			handleQuery(message, qData);
		}).completionHandler(res -> {
			LOG.info("Database Service handler registered." + res.succeeded());
		});
	}
	
	private void handleQuery(Message message, QueryData qData)
	{
		qData.startQuery();
		
		this.client.getConnection(res -> {
		  if (res.succeeded()) 
		  {
		    SQLConnection connection = res.result();

		    if (qData.queryDef.isUpdateQuery)
		    {
		    	if ( qData.queryDef.runInBatch ) runBatchUpdateQuery(message, qData, connection);
		    	else runUpdateQuery(message, qData, connection);
		    }
		    else
		    {
		    	runSelectQuery(message, qData, connection);
		    }
		  } 
		  else 
		  {
			  LOG.error("Error in query.", res.cause());
			  qData.setError(res.cause());
			  Integer id = LocalCache.getInstance().store(qData);
			  message.reply(id);
		  }
		});
	}

	private void runSelectQuery(Message message, QueryData qData, SQLConnection connection)
	{
		connection.queryWithParams(qData.queryDef.query, qData.getParams(), res2 -> {
		  if (res2.succeeded()) 
		  {
			  qData.setResult(res2.result().getRows());
		  }
		  else
		  {
			  qData.setError(res2.cause());
		  }
		  connection.close();
		  message.reply(LocalCache.getInstance().store(qData));
		});
	}
	
	private void runUpdateQuery(Message message, QueryData qData, SQLConnection connection)
	{
		LOG.info("Query:" + qData.queryDef.query + ". Params:" + qData.getParams());
		connection.updateWithParams(qData.queryDef.query, qData.getParams(), res2 -> {
		  if (res2.succeeded()) 
		  {
			  qData.setResult(res2.result());
		  }
		  else
		  {
		      qData.setError(res2.cause());
			  LOG.error("Error:", res2.cause());
		  }
		  connection.close();
		  message.reply(LocalCache.getInstance().store(qData));
		});
	}

	private void runBatchUpdateQuery(Message message, QueryData qData, SQLConnection connection)
	{
		LOG.info("Batch Query:" + qData.queryDef.query + ". Params:" + qData.getBatchParams());
		connection.batchWithParams(qData.queryDef.query, qData.getBatchParams(), res2 -> {
		  if (res2.succeeded()) 
		  {
			  qData.setBatchResult(res2.result());
		  }
		  else
		  {
		      qData.setError(res2.cause());
			  LOG.error("Error:", res2.cause());
		  }
		  connection.close();
		  message.reply(LocalCache.getInstance().store(qData));
		});
	}

	public static void main(String[] args) throws Exception
	{
		Vertx.vertx().deployVerticle(ConfigVerticle.class.getCanonicalName(), result -> {
			
			if ( result.succeeded() )
			{
				Vertx.vertx().deployVerticle(DatabaseVerticle.class.getCanonicalName(), d -> {
					
					if ( d.succeeded())
					{
						LOG.info("Successfully started database service");
					}
					else
					{
						LOG.error("Error in starting database service");
					}
				});
			}
		});
	}
}
