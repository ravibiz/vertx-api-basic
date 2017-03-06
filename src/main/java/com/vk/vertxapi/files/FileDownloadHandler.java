package com.vk.vertxapi.files;

import java.io.File;
import java.util.Map;

import javax.activation.MimetypesFileTypeMap;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.vk.vertxapi.api.AbstractRouteHandler;
import com.vk.vertxapi.config.ConfigVerticle;
import com.vk.vertxapi.message.MessageVerticle;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class FileDownloadHandler extends AbstractRouteHandler 
{

	private final static Logger LOG = LogManager.getLogger(FileDownloadHandler.class);
	
    public FileDownloadHandler(Vertx vertx) 
    {
        super(vertx);
        this.get("/filecheck/:token/:fileName").handler(this::checkFileExist);
		this.get("/download/:token/:fileName").handler(this::download);
    }

    private void checkFileExist(RoutingContext context)
    {
    	try
    	{
    		LOG.info("Checking file resource for download");
    		String token = context.request().getParam("token");
			JsonObject userTokenPayloadJson = verifyToken(token);
			
			if (null == userTokenPayloadJson )
			{
				sendError(context, "Unauthorized access!!");
				return;
			}
			 		
			String downloadPrefix = ConfigVerticle.getInstance().getConfigValue("resourceDownloadPath").toString();
	    	LOG.info(downloadPrefix);
	    	
	    	String filename = context.request().getParam("filename");
	    	
	    	File file = new File(downloadPrefix + "/" + filename);
	    	LOG.info("File to download(s) " + file.length());
	    	if(file.length() == 0){
	    		sendError(context, "File does not exist !!");
	    		return;
	    	}
        	sendSuccess(context, "Request file exist");
    	}
    	catch(Exception e)
    	{
    		LOG.error("Error in processing file check request");
    		e.printStackTrace();
    		sendError(context, MessageVerticle.getInstance().getMessage(Messages.INTERNAL_ERROR));
    	}
    }
    
    private void download(RoutingContext context) 
    {
    	try
    	{
    		LOG.info("Download Begins......");
    		String token = context.request().getParam("token");
			JsonObject userTokenPayloadJson = verifyToken(token);
			
			if (null == userTokenPayloadJson )
			{
				sendError(context, "Unauthorized access!!");
				return;
			}
			 		
			String downloadPrefix = ConfigVerticle.getInstance().getConfigValue("resourceDownloadPath").toString();
	    	LOG.info(downloadPrefix);
	    	
	    	String filename = context.request().getParam("filename");
	    	
	    	File file = new File(downloadPrefix + "/" + filename);
	    	LOG.info("File to download(s) " + file.length());
	    	if(file.length() == 0){
	    		sendError(context, "File does not exist !!");
	    		return;
	    	}

	    	HttpServerResponse response =  context.response();
	    	response.putHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
	    	response.putHeader("Access-Control-Allow-Origin", "*");
	    	response.putHeader("Access-Control-Allow-Headers", "content-type, downladfilename");
	    	response.putHeader("Access-Control-Expose-Headers", "content-type, downladfilename");
	    	
	    	response.putHeader("Content-Length", Long.toString(file.length()));
	    	response.putHeader("Content-Disposition", "filename=\"" + filename + "\";");
	    	response.putHeader("downladfilename", file.getName());
	    	response.setChunked(true);
	    	
	    	MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
	    	String mimeType = mimeTypesMap.getContentType(file);
	    	LOG.info("File MimeType : " + mimeType);
	    	response.putHeader("content-type", mimeType);
			
	    	response.sendFile(file.getAbsolutePath());
			LOG.info("Successfully written file");

    	}
    	catch(Exception e)
    	{
    		LOG.error("Error in processing download request");
    		e.printStackTrace();
    		sendError(context, MessageVerticle.getInstance().getMessage(Messages.INTERNAL_ERROR));
    	}
    }

	private JsonObject verifyToken(String token)
	{
        try 
        {
        	String fbSecret = ConfigVerticle.getInstance().getFirebaseObjVal("fb_secret", "IamHeret05vEYOU!!!");
        	
			Claims claims = Jwts.parser().setSigningKey(fbSecret.getBytes("UTF-8")).parseClaimsJws(token).getBody();
			Map<String, Object> payload = (Map<String, Object>) claims.get("d");
			JsonObject payloadJson = new JsonObject(payload);
			String uid = payloadJson.getString("uid");
			
			String role = payloadJson.getString("role");
			if ( null == role || role.trim().length() == 0) return null;
			
			LOG.info("Uid: " + uid + " Token is valid ") ;
			return payloadJson;
		} 
        catch (Exception e) 
        {
        	e.printStackTrace();
        	return null;
		} 
	}
	
	private enum Messages
	{
		INTERNAL_ERROR
	}

}
