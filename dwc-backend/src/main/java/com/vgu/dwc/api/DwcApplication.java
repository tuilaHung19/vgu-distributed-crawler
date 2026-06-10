package com.vgu.dwc.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.vgu.dwc")
public class DwcApplication {

	public static void main(String[] args) {
		SpringApplication.run(DwcApplication.class, args);
	}

}