package com.cedge.sftp.service;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cedge.sftp.util.CommonConstants;
import com.cedge.sftp.util.PropertyReader;
import com.cedge.sftp.util.SFTPUtility;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

public class FileService {

	static Logger logger = LoggerFactory.getLogger(FileService.class);
	private static final int THREAD_POOL_SIZE = Integer.parseInt(PropertyReader.getProperty(CommonConstants.THREAD_POOL));
	private static final String SERVER_DIR = PropertyReader.getProperty(CommonConstants.SERVER_PATH);
	private static final String LOCAL_DIR = PropertyReader.getProperty(CommonConstants.LOCAL_PATH);

	public static void download_UploadFile(LinkedHashMap<Integer, HashMap<String, String>> mapData, String bankcode,
			LinkedHashMap<String, String> serverInfo, Connection conn) throws JSchException {
		ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE); // Adjust thread pool size as needed
		try {
			for (Integer key : mapData.keySet()) {
				HashMap<String, String> data = mapData.get(key);
				String value = data.get("FLOW");

				Callable<Void> task = () -> {
					try  {
						ChannelSftp channelSftp = SFTPUtility.connectToSFTPServer(serverInfo.get("username"), serverInfo.get("ip"),
								serverInfo.get("pfms_password"), 22);
						if ("Download".equals(value)) {
							downloadFiles(mapData, bankcode, channelSftp, conn, data);
						} else {
							uploadFiles(mapData, bankcode, channelSftp, conn, data);
						}
					} catch (JSchException e) {
						logger.error("SFTP error: " + e.getMessage(), e);
					}
					return null;
				};

				executorService.submit(task);
			}
		} catch (Exception e) {
			logger.error("Exception occurred during file transfer: " + e.getMessage(), e);
		} finally {
			executorService.shutdown();
			// Wait for all tasks to complete or timeout
			try {
				if (!executorService.awaitTermination(60, java.util.concurrent.TimeUnit.SECONDS)) {
					executorService.shutdownNow();
				}
			} catch (InterruptedException ex) {
				executorService.shutdownNow();
				Thread.currentThread().interrupt();
			}
		}
	}

	//	public static void download_UploadFile(LinkedHashMap<Integer, HashMap<String, String>> mapData, String bankcode,
	//			LinkedHashMap<String, String> serverInfo, Connection conn) throws JSchException {
	//		ChannelSftp channelSftp = SFTPUtility.connectToSFTPServer(serverInfo.get("username"), serverInfo.get("ip"),
	//				serverInfo.get("pfms_password"), 22);
	//		ExecutorService executorService = Executors.newFixedThreadPool(20); // Adjust thread pool size as needed
	//		try {
	//
	//			if (channelSftp.isConnected()) {
	//				for (Integer key : mapData.keySet()) {
	//					HashMap<String, String> data1 = mapData.get(key);
	//					HashMap<String, String> data = mapData.get(key);
	//
	//					String value = data.get("FLOW");
	//					Callable<Void> task = () -> {
	//						if ("Download".equals(value)) {
	//							System.out.println(channelSftp.isConnected());
	//
	//							downloadFiles(mapData, bankcode, channelSftp, conn,data);
	//						} else {
	//							uploadFiles(mapData, bankcode, channelSftp, conn,data);
	//						}
	//						return null;
	//					};
	//
	//					executorService.submit(task);
	//				}
	//			}
	//		} catch (Exception e) {
	//			e.printStackTrace();
	//			logger.error("Exception occurred during file transfer: " + e.getMessage());
	//		} finally {
	//			SFTPUtility.disconnectFromSFTPServer(channelSftp);
	//			executorService.shutdown();
	//		}
	//	}
	//




	public static void downloadFiles(LinkedHashMap<Integer, HashMap<String, String>> map, String bankcode,
			ChannelSftp channelSftp, Connection conn,HashMap<String, String> data) {
		String value = data.get("FLOW");
		boolean	isDone=channelSftp.isConnected();

		if (!channelSftp.isConnected()) {
			logger.error("ChannelSftp is not connected for download");
			return;
		}
		try {
			//String path = data.get("source");
			//String home = "/pfms";
			String FromPath=SERVER_DIR+data.get("source");
			System.out.println(FromPath + " "+ isDone);
			channelSftp.cd(FromPath); // Change to the source directory
			String downloadPattern = bankcode + "*.xml";
			Vector<ChannelSftp.LsEntry> list = channelSftp.ls(downloadPattern);

			for (ChannelSftp.LsEntry oListItem : list) {
				String filemtime=oListItem.getAttrs().getAtimeString();
				SimpleDateFormat formatter = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy"); 
				//String Filetime=formatter.format(filemtime);
				Date date = new Date();  
				String systime=formatter.format(date);
				Date d1 = null;
				Date d2 = null;
				try {
					d1 = formatter.parse(filemtime);
					d2 = formatter.parse(systime);
				} catch (ParseException e) {
					e.printStackTrace();
				}    
				long diff = d2.getTime() - d1.getTime();        
				long diffMinutes = diff / (60 * 1000);
				if (diffMinutes>1){
					//String destinationFolder1 ="";
					//destinationFolder1 =data.get("destination");
					String destinationFolder=LOCAL_DIR+data.get("destination");
					File pathName = new File(destinationFolder);
					System.out.println(pathName.exists());
					if (!pathName.exists()) {
						pathName.mkdirs();}
					System.out.println("destinationFolder is:"+destinationFolder);

					channelSftp.lcd(destinationFolder);
					logger.debug("destinationFolder is:"+destinationFolder);


					// Download and process file
					if (oListItem.getFilename() != null && oListItem.getFilename().endsWith(".xml")
							&& !oListItem.getFilename().contains("Processed")) {
						channelSftp.get(oListItem.getFilename(), destinationFolder + "/" + oListItem.getFilename());
						logger.info("Downloaded file: " + oListItem.getFilename());
						channelSftp.rm(oListItem.getFilename()); // Remove file after downloading
						fileInformation(bankcode, oListItem.getFilename(), value, conn,FromPath,destinationFolder);
					}
				}}

		} catch (SftpException e) {
			logger.error("Error during file download: " + e.getMessage());
		}

	}

	public static void uploadFiles(LinkedHashMap<Integer, HashMap<String, String>> map, String bankcode,
			ChannelSftp channelSftp, Connection conn, HashMap<String, String> data) {
		String value = data.get("FLOW");
//		boolean	isDone=channelSftp.isConnected();
		if (!channelSftp.isConnected()) {
			logger.error("ChannelSftp is not connected for upload");
			return;
		}
		try {
			//	String home = "/pfms";
			//	String source = "D:"+ data.get("source");
			String FromPath=LOCAL_DIR+data.get("source");
			String destinationPath = SERVER_DIR+data.get("destination");

			File folder = new File(FromPath);
			FileFilter fileFilter = f -> !f.isDirectory() && f.getName().startsWith(bankcode)
					&& (f.getName().endsWith(".xml") || f.getName().contains(".XML"));
			File[] files = folder.listFiles(fileFilter);

			if (files != null) {
				for (File file : files) {
					long diffMinutes = calculateTimeDifference(file);

					if (diffMinutes > 1) {
						channelSftp.cd(destinationPath);
						try (FileInputStream fis = new FileInputStream(file)) {
							channelSftp.put(fis, file.getName());
						} catch (IOException e) {
							logger.error("Error during file upload: " + e.getMessage());
						}

						// Move file to archive
						String destinationArchivePath =LOCAL_DIR+ data.get("destination_archieve");
						File dir = new File(destinationArchivePath);
						if (!dir.exists()) {
							dir.mkdirs();
						}
						boolean success = file.renameTo(new File(dir, file.getName()));
						if (!success) {
							moveFileToArchive(file, dir);
						}

						fileInformation(bankcode, file.getName(), value, conn,FromPath,destinationPath);
					}
				}
			}

		} catch (Exception e) {
			logger.error("Error during file upload: " + e.getMessage());
		}
		
	}

	private static long calculateTimeDifference(File file) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
		String fileTime = sdf.format(file.lastModified());
		Date date = new Date();
		String systemTime = sdf.format(date);

		Date d1 = sdf.parse(fileTime);
		Date d2 = sdf.parse(systemTime);

		long diff = d2.getTime() - d1.getTime();
		return diff / (60 * 1000);
	}

	private static void moveFileToArchive(File file, File dir) {
		try {
			String command = "mv " + file.toString() + " " + dir + "/" + file.getName();
			Runtime.getRuntime().exec(command);
			logger.info("File moved to archive: " + file.getName());
		} catch (IOException e) {
			logger.error("Error moving file to archive: " + e.getMessage());
		}
	}

	public static boolean fileInformation(String bankCode, String filename, String value, Connection conn,String frompath,String topath) {
		boolean isDone = false;
		String query = "INSERT INTO PSFTPFileInfo1(REQ_RES, DWN_UPLOAD, bankcode, dwn_creatdate,FROMPATH,TOPATH) VALUES (?, ?, ?, systimestamp,?,?)";

		try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {

			if (filename.toLowerCase().endsWith(".xml")) {
				preparedStatement.setString(1, filename);
				preparedStatement.setString(2, value);
				preparedStatement.setString(3, bankCode);
				preparedStatement.setString(4, frompath);
				preparedStatement.setString(5, topath);

				//logger.info("Executing Query: " + preparedStatement);
				preparedStatement.executeUpdate();
				isDone = true;
			}
		} catch (SQLException e) {
			logger.error("Error while inserting data into PSFTPFileInfo for bank code: " + bankCode, e);
		}

		return isDone;
	}

}
