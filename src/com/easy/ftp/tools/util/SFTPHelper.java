package com.easy.ftp.tools.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.eclipse.core.resources.IFile;

import com.easy.ftp.tools.enums.ConfigServer;

public class SFTPHelper {

	/**
	 * 用户名
	 */
	private static String user;
	/**
	 * 登录密码
	 */
	private static String passwd;
	/**
	 * 主机IP
	 */
	private static String host;

	/**
	 * 主机端口
	 */
	private static int port;

	public static SFTPServer server() {

		return new SFTPServer(host, port, user, passwd);
	}

	public static void upload(String localFile, String remoteFile, int closeFlag) throws Exception {

		SFTPServer server = SFTPHelper.server();

		// open
		server.Open();
		// upload
		server.Upload(localFile, remoteFile);

		server.Close();
	}

	public static void init(IFile ifile) throws IOException {
		init(ifile.getProject().getFile("ftp.properties").getLocation().toFile());
	}

	public static void init(File file) throws IOException {
		Properties properties = getProperties(file);
		host = properties.getProperty("ssh.host");
		port = Integer.valueOf(properties.getProperty("ssh.port"));
		user = properties.getProperty("ssh.user");
		passwd = properties.getProperty("ssh.password");
		ConfigServer.INIT.initValue(host, port + "", user, passwd, properties.getProperty("app.name"));
	}

	public static Properties getProperties(File file) throws IOException {

		Properties properites = new Properties();

		properites.load(new FileInputStream(file));

		return properites;
	}

}
