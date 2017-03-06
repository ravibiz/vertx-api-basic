package com.vk.vertxapi.api;

import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.vk.vertxapi.config.ConfigVerticle;
import com.vk.vertxapi.config.ServiceDef;
import com.vk.vertxapi.config.ServicesConfig;
import com.vk.vertxapi.util.StringUtils;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;

public class ServerVerticle extends AbstractVerticle
{
    private static final Logger LOG = LogManager.getLogger(ServerVerticle.class);

    private List<String> configFiles;

    public static void main(String[] args)
    {
        if (args.length == 1)
        {
            new ServerVerticle(args[0]).startServerVerticle();
        }
        else
        {
            new ServerVerticle().startServerVerticle();
        }
    }

    public ServerVerticle()
    {
        this(Collections.EMPTY_LIST);
        LOG.info(">> No parameters");
    }

    public ServerVerticle(String configFilesAsText)
    {
    	this(StringUtils.fastSplit(configFilesAsText, ','));
    	LOG.info(">> Configfileastext " + configFilesAsText);
    }

    public ServerVerticle(List<String> configFiles)
    {
        this.configFiles = configFiles;
    }

    private void startServerVerticle()
    {
    	
        VertxInstance.get().deployVerticle(this, new DeploymentOptions().setWorker(true), result ->
        {
            if (result.succeeded())
            {
                LOG.info("Server started.");
            }
            else
            {
                LOG.error("Server did not start. Message:" + result.result(), result.cause());
                System.exit(1);
            }
        });
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception
    {
        this.startConfigService(startFuture);
    }

    private void startConfigService(Future<Void> startFuture)
    {
    	LOG.info("Config files " + this.configFiles.toString());
        VertxInstance.get().deployVerticle(new ConfigVerticle(this.configFiles), new DeploymentOptions().setWorker(true), result ->
        {
            if (result.succeeded())
            {
                LOG.info("Config Service started.");
                ServicesConfig services = ConfigVerticle.getInstance().getServices();
                this.startParallelAfterSequential(startFuture, services.getSequential(), services.getParallel());
            }
            else
            {
                LOG.error("Server did not start. Message:" + result.result(), result.cause());
                System.exit(1);
            }
        });
    }

    private void startParallelAfterSequential(Future<Void> startFuture, final Deque<ServiceDef> sequentialServices,
                                              List<ServiceDef> parallelServices)
    {
        if (sequentialServices.isEmpty())
        {
            this.startParallelServices(startFuture, parallelServices);
        }
        else
        {
            ServiceDef serviceDef = sequentialServices.pop();
            Class serviceClass = serviceDef.getType();
            LOG.info(serviceClass.getName() + " starting ...");
            VertxInstance.get().deployVerticle(serviceClass.getCanonicalName(), serviceDef.getDeploymentOptions(), result ->
            {
                if (result.succeeded())
                {
                    LOG.info(serviceClass.getName() + " started.");
                    this.startParallelAfterSequential(startFuture, sequentialServices, parallelServices);
                }
                else
                {
                    startFuture.fail(result.cause());
                }
            });
        }

    }

    private void startParallelServices(Future<Void> startFuture, List<ServiceDef> servicesToStart)
    {

        Set<String> serviceNames = new HashSet<String>();

        for (ServiceDef serviceDef : servicesToStart)
        {
            Class serviceClass = serviceDef.getType();
            String serviceName = serviceClass.getCanonicalName();
            serviceNames.add(serviceName);
            LOG.info(serviceClass.getName() + " starting ...");

            VertxInstance.get().deployVerticle(serviceName, serviceDef.getDeploymentOptions(), result ->
            {
                this.handleStartResponse(serviceNames, serviceName, result);
            });
        }

        vertx.setTimer(20000, id -> {

            if (!serviceNames.isEmpty())
            {
                StringBuilder sb = new StringBuilder("Services not started:");
                for (String serviceName : serviceNames)
                {
                    sb.append(serviceName).append(":");
                }
                LOG.info(sb.toString());
                startFuture.fail(sb.toString());
            }
            else
            {
                LOG.info("All services started.");
                startFuture.complete();
            }
        });
    }

    private void handleStartResponse(Set<String> serviceNames, String serviceName, AsyncResult<String> result)
    {
        if (result.succeeded())
        {
            LOG.info(serviceName + " started.");
            serviceNames.remove(serviceName);
        }
        else
        {
            LOG.error(serviceName + " did not start.", result.cause());
        }
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception
    {
        super.stop(stopFuture);
    }

}
