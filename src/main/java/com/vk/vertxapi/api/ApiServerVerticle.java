package com.vk.vertxapi.api;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.vk.vertxapi.api.handler.TestHandler;
import com.vk.vertxapi.config.ConfigVerticle;
import com.vk.vertxapi.files.FileDownloadHandler;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.JksOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.AuthHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;

public class ApiServerVerticle extends AbstractVerticle 
{
    private final static Logger LOG = LogManager.getLogger(ApiServerVerticle.class);

    @Override
    public void start(Future<Void> startFuture) throws Exception 
    {
    		LOG.info("Starting api server ...");
        Router router = Router.router(vertx);

        boolean allowCrossOrigin = ConfigVerticle.getInstance().getBoolean("allowcrossorigin", false);
        if ( allowCrossOrigin )
        {
            router.route().handler(CorsHandler.create("*")
          	      .allowedMethod(HttpMethod.GET)
          	      .allowedMethod(HttpMethod.POST)
          	      .allowedMethod(HttpMethod.OPTIONS)
          	      .allowedHeader("Authorization")
          	      .allowedHeader("Content-Type"));
        }

        this.setupPingHandler(router);
        this.setupApiHandler(router);
        this.setupEventBusHandler(router);

        int apiPort = ConfigVerticle.getInstance().getInteger("api_port", 1442);
        
        boolean useSSL = ConfigVerticle.getInstance().getBoolean("usessl", false);
        if ( useSSL )
        {
        		LOG.info("HTTPS enbaled webserver");
            String ssl_keystore = ConfigVerticle.getInstance().getStringValue("ssl_keystore", "ssl/apiserver.jks");
            String ssl_password = ConfigVerticle.getInstance().getStringValue("ssl_password", "Apiserver123#");
            HttpServerOptions options = new HttpServerOptions()
                    .setKeyStoreOptions(new JksOptions().
                            setPath(ssl_keystore).
                            setPassword(ssl_password))
                    .setTrustStoreOptions(new JksOptions().
                            setPath(ssl_keystore).
                            setPassword(ssl_password))
                    .setSsl(true)
                    .setCompressionSupported(true)
                    .setTcpKeepAlive(true);
            vertx.createHttpServer(options).requestHandler(router::accept).listen(apiPort);
        }
        else
        {
        	LOG.info("HTTP enbaled webserver");
            vertx.createHttpServer().requestHandler(router::accept).listen(apiPort);
        }
        startFuture.complete();
        LOG.info("clms server started.");
    }

    private void setupPingHandler(Router router) 
    {
    		router.mountSubRouter("/ping", new PingHandler(vertx));
    }

    private void setupApiHandler(Router router) 
    {
    	boolean secureApi = ConfigVerticle.getInstance().getBoolean("secure_api", false);
    	if ( secureApi )
    	{
        	AuthHandler authHandler = JWTAuthHandler.create(new AuthTokenVerifier());
        	router.route("/api/*").handler(authHandler);
    	}
    	new ConfiguredRestApiHandler().setup(router);
    	router.mountSubRouter("/file/", new FileDownloadHandler(vertx));
    	router.mountSubRouter("/mockjson/", new MockRestApiHandler(vertx));
    	
    	router.mountSubRouter("/test/", new TestHandler(vertx));
    }

    private void setupEventBusHandler(Router router) 
    {
        SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
        PermittedOptions inbound = new PermittedOptions().setAddress("*");
        PermittedOptions outbound = new PermittedOptions().setAddress("*");
        BridgeOptions options = new BridgeOptions().addInboundPermitted(inbound).addOutboundPermitted(outbound);
        sockJSHandler.bridge(options, be -> {
        });
        router.route("/apibus/*").handler(sockJSHandler);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }

    public static void main(String[] args) {
    }

}
