package com.example.parcial_sw1.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailService {

    @Value("${spring.sendgrid.api-key}") // Aquí se hace referencia a la propiedad del archivo de configuración
    private String sendGridApiKey;

    public void sendEmail(String to, String subject, String content) throws IOException {
        Email from = new Email("gusta25demarzo@gmail.com");
        Email recipient = new Email(to);
        Content emailContent = new Content("text/html", content);
        Mail mail = new Mail(from, subject, recipient, emailContent);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        Response response = sg.api(request);

        // Imprimir el código de respuesta y los mensajes de SendGrid
        System.out.println("Response status: " + response.getStatusCode());
        System.out.println("Response body: " + response.getBody());
        System.out.println("Response headers: " + response.getHeaders());
    }

}
