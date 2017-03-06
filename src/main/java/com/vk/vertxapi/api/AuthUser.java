package com.vk.vertxapi.api;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AbstractUser;
import io.vertx.ext.auth.AuthProvider;

public class AuthUser extends AbstractUser {

	private JsonObject principal;
	
	public AuthUser(JsonObject principal) {
		this.principal = principal;
	}
	
	@Override
	public JsonObject principal() {
		// TODO Auto-generated method stub
		return principal;
	}

	@Override
	public void setAuthProvider(AuthProvider authProvider) {
		// TODO Auto-generated method stub
	}

	@Override
	protected void doIsPermitted(String permission, Handler<AsyncResult<Boolean>> resultHandler) {
		// TODO Auto-generated method stub
	}

}
