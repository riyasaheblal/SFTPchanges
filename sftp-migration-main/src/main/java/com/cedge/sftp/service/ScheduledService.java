package com.cedge.sftp.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.cedge.sftp.config.DatabaseConfig;
import com.cedge.sftp.util.DbConnConfig;
import com.jcraft.jsch.JSchException;

@Component
public class ScheduledService {
	
	@Autowired
	DatabaseConfig dbconfig;
	
	Logger logger = LoggerFactory.getLogger(ScheduledService.class);
	@Scheduled(initialDelay = 1000, fixedRate = 10000)
    public void performTask() throws JSchException {
		System.out.println("hey");
		List<String> bankCodes = new ArrayList<String>();
		String sql = "SELECT DISTINCT bankcode FROM all_banks WHERE PSFTPFLMVFLG='Y'";
        
        try (Connection conn = DbConnConfig.getConnection(dbconfig);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                bankCodes.add(rs.getString("bankcode"));
            }
            for (String string : bankCodes) {
            	System.out.println("--------------------------------"+string+"--------------------------------");
            	MiddleService.processFunction2(string, conn);
            	
            	// Wait for 1 minute before processing the next bank code
                try {
                    Thread.sleep(10000); // 30,000 milliseconds = 30 seconds
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Restore interrupted status
                    e.printStackTrace();
                }
    		}      
        } catch (SQLException e) {
            e.printStackTrace();
       }		
    }
	
}
