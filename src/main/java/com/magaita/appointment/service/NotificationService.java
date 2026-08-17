package com.magaita.appointment.service;

import com.magaita.appointment.dto.NotificationDto;
import com.magaita.appointment.entity.User;
import org.springframework.stereotype.Service;


public interface NotificationService {
    void sendEmail(NotificationDto notificationDto, User user);
}
