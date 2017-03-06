package com.vk.vertxapi.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.io.Files;


public class CSVWriter 
{
	

    public static String getCSV(List<Map<String, String>> flatJson, char separator) 
    {
        Set<String> headers = getHeaders(flatJson);
        String csvString = StringUtils.arrayToString(StringUtils.toStringArray(headers.toArray()), separator) + "\n";

        for (Map<String, String> map : flatJson) 
        {
            csvString = csvString + getSeperatedColumns(headers, map, separator) + "\n";
        }

        return csvString;
    }
    
	public static String getSeperatedColumns(Set<String> headers, 
			Map<String, String> map, char separator) 
	{
        List<String> items = new ArrayList<String>();
        for (String header : headers) 
        {
            String value = map.get(header) == null ? "" : map.get(header);
            
            value = value.contains(separator + "") ? "\"" + value + "\"" : value;
            items.add(value);
        }
        return StringUtils.arrayToString(StringUtils.toStringArray(items.toArray()), separator);
    }

	
	public static String[] getSeperatedColumnsArray(Set<String> headers, 
			Map<String, String> map, char separator) 
	{
        List<String> items = new ArrayList<String>();
        for (String header : headers) 
        {
            String value = map.get(header) == null ? "" : map.get(header);
            
            value = value.contains(separator + "") ? "\"" + value + "\"" : value;
            items.add(value);
        }
        return StringUtils.toStringArray(items.toArray());
    }
	
	public static Set<String> getHeaders(List<Map<String, String>> flatJson) 
	{
        Set<String> headers = new LinkedHashSet();

        for (Map<String, String> map : flatJson) {
            headers.addAll(map.keySet());
        }

        return headers;
    }
	
	public static void main(String ... args)
	{
		String outputJson = "[{\"Client Name\":\"Codepro,it\",\"Team Name\":\"Team Alpha\",\"AsOfDate\":\"2016-12-06\",\"Manager User Name\":\"shilpa@jigsawacademy.com\",\"Manager Name\":\"Shilpa G\",\"Manager Designation\":\"Manager\",\"Learner User name\":\"shilpa.sorab@gmail.com\",\"Learner Name\":\"Shilpa Naik\",\"Learner Designation\":\"System Analyst\",\"Learner Rank\":null,\"Learning Path Assigned\":\"Data Science and Storytelling LP\",\"Learning Path Expected Start Date\":\"2016-12-06T05:00:00Z\",\"Learning Path Expected End Date\":\"2017-06-05T03:59:59Z\",\"Learning Path Actual Start Date\":null,\"Learning Path Actual End Date\":null,\"Learning Path Duration\":2073.24,\"Total No of Proficiency\":3,\"No of Proficiency Completed\":0,\"No of attempts taken to complete Test\":0,\"Profciency Score\":null,\"Is Requested For Proficiency Retake\":null,\"Learner Status\":\"Not Started\",\"Actual Progress(cumulative)\":0,\"Expected Progress(cumulative)\":0,\"Actual Planned Hrs(cumulative)\":0,\"Actual Planned Hrs Per Week\":0,\"Expected Planned Hrs Per Week\":0,\"Expected Hrs to be spend on System Including Backlog\":0,\"Points Earned\":0,\"Total Time Spent\":0,\"Total Videos Watched\":null,\"Week Number\":49}]";
		String dd = CSVWriter.getCSV(JSONFlattener.parseJson(outputJson), ',');
		System.out.println(dd);
		
		try {
			Files.write(dd.getBytes(), new File("new.csv"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
