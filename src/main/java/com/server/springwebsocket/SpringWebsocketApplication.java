package com.server.springwebsocket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties; 
import java.util.UUID; 

@SpringBootApplication
public class SpringWebsocketApplication {

	public static void main(String[] args) {
		try {
			System.out.println("path: " + System.getProperty("user.dir") + "\\src\\main\\resources\\static\\build");
			String build = UUID.randomUUID().toString();
			File file = new File(System.getProperty("user.dir") + "\\src\\main\\resources\\static\\build");
			
			FileWriter fw = new FileWriter(file.getAbsoluteFile(), false);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(build);
            bw.close();


			file = new File(System.getProperty("user.dir") + "\\src\\main\\java\\com\\server\\springwebsocket\\build");
			
			fw = new FileWriter(file.getAbsoluteFile(), false);
            bw = new BufferedWriter(fw);
            bw.write(build);
            bw.close();
		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
		SpringApplication.run(SpringWebsocketApplication.class, args);
	}

}
