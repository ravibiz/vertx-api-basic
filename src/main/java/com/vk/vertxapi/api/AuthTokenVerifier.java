package com.vk.vertxapi.api;

import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.vk.vertxapi.config.ConfigVerticle;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTOptions;

public class AuthTokenVerifier implements JWTAuth 
{

	private final static Logger LOG = LogManager.getLogger(AuthTokenVerifier.class);
	
	@Override
	public void authenticate(JsonObject jsonObj, Handler<AsyncResult<io.vertx.ext.auth.User>> handler) 
	{
		String token = jsonObj.getString("jwt");
        LOG.info("Firebase Token : " + token);
        
        try 
        {
        	String fbSecret = ConfigVerticle.getInstance().getFirebaseObjVal("fb_secret", "IamHeret05vEYOU!!!");
        	
			Claims claims = Jwts.parser().setSigningKey(fbSecret.getBytes("UTF-8")).parseClaimsJws(token).getBody();
			Map<String, Object> payload = (Map<String, Object>) claims.get("d");
			JsonObject payloadJson = new JsonObject(payload);
			String uid = payloadJson.getString("uid");
			
			String role = payloadJson.getString("role");
			if ( null == role || role.trim().length() == 0) handler.handle(Future.failedFuture("Invalid Token, please login again"));
			LOG.info("Uid: " + uid + " Token is valid") ;
			AuthUser user = new AuthUser(payloadJson); 
			handler.handle(Future.succeededFuture(user));
		} 
        catch (Exception e) 
        {
        	e.printStackTrace();
			handler.handle(Future.failedFuture("Unauthozied access"));
		} 
	}

	@Override
	public String generateToken(JsonObject arg0, JWTOptions arg1) 
	{
		return null;
	}
}
