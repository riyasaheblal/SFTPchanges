package com.cedge.sftp.util;

import com.jcraft.jsch.*;
import java.util.Properties;
import java.util.logging.Logger;

public class SFTPUtility {

	private static final Logger logger = Logger.getLogger(SFTPUtility.class.getName());
	private static final int MAX_RETRIES =Integer.parseInt(PropertyReader.getProperty(CommonConstants.MAX_RETRIES));
	private static final long RETRY_DELAY_MS =Long.parseLong(PropertyReader.getProperty(CommonConstants.RETRY_DELAY_MS)); // 2 seconds
	public static ChannelSftp connectToSFTPServer(String username, String ip, String password, int port) throws JSchException {
		for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
			try {
				JSch jsch = new JSch();
				Session session = jsch.getSession(username, ip, port);
				session.setPassword(password);
				Properties config = new Properties();
				config.put("StrictHostKeyChecking", "no");
				session.setConfig(config);

				logger.info("Connecting to SFTP server...");
				session.connect();

				Channel channel = session.openChannel("sftp");
				channel.connect();
				logger.info("Connected to SFTP server.");

				return (ChannelSftp) channel;
			}  catch (JSchException e) {
				if (attempt == MAX_RETRIES - 1) {
					throw e; // Rethrow exception if final attempt fails
				}
				try {
					Thread.sleep(RETRY_DELAY_MS);
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
				}
			}
		}
		return null; // This line will never be reached
	}

	public static void disconnectFromSFTPServer(ChannelSftp channelSftp) {
		if (channelSftp != null) {
			try {
				channelSftp.disconnect();
				channelSftp.getSession().disconnect();
				logger.info("Disconnected from SFTP server.");
			} catch (JSchException e) {
				logger.severe("Error while disconnecting from SFTP server: " + e.getMessage());
			}
		}
	}
}
