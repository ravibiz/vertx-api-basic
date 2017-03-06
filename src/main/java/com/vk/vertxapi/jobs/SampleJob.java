package com.vk.vertxapi.jobs;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class SampleJob implements org.quartz.Job 
{
	private final static Logger LOG = LogManager.getLogger(SampleJob.class);

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException 
	{
		LOG.info("Running job " + SampleJob.class);
		try 
		{
			LOG.info("Successfully Run job " + SampleJob.class);
		} 
		catch (Exception e) 
		{
			LOG.error("Error in running job " + SampleJob.class);
			e.printStackTrace();
		}
	}
}
