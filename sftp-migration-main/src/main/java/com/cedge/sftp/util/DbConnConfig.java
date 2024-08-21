package com.cedge.sftp.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.cedge.sftp.config.DatabaseConfig;

@Component
public class DbConnConfig {
	
	private final DatabaseConfig dbConf;
	
	public DbConnConfig(DatabaseConfig dbConf) {
		this.dbConf = dbConf;
	}
	
	private static final Logger logger = LogManager.getLogger(DbConnConfig.class);
	
	public static Connection getConnection(DatabaseConfig dbConf) {
		 try {
		        Connection connection = DriverManager.getConnection(dbConf.getDatabaseServerForAllApp(), 
		        		dbConf.getUsernameForAllApp(), 
		        		dbConf.getPasswordForAllApp());
		        Class.forName(dbConf.getDriver());
		        return connection;
		    } catch (SQLException e) {
		    	logger.error("DbConfig.getDBConnection() SqlException "+e);
		    	return null;
		    } catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
	}

}
