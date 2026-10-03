//package com.mycash.core.Utills;
//
//import javax.mail.*;
//import javax.mail.internet.*;
//import java.io.File;
//import java.util.Properties;
//
//public class EmailUtil {
//
//    public static void sendEmailWithReport(String reportDirPath) {
//
//        try {
//            String zipPath = reportDirPath + ".zip";
//
//            String from = "your_email@example.com";
//            String to = "recipient@example.com";
//
//            Properties props = new Properties();
//            props.put("mail.smtp.host", "smtp.gmail.com");
//            props.put("mail.smtp.port", "587");
//            props.put("mail.smtp.auth", "true");
//            props.put("mail.smtp.starttls.enable", "true");
//
//            Session session = Session.getInstance(props, new Authenticator() {
//                protected PasswordAuthentication getPasswordAuthentication() {
//                    return new PasswordAuthentication(
//                            "your_email@gmail.com",
//                            "your_app_password"
//                    );
//                }
//            });
//
//            Message message = new MimeMessage(session);
//            message.setFrom(new InternetAddress(from));
//            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
//            message.setSubject("Allure Report");
//
//            BodyPart textPart = new MimeBodyPart();
//            textPart.setText("Please find attached Allure report.");
//
//            MimeBodyPart attachmentPart = new MimeBodyPart();
//            File f = new File(zipPath);
//            if (!f.exists()) {
//                System.out.println("Report zip not found: " + zipPath);
//            } else {
//                attachmentPart.attachFile(f);
//            }
//
//            Multipart multipart = new MimeMultipart();
//            multipart.addBodyPart(textPart);
//            if (f.exists()) multipart.addBodyPart(attachmentPart);
//
//            message.setContent(multipart);
//
//            Transport.send(message);
//
//            System.out.println("Email sent successfully!");
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}
