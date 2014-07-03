package org.wso2.mobile.idp;

public class OauthEndPoints {
	private String transportProtocol = "http://";
	private String authorizeURL;
	private String accessTokenURL;
	private String redirectURL;
	private static OauthEndPoints oauthEndPoints;
	
	private OauthEndPoints(){
		
	}	
	public static OauthEndPoints getInstance(){
		if(oauthEndPoints==null){
			oauthEndPoints = new OauthEndPoints();
		}
		return oauthEndPoints; 
	}
	public String getAuthorizeURL() {
		return authorizeURL;
	}
	public String getAccessTokenURL() {
		return accessTokenURL;
	}
	public String getRedirectURL() {
		return redirectURL;
	}
	public void setRedirectURL(String redirectURL) {
		this.redirectURL = redirectURL;
	}
	public void setEndPointURLs(String host, String port) {
		this.authorizeURL = transportProtocol+host+":"+port+"/oauth2/authorize";
		this.accessTokenURL = transportProtocol+host+":"+port+"/oauth2/token";
	}
}
