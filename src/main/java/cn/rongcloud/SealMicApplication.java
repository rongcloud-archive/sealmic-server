package cn.rongcloud;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = {"cn.rongcloud"})
@ServletComponentScan
@EnableAsync
@Slf4j
public class SealMicApplication {

	public static void main(String[] args) {
		SpringApplication.run(SealMicApplication.class, args);
		log.info("SealMicApplication started");
	}
}
