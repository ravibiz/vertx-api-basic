package com.vk.vertxapi.api;

import io.vertx.core.Vertx;

public class VertxInstance
{
    public static final Vertx vertx = Vertx.vertx();

    public static Vertx get()
    {
        return vertx;
    }
}
