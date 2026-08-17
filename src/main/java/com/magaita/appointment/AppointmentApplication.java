package com.magaita.appointment;

import com.magaita.appointment.dto.NotificationDto;
import com.magaita.appointment.entity.User;
import com.magaita.appointment.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@RequiredArgsConstructor
public class AppointmentApplication {

	private final NotificationService notificationService;

	public static void main(String[] args) {

		SpringApplication.run(AppointmentApplication.class, args);
	}
	@Bean
	CommandLineRunner runner(){
		return args -> {
			NotificationDto notificationDto = NotificationDto.builder()
					.recipient("gideonaugustino1998@gmail.com")
					.subject("Testing mail")
					.message("Hello this is a test mail from gideon")
					.build();
			notificationService.sendEmail(notificationDto,new User());
		};
	}

}
