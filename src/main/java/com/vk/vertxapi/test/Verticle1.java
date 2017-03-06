package com.vk.vertxapi.test;

import com.vk.vertxapi.api.VertxInstance;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;

public class Verticle1 extends AbstractVerticle 
{
    @Override
    public void start(Future<Void> startFuture) throws Exception 
    {
    	System.out.println("Called start method");
    	this.receiveMessage(startFuture);
    }

    private void receiveMessage(Future<Void> startFuture) 
    {
    	EventBus eb = vertx.eventBus();
    	eb.localConsumer("msg",   message -> 
    	{
    		String msg = message.body().toString();
    		System.out.println("T1 = " + msg);
    		try {
				Thread.sleep(1000);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		message.reply("Received " + msg);
    	});
    	
    	eb.localConsumer("msg2",   message -> 
    	{
    		String msg = message.body().toString();
    		System.out.println("T2 " + msg);
    		try {
				Thread.sleep(1000);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		message.reply("Received " + msg);
    	});

	}


	@Override
    public void stop() throws Exception 
    {
        super.stop();
        System.out.println("Called stop method");
    }

    public static void main(String[] args) throws Exception
    {
    	VertxInstance.get().deployVerticle(Verticle1.class.getCanonicalName(), new DeploymentOptions().setWorker(false), result ->
    	{	
    		if ( result.succeeded())
    		{
    			System.out.println("Deloymed verticle");
    		}
    		else
    		{
    			System.err.println("Failed to deploy verticle");
    		}
    	});
    	
    	Thread.sleep(5000);
    	System.out.println("Ready to accept message");
    	
    	for (int i=0; i<10; i++)
    	{
        	VertxInstance.get().eventBus().send("msg", "Message"+i, res ->
        	{
        		if (res.failed()) System.err.println("failed");
        	});
        	
        	VertxInstance.get().eventBus().send("msg2", "Message"+i, res ->
        	{
        		if (res.failed()) System.err.println("failed");
        	});
    	}
	}
}
