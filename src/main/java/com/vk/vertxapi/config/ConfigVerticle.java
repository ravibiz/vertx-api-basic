package com.vk.vertxapi.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.vk.vertxapi.api.VertxInstance;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public class ConfigVerticle extends AbstractVerticle
{
	private static final Logger LOG = LogManager.getLogger(ConfigVerticle.class);

	private static ConfigVerticle INSTANCE;

	private String baseConfigFile = "config/conf-default.json";
	private String esConfig = "config/es-default.json";
	private String servicesConfig = "config/services-default.json";
	private String restUrlConfig = "config/resturl-default.json";

	private List<String> configFilesToLoad;

	private JsonObject serverConfig = null;

	public ConfigVerticle()
	{
		this(Collections.EMPTY_LIST);
	}

    public ConfigVerticle(String siteConfigFile)
    {
        this(Collections.singletonList(siteConfigFile));
    }

	public ConfigVerticle(List<String> configFilesToLoad)
	{
		this.configFilesToLoad = configFilesToLoad;
	}

    public static ConfigVerticle getInstance()
	{
		return INSTANCE;
	}
	
	public Object getConfigValue(String key)
	{
		return this.serverConfig.getValue(key);
	}

	public String getStringValue(String key, String defaultValue)
	{
		if (!this.serverConfig.containsKey(key)) return defaultValue;
		return (String) this.serverConfig.getValue(key);
	}

	public int getInteger(String key, int defaultValue)
	{
		return this.serverConfig.getInteger(key, defaultValue);
	}

	public boolean getBoolean(String key, boolean defaultValue)
	{
		return this.serverConfig.getBoolean(key, defaultValue);
	}

	@Override
	public void start(Future<Void> startFuture) throws Exception
	{
		Deque<String> allConfigFiles = new ArrayDeque<>();
		if (this.configFilesToLoad != null && !this.configFilesToLoad.isEmpty())
		{
			int size = this.configFilesToLoad.size();
			for (int i = (size -1); i>=0; i--)
			{
				allConfigFiles.push(this.configFilesToLoad.get(i));
			}
		}
		allConfigFiles.push(this.esConfig);
		allConfigFiles.push(this.servicesConfig);
		allConfigFiles.push(this.restUrlConfig);
		allConfigFiles.push(this.baseConfigFile);

		this.serverConfig = new JsonObject();
		this.loadConfig(allConfigFiles, startFuture);
		INSTANCE = this;
	}

	public ServicesConfig getServices()
	{
		return new ServicesConfig((JsonObject) this.getConfigValue("services"));
	}

	private void loadConfig(Deque<String> configFiles, Future<Void> startFuture)
	{
		if (configFiles.isEmpty())
		{
			LOG.info("Final merged config : " + this.serverConfig.encodePrettily());
			startFuture.complete();
			return;
		}

		String configFile = configFiles.pop();
		VertxInstance.get().fileSystem().readFile(configFile, result -> {
			if (result.succeeded())
			{
				String jsonText = result.result().toString();
				JsonObject configJson = new JsonObject(jsonText);
				this.serverConfig = this.serverConfig.mergeIn(configJson);

				LOG.info("Config file: " + configFile + ". Value:" + configJson.encodePrettily());
				loadConfig(configFiles, startFuture);
			}
			else
			{
				this.serverConfig = new JsonObject();
				String message = "Could not read config file : " + configFile + ". Cause:" + result.cause();
				LOG.error(message, result.cause());
				startFuture.fail(message);
			}
		});

	}

	public String getVersion()
	{
		Class clazz = this.getClass();
		String className = clazz.getSimpleName() + ".class";
		String classPath = clazz.getResource(className).toString();
		if (!classPath.startsWith("jar")) // Class not from JAR
		{
			return "NA";
		}
		String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) + "/META-INF/MANIFEST.MF";

		InputStream is = null;
		try
		{
			is = new URL(manifestPath).openStream();
			Manifest manifest = new Manifest(is);
			Attributes attr = manifest.getMainAttributes();
			return attr.getValue("Manifest-Version");
		}
		catch (IOException e)
		{
			LOG.error("Error in getting version.", e);
		}
		finally
		{
			try
			{
				if (is != null) is.close();
			}
			catch (IOException e)
			{
				//Nothing to do
			}
		}
		return "NA";
	}
	
	public JsonObject getFirebaseConfig()
	{
        JsonObject config = (JsonObject) this.getConfigValue("firebase");
        if (config == null)
        {
            throw new RuntimeException("Could not find config with key 'firebase'");
        }
        return config;
	}
	
	public String getFirebaseObjVal(String key, String defaultValue)
	{
        JsonObject config = getFirebaseConfig();
        return config.getString(key,defaultValue);
    }

	
	public static void main(String[] args) {
		
		
	}
}
