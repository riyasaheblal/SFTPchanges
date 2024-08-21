package com.cedge.sftp.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.JSchException;

public class MiddleService {

	static Logger logger = LoggerFactory.getLogger(MiddleService.class);

	public static void processFunction2(String bankcode, Connection connection) throws JSchException {

		logger.info("============Start processFunction2==========================");
		LinkedHashMap<Integer, HashMap<String, String>> mapData = fetchCommonData(bankcode, connection);
		LinkedHashMap<String, String> serverInfo = serverDetails(bankcode, connection);
		if (serverInfo != null && mapData != null && serverInfo.size() > 0 && mapData.size() > 0) {

			FileService.download_UploadFile(mapData, bankcode, serverInfo, connection);

		}
		logger.info("============END processFunction2==========================");

	}

	public static LinkedHashMap<Integer, HashMap<String, String>> fetchCommonData(String bankcode, Connection conn) {
		LinkedHashMap<Integer, HashMap<String, String>> map = new LinkedHashMap<>();

		String query = "select PFMS_SFTP_PATH, CEDGE_SFTP_PATH, RTRIM(TRIM(PFMS_SFTP_PATH),'/') || 'Archive/' AS PFMS_SFTP_PATH_ARCH, FLOW from APP_SFTP_PATH";
		logger.info("fetchCommonData SQL: " + query);
		PreparedStatement preparedStatement = null;
		ResultSet resultSet=null;
		try  {
			preparedStatement = conn.prepareStatement(query);
			 resultSet = preparedStatement.executeQuery();
			int count = 0;
			while (resultSet.next()) {
				count++;
				LinkedHashMap<String, String> map1 = new LinkedHashMap<>();
				map1.put("source", resultSet.getString("PFMS_SFTP_PATH"));
				map1.put("destination", resultSet.getString("CEDGE_SFTP_PATH"));
				map1.put("destination_archieve", resultSet.getString("PFMS_SFTP_PATH_ARCH"));
				map1.put("FLOW", resultSet.getString("FLOW"));
				map.put(count, map1);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}finally
		{
			try
			{
				if(preparedStatement!=null)
					preparedStatement.close();
				if(resultSet!=null)
					resultSet.close();

			}catch(Exception e)
			{

			}
		}

		return map;
	}

	public static LinkedHashMap<String, String> serverDetails(String bankcode, Connection connection3) {
		LinkedHashMap<String, String> serverInfo = new LinkedHashMap<>();

		String query = "select PFMSFTPIP, PFMSFTPUSER, PFMSFTPPWD from all_banks where bankcode = ?";
		logger.info("server_details SQL: " + query);

		try (PreparedStatement preparedStatement = connection3.prepareStatement(query)) {
			preparedStatement.setString(1, bankcode);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
					serverInfo.put("ip", resultSet.getString("PFMSFTPIP"));
					serverInfo.put("username", resultSet.getString("PFMSFTPUSER"));
					serverInfo.put("pfms_password", resultSet.getString("PFMSFTPPWD"));
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return serverInfo;
	}

}
