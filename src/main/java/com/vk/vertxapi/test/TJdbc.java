package com.vk.vertxapi.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.vk.vertxapi.api.VertxInstance;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.asyncsql.PostgreSQLClient;
import io.vertx.ext.sql.SQLConnection;

public class TJdbc 
{
	
	public static void main(String[] args) 
	{
		
		 tt();
		
		//basic();
		
	}

	private static void tt() {
		final AsyncSQLClient jdbc = PostgreSQLClient.createNonShared(VertxInstance.get(), new JsonObject()
		        .put("host", "127.0.0.1")
		        .put("port", 5432)
		        .put("username", "ola")
		        .put("password", "ola")
		        .put("database", "appdb")
		        .put("driver_class", "org.postgresql.Driver")
		        .put("maxPoolSize", 20)
		        .put("charset", "UTF-8")
		        .put("queryTimeout", 10000)
				);
		
		jdbc.getConnection(res -> {
			if ( res.succeeded() )
			{
				System.out.println("connected");
				SQLConnection connection = res.result();
				
				connection.queryWithParams("select * from messages", new JsonArray(), res2 -> {
					  if (res2.succeeded()) 
					  {
						  System.out.println("QE Sucesss");
						  System.out.println(res2.result().getRows());
					  }
					  else
					  {
						  System.out.println("QE failed");
						  System.out.println(res2.cause());
					  }
					  connection.close();
					});
				
			}
			else
			{
				res.cause().printStackTrace();
			}
		});
	}

	private static void basic() throws SQLException {
		Connection connection = null;
		try {

			Class.forName("org.postgresql.Driver");
			
			connection = DriverManager.getConnection(
					"jdbc:postgresql://localhost:5432/appdb", "ola",
					"ola");

		} catch (Exception e) {

			System.out.println("Connection Failed! Check output console");
			e.printStackTrace();
			return;

		}
		finally
		{
			if (connection != null) {
				System.out.println("You made it, take control your database now!");
				connection.close();
			} else {
				System.out.println("Failed to make connection!");
			}
		}
	}
}
