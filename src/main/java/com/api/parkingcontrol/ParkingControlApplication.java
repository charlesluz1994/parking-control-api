package com.api.parkingcontrol;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class ParkingControlApplication {

	public static void main(String[] args) {
		SpringApplication.run(ParkingControlApplication.class, args);
	}

	/*Método que retorna uma string para tela, após acessar o '/' */
	@GetMapping("/")
	public String index(){
		return "For POST method, access the URL http://localhost:8080/parking-spot";
	}
}
