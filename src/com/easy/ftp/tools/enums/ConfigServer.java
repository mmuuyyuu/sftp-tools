package com.easy.ftp.tools.enums;


public enum ConfigServer {

	INIT(""), HOST(""), PORT(""), USER(""), PASSWORD(""), APP_NAME("");

	String v;

	private ConfigServer(String value) {
		this.v = value;
	}

	public String getV() {
		return v;
	}

	public void setV(String v) {
		this.v = v;
	}

	public void initValue(String host, String port, String user, String password, String appName) {
		HOST.setV(host);
		PORT.setV(port);
		USER.setV(user);
		PASSWORD.setV(password);
		APP_NAME.setV(appName);

	}
	
 

}
