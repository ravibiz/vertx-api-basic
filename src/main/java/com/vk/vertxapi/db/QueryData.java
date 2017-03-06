package com.vk.vertxapi.db;

import java.util.ArrayList;
import java.util.List;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.UpdateResult;

public class QueryData
{
	public String queryId;
	public QueryDef queryDef;
	
	public JsonObject paramsObject;
	public List<JsonObject> paramsObjectList;
	public List<Integer> batchUpdatedRows;
	
	public List<JsonObject> rows;
	public UpdateResult updateResult;

	public long responseTimeInMillis;
	public boolean errorFlag;
	public Throwable error;
	public String errorMessage;
	private long startTime;
	public String role;
	
	public QueryData()
	{
		
	}
	
	public QueryData(String queryId, JsonObject paramsObject)
	{
		this.queryId = queryId;
		this.paramsObject = paramsObject;
	}

	public QueryData(String queryId, List<JsonObject> paramsObjectList, boolean dummy)
	{
		this.queryId = queryId;
		this.paramsObjectList = paramsObjectList;
	}

	public JsonArray getParams()
	{
		if (this.paramsObject == null || this.queryDef == null || this.queryDef.paramList == null) return null;
		return buildParams(this.paramsObject, this.queryDef.paramList);
	}

	public List<JsonArray> getBatchParams()
	{
		if (this.paramsObjectList == null || this.queryDef == null || this.queryDef.paramList == null) return null;
		
		List<JsonArray> batchParamsL = new ArrayList<>();
		for (JsonObject paramsObj : this.paramsObjectList)
		{
			batchParamsL.add(buildParams(paramsObj, this.queryDef.paramList));
		}
		return batchParamsL;
	}

	private JsonArray buildParams(JsonObject paramsObj, String[] paramList ) 
	{
		JsonArray params = new JsonArray();
		for (String paramKey : paramList)
		{
			Object value = paramsObj.getValue(paramKey);
			
			if (value == null)
			{
				params.addNull();
			}
			else if (value instanceof JsonObject)
			{
				params.add(value.toString());
			}
			else
			{
				params.add(value);
			}
		}
		return params;
	}

	public QueryData checkIsAuthorized(String role)
	{
		this.role = role;
		return this;
	}

	public boolean isAuthorized(String role)
	{
		this.role = role;
		QueryPrepareService.getInstance().prepareQueryData(this);
		return this.queryDef.isAuthorized;
	}

	public QueryData setError(String error)
	{
		this.errorFlag = true;
		this.errorMessage = error;
		this.endQuery();
		return this;
	}
	
	public QueryData setError(Throwable cause)
	{
		this.error = cause;
		return this.setError(cause.getMessage());
	}
	
	public QueryData  startQuery()
	{
		this.startTime = System.currentTimeMillis();
		return this;
	}

	public QueryData endQuery()
	{
		this.responseTimeInMillis = System.currentTimeMillis() - this.startTime;
		this.startTime = 0;
		return this;
	}

	public JsonArray getJsonDataRows(String jsonField)
	{
		JsonArray jsonRows = new JsonArray();
		for (JsonObject record : this.rows)
		{
			jsonRows.add(new JsonObject(record.getString(jsonField)));
		}
		return jsonRows;
	}

	public QueryData setResult(List<JsonObject> rows)
	{
		this.rows = rows;
		return this.endQuery();
	}

	public QueryData setBatchResult(List<Integer> updatedRows)
	{
		this.batchUpdatedRows  = updatedRows;
		return this.endQuery();
	}

	public QueryData setResult(UpdateResult updateResult)
	{
		this.updateResult = updateResult;
		return this.endQuery();
	}
	
	public String getJson()
	{
		if ( null != this.rows)
			return "{ \"values\" : " + this.rows.toString() + " }";
		
		if ( null != updateResult)
			return updateResult.toJson().toString();
		
		return new JsonObject().put("status", "error").put("message", this.errorMessage).toString();
	}
	
	public String getJsonObj(String key)
	{
		if ( null != this.rows)
			return "{ \"" + key + "\" : " + this.rows.get(0) + " }";
		
		if ( null != updateResult)
			return updateResult.toJson().toString();
		
		return new JsonObject().put("status", "error").put("message", this.errorMessage).toString();
	}
	
	public String getJson(String key)
	{
		if ( null != this.rows)
			return "{ \"" + key + "\" : " + this.rows.toString() + " }";
		
		if ( null != updateResult)
			return updateResult.toJson().toString();
		
		return new JsonObject().put("status", "error").put("message", this.errorMessage).toString();
	}
	
	public String getJsonByKey(String outputKey, String key)
	{
		if ( null != this.rows)
			return "{ \"" + outputKey + "\" : " + this.rows.get(0).getString(key) + " }";
		
		if ( null != updateResult)
			return updateResult.toJson().toString();
		
		return new JsonObject().put("status", "error").put("message", this.errorMessage).toString();
	}
	
	public String getJsonColumnObj(String key)
	{
		if ( null != this.rows)
			return this.rows.get(0).getString(key).toString();
		
		return new JsonObject().put("status", "error").put("message", this.errorMessage).toString();
	}
}
