package com.easy.ftp.tools.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpProgressMonitor;

public class SFTPServer {
	/**
	 * 用户名
	 */
	private String user;
	/**
	 * 登录密码
	 */
	private String passwd;
	/**
	 * 主机IP
	 */
	private String host;
	/**
	 * 端口
	 */
	private int port;
	private JSch jsch;
	private Session session;

	/**
	 * 首次连接主机是否进行公钥确认
	 */
	private boolean StrictHostKeyChecking = false;

	/**
	 * 连接超时设置，默认0为使用默认的超时设置
	 */
	private int Timeout = 0;

	/**
	 * SFTP Session超时设置，默认0为使用默认的超时设置
	 */
	private int SessionTimeout = 0;

	public SFTPServer(String host, int port, String user, String passwd) {
		this.user = user;
		this.passwd = passwd;
		this.host = host;
		this.port = port;
		this.jsch = new JSch();
	}

	/**
	 * 打开服务器连接
	 * 
	 * @return 是否连接成功
	 * @throws Exception
	 */
	public void Open() throws Exception {
		if (session == null || !session.isConnected()) {

 			session = jsch.getSession(user, host, port);
			session.setPassword(passwd);
 			if (!StrictHostKeyChecking) {
 				java.util.Properties config = new java.util.Properties();
				config.put("StrictHostKeyChecking", "no");
				session.setConfig(config);
			}
			session.setTimeout(SessionTimeout);
		}  
		if (Timeout == 0) {
			session.connect();
		} else {
			session.connect(Timeout);
		}

	}

	/**
	 * 关闭服务器连接
	 */
	public void Close() {
		if (session != null && session.isConnected()) {
			session.disconnect();
		}
	}

	/**
	 * 获取JSch ChannelSftp 用于操作原生JSch Sftp方法
	 * 
	 * @return
	 * @throws Exception
	 */
	public ChannelSftp ShareSftp() throws Exception {
		ChannelSftp channel = null;
		channel = (ChannelSftp) session.openChannel("sftp");
		return channel;
	}

	/**
	 * 上传本地文件到指定服务器路径
	 * 
	 * @param localFile  本地文件路径
	 * @param remoteFile 服务器文件路径
	 * @return
	 * @throws Exception
	 */
	public boolean Upload(String localFile, String remoteFile) throws Exception {
		return Upload(localFile, remoteFile, null);
	}

	public boolean Cd(ChannelSftp channel, String remotePath) {
		try {
			channel.cd(remotePath);
			return true;
		} catch (Exception e) {
			return false;
		}

	}

	/**
	 * 上传本地文件到指定服务器路径
	 * 
	 * @param localFile       本地文件路径
	 * @param remoteFile      服务器文件路径
	 * @param progressMonitor
	 * @return
	 * @throws Exception
	 */
	public boolean Upload(String localFile, String remote, SftpProgressMonitor progressMonitor) throws Exception {
		ChannelSftp channel = null;
		InputStream inputStream = null;
		try {
			channel = (ChannelSftp) session.openChannel("sftp");
			if (Timeout == 0) {
				channel.connect();
			} else {
				channel.connect(Timeout);
			}

			inputStream = new FileInputStream(new File(localFile));

			channel.setInputStream(inputStream);

			File remoteFile = new File(remote);

			String remoteParentPath = remoteFile.getParent().replace("\\", "/");

			boolean ok = Cd(channel, remoteParentPath);

			if (!ok) {
				channel.mkdir(remoteParentPath);
				Cd(channel, remoteParentPath);
			}
			if (progressMonitor != null) {
				channel.put(inputStream, remoteFile.getName(), progressMonitor, ChannelSftp.OVERWRITE);
			} else {

				channel.put(inputStream, remoteFile.getName(), ChannelSftp.OVERWRITE);
			}

			channel.disconnect();
			inputStream.close();
			remoteFile = null;
			return true;
		} catch (Exception e) {
			if (channel != null && channel.isConnected()) {
				channel.disconnect();
			}
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (Exception e1) {

				}
			}
 			throw e;
		}
	}

	public static void main(String[] args) throws Exception {

		SFTPServer server = new SFTPServer("172.31.205.171", 22, "mfcsdbpg", "mfcsdbpg");

		server.Open();

		ChannelSftp channel = null;
		channel = (ChannelSftp) server.session.openChannel("sftp");
		String localFile = "E:\\java\\shiji\\bank-interface\\src\\mrbpg_recon\\src\\main\\java\\com\\minpay\\reconciliation\\App.java";

		server.Upload(localFile, "app/recon/classes/com/minpay/reconciliation/a/App.java");
		channel.quit();
		server.Close();
	}

}
