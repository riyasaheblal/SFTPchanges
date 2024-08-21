package com.cedge.sftp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

import lombok.Data;

@Configuration
@PropertySources({
    @PropertySource("classpath:database.properties")
})
public class DatabaseConfig {

	@Value("${DatabaseServerForAllApp}")
	private String databaseServerForAllApp;
	@Value("${UsernameForAllApp}")
	private String usernameForAllApp;
	@Value("${PasswordForAllApp}")
	private String passwordForAllApp;
	@Value("${Driver}")
	private String driver;
	public String getDatabaseServerForAllApp() {
		return databaseServerForAllApp;
	}
	public void setDatabaseServerForAllApp(String databaseServerForAllApp) {
		this.databaseServerForAllApp = databaseServerForAllApp;
	}
	public String getUsernameForAllApp() {
		return usernameForAllApp;
	}
	public void setUsernameForAllApp(String usernameForAllApp) {
		this.usernameForAllApp = usernameForAllApp;
	}
	public String getPasswordForAllApp() {
		return passwordForAllApp;
	}
	public void setPasswordForAllApp(String passwordForAllApp) {
		this.passwordForAllApp = passwordForAllApp;
	}
	public String getDriver() {
		return driver;
	}
	public void setDriver(String driver) {
		this.driver = driver;
	}
	public DatabaseConfig(String databaseServerForAllApp, String usernameForAllApp, String passwordForAllApp,
			String driver) {
		super();
		this.databaseServerForAllApp = databaseServerForAllApp;
		this.usernameForAllApp = usernameForAllApp;
		this.passwordForAllApp = passwordForAllApp;
		this.driver = driver;
	}
	public DatabaseConfig() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	

}
