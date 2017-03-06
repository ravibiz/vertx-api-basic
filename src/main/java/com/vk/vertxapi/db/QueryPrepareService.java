package com.vk.vertxapi.db;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.vk.vertxapi.config.ConfigVerticle;
import com.vk.vertxapi.util.MultiFileReader;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;


public class QueryPrepareService extends AbstractVerticle implements MultiFileReader.ResultHandler
{
	private final static Logger LOG = LogManager.getLogger(QueryPrepareService.class);
	private Map<String, QueryDef> queryMap;
	
	private static QueryPrepareService INSTANCE;
	
	public static QueryPrepareService getInstance()
	{
		return INSTANCE;
	}

	private Future<Void> startFuture;

	@Override
	public void start(Future<Void> startFuture) throws Exception
	{
		this.startFuture = startFuture;
		this.setupQueryMap();
		INSTANCE = this;
	}

	private void setupQueryMap()
	{
		JsonArray queryFiles = (JsonArray) ConfigVerticle.getInstance().getConfigValue("queryfiles");
        if (queryFiles == null || queryFiles.isEmpty())
        {
            queryFiles = new JsonArray().add("config/queries.json");
        }
		new MultiFileReader(queryFiles.getList(), this).read();
	}
	
	public QueryData prepareQueryData(QueryData qData)
	{
		if (qData.queryDef == null)
		{
			if (!this.queryMap.containsKey(qData.queryId))
			{
				qData.setError("Query not configured:" + qData.queryId);
				return qData;
			}
			
			QueryDef qDef = this.queryMap.get(qData.queryId);
			qData.queryDef = qDef;
			if ( null != qData.role ) qData.queryDef.setIsAuthorized(qData.role);
		}
		
		/*
		if (qData.queryDef.isInsertQuery)
		{
			qData.paramsObject.put("id", SequenceIdGenerator.getInstance().getNextSequence(qData.queryDef.tableForInsert));
		}
		*/
		
		return qData;
	}
	
	@Override
	public void onSuccess(List<String> files, List<String> filesData)
	{
		this.queryMap = new HashMap<String, QueryDef>();
		for (String fileData : filesData)
		{
			JsonObject json = new JsonObject(fileData);
			for (Object queryObject : json.getJsonArray("queries"))
			{
				QueryDef queryDef = new QueryDef((JsonObject) queryObject);
				this.queryMap.put(queryDef.queryId, queryDef);
			}
		}
		LOG.info("Query map created.");
		this.startFuture.complete();
	}

	@Override
	public void onError(String filename, Throwable cause)
	{
		LOG.error("Could not create query map due to an error with file: " + filename, cause);
		this.startFuture.fail(cause);
	}
	
}
