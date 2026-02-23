package br.com.nexus.nexus_wms;

import org.springframework.boot.SpringApplication;

public class TestNexusWmsApplication {

	public static void main(String[] args) {
		SpringApplication.from(NexusWmsApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
