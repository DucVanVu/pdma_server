package org.pepfar.pdma;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.support.SpringBootServletInitializer;

@SpringBootApplication
public class PdmaApplication extends SpringBootServletInitializer
{

	public static void main(String[] args) {
		SpringApplication.run(PdmaApplication.class, args);
	}

}
