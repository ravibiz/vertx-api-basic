package com.vk.vertxapi.util;

import com.google.inject.AbstractModule;
import com.vk.vertxapi.email.template.VelocityTemplateProcessor;


public class GuiceBinder extends AbstractModule
{
    @Override
    protected void configure()
    {
        bind(VelocityTemplateProcessor.class).to(VelocityTemplateProcessor.class).asEagerSingleton();
    }
}
