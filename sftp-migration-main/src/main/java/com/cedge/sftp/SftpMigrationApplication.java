package com.cedge.sftp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SftpMigrationApplication {

	public static void main(String[] args) {
		SpringApplication.run(SftpMigrationApplication.class, args);
	}

}
