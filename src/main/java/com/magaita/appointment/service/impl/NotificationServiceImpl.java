package com.magaita.appointment.service.impl;

import com.magaita.appointment.dto.NotificationDto;
import com.magaita.appointment.entity.Notification;
import com.magaita.appointment.entity.User;
import com.magaita.appointment.repository.NotificationRepo;
import com.magaita.appointment.service.NotificationService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepo notificationRepo;
    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;


    @Override
    @Async
    public void sendEmail(NotificationDto notificationDto, User user) {
         try{
             MimeMessage mimeMessage = javaMailSender.createMimeMessage();

             MimeMessageHelper helper = new MimeMessageHelper(
                     mimeMessage,
                     MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                     StandardCharsets.UTF_8.name()
             );
             helper.setTo(notificationDto.getRecipient());
             helper.setSubject(notificationDto.getSubject());

             //Use template if provided
             if(notificationDto.getTemplateName() != null){
                 Context context = new Context();
                 context.setVariables(notificationDto.getTemplateVariables());
                 String htmlContext = templateEngine.process(notificationDto.getTemplateName(),context);
                 helper.setText(htmlContext,true);
             }else{
                 helper.setText(notificationDto.getMessage(),true);
             }

             javaMailSender.send(mimeMessage);
             log.info("Email sent out");

             //Save to database table
             Notification notificationToSave = Notification.builder()
                     .recipient(notificationDto.getRecipient())
                     .subject(notificationDto.getSubject())
                     .message(notificationDto.getMessage())
                     .type(notificationDto.getType())
                     .user(user)
                     .build();
             notificationRepo.save(notificationToSave);

         }catch (Exception e){
             log.info(e.getMessage());
         }
    }
}
