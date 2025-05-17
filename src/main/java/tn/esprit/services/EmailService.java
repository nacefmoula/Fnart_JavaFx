package tn.esprit.services;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.util.Properties;

public class EmailService {
    // Mailtrap credentials
    private static final String EMAIL_USERNAME = "0f9cdf3bffc2c1"; // Your Mailtrap username
    private static final String EMAIL_PASSWORD = "db3557c4396ab5"; // Your Mailtrap password
    private static final String SMTP_HOST = "smtp.mailtrap.io";
    private static final int SMTP_PORT = 2525;

    // Gmail credentials
    private static final String GMAIL_EMAIL = "nacef627@gmail.com";
    private static final String GMAIL_PASSWORD = "qukq yzyy pzov cvay";
    private static final String GMAIL_SMTP_HOST = "smtp.gmail.com";
    private static final String GMAIL_SMTP_PORT = "587";

    /**
     * Sends a simple email using Gmail SMTP
     * @param recipient the recipient's email address
     * @param subject the email subject
     * @param content the email content as plain text
     */
    public static void sendEmail(String recipient, String subject, String content) {
        // SMTP server configuration for Gmail
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", GMAIL_SMTP_HOST);
        properties.put("mail.smtp.port", GMAIL_SMTP_PORT);

        // Create a session with authentication
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(GMAIL_EMAIL, GMAIL_PASSWORD);
            }
        });

        try {
            // Create the email message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(GMAIL_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
            message.setSubject(subject);
            message.setText(content);

            // Send the email
            Transport.send(message);
            System.out.println("Email sent successfully to " + recipient);
        } catch (MessagingException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Sends a password reset email with HTML content using Mailtrap
     * @param recipientEmail the recipient's email address
     * @param resetToken the password reset token
     */
    public static void sendPasswordResetEmail(String recipientEmail, String resetToken) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_USERNAME, EMAIL_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("noreply@designmodo.com"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Password Reset - Designmodo");

            String resetLink = "designmodo://reset-password/" + resetToken;

            String htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Password Reset - Designmodo</title>
                    <style>
                        body {
                            margin: 0;
                            padding: 0;
                            font-family: Arial, Helvetica, sans-serif;
                            background-color: #f5f5f5;
                            color: #333333;
                        }
                        .container {
                            max-width: 600px;
                            margin: 0 auto;
                            padding: 20px;
                        }
                        .header {
                            text-align: center;
                            padding: 20px 0;
                        }
                        .logo {
                            max-width: 200px;
                        }
                        .card {
                            background-color: #ffffff;
                            border-radius: 8px;
                            box-shadow: 0 2px 10px rgba(0,0,0,0.08);
                            padding: 40px;
                            margin: 20px 0;
                        }
                        .eyes {
                            text-align: center;
                            margin-bottom: 10px;
                        }
                        h1 {
                            font-size: 32px;
                            color: #333333;
                            text-align: center;
                            margin-bottom: 30px;
                        }
                        .message {
                            font-size: 16px;
                            line-height: 1.6;
                            margin-bottom: 30px;
                            text-align: center;
                        }
                        .button {
                            text-align: center;
                            margin: 30px 0;
                        }
                        .button a {
                            display: inline-block;
                            background-color: #007bff;
                            color: #ffffff;
                            text-decoration: none;
                            padding: 14px 40px;
                            border-radius: 4px;
                            font-weight: bold;
                            font-size: 16px;
                        }
                        .email-info {
                            text-align: center;
                            color: #666666;
                            margin-top: 20px;
                        }
                        .email-info a {
                            color: #007bff;
                            text-decoration: none;
                        }
                        .footer {
                            text-align: center;
                            padding: 20px 0;
                            color: #666666;
                            font-size: 14px;
                        }
                        .notice {
                            font-size: 14px;
                            color: #666666;
                            text-align: center;
                            margin-top: 30px;
                        }
                        @media only screen and (max-width: 480px) {
                            .card {
                                padding: 20px;
                            }
                            h1 {
                                font-size: 24px;
                            }
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <img src="https://yourcompany.com/logo.png" alt="Designmodo" class="logo">
                        </div>
                        
                        <div class="card">
                            <div class="eyes">
                                <span style="font-size: 32px;">👀</span>
                            </div>
                            
                            <h1>Password reset</h1>
                            
                            <div class="message">
                                <p>Someone requested that the password be reset for the following account:</p>
                                <p>To reset your password, visit the following address:</p>
                            </div>
                            
                            <div class="button">
                                <a href="%s">Click here to reset your password</a>
                            </div>
                            
                            <div class="email-info">
                                Your email: <a href="mailto:%s">%s</a>
                            </div>
                            
                            <div class="notice">
                                If this was a mistake, just ignore this email and nothing will happen.
                            </div>
                        </div>
                        
                        <div class="footer">
                            Copyright © 2025 Designmodo. All Rights Reserved.
                        </div>
                    </div>
                </body>
                </html>
            """.formatted(resetLink, recipientEmail, recipientEmail);

            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);
            System.out.println("Reset password email sent successfully to " + recipientEmail);

            // Show success alert
            Platform.runLater(() -> {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Email Sent");
                alert.setHeaderText(null);
                alert.setContentText("Password reset email has been sent to " + recipientEmail);
                alert.showAndWait();
            });
        } catch (MessagingException e) {
            System.err.println("Error sending reset password email: " + e.getMessage());
            e.printStackTrace();

            // Show error alert
            Platform.runLater(() -> {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Failed to send password reset email: " + e.getMessage());
                alert.showAndWait();
            });
        }
    }

    /**
     * Sends a password changed confirmation email with HTML content using Mailtrap
     * @param toEmail the recipient's email address
     */
    public static void sendPasswordChangedConfirmation(String toEmail) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_USERNAME, EMAIL_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("noreply@designmodo.com"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Password Changed Successfully - Designmodo");

            String emailContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Password Changed - Designmodo</title>
                    <style>
                        body {
                            margin: 0;
                            padding: 0;
                            font-family: Arial, Helvetica, sans-serif;
                            background-color: #f5f5f5;
                            color: #333333;
                        }
                        .container {
                            max-width: 600px;
                            margin: 0 auto;
                            padding: 20px;
                        }
                        .header {
                            text-align: center;
                            padding: 20px 0;
                        }
                        .card {
                            background-color: #ffffff;
                            border-radius: 8px;
                            box-shadow: 0 2px 10px rgba(0,0,0,0.08);
                            padding: 40px;
                            margin: 20px 0;
                        }
                        .success-icon {
                            text-align: center;
                            margin-bottom: 20px;
                        }
                        .success-icon span {
                            display: inline-block;
                            width: 60px;
                            height: 60px;
                            background-color: #28a745;
                            border-radius: 50%;
                            color: white;
                            font-size: 30px;
                            line-height: 60px;
                        }
                        h1 {
                            font-size: 28px;
                            color: #333333;
                            text-align: center;
                            margin-bottom: 30px;
                        }
                        .message {
                            font-size: 16px;
                            line-height: 1.6;
                            margin-bottom: 30px;
                        }
                        .tips {
                            background-color: #f8f9fa;
                            border-left: 4px solid #007bff;
                            padding: 15px;
                            margin: 25px 0;
                        }
                        .tips h3 {
                            margin-top: 0;
                            color: #333333;
                        }
                        .tips ul {
                            padding-left: 20px;
                            margin-bottom: 0;
                        }
                        .tips li {
                            margin-bottom: 5px;
                        }
                        .footer {
                            text-align: center;
                            padding: 20px 0;
                            color: #666666;
                            font-size: 14px;
                        }
                        @media only screen and (max-width: 480px) {
                            .card {
                                padding: 20px;
                            }
                            h1 {
                                font-size: 24px;
                            }
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <img src="https://yourcompany.com/logo.png" alt="Designmodo" class="logo">
                        </div>
                        
                        <div class="card">
                            <div class="success-icon">
                                <span>✓</span>
                            </div>
                            
                            <h1>Password Changed Successfully</h1>
                            
                            <div class="message">
                                <p>Hello,</p>
                                <p>We're confirming that your password was successfully changed.</p>
                                <p>If you didn't make this change, please contact our support team immediately as your account may have been compromised.</p>
                            </div>
                            
                            <div class="tips">
                                <h3>Account Security Tips:</h3>
                                <ul>
                                    <li>Use strong, unique passwords for each of your accounts</li>
                                    <li>Enable two-factor authentication when available</li>
                                    <li>Never share your login credentials with others</li>
                                </ul>
                            </div>
                            
                            <p style="text-align: center; color: #666666; margin-top: 25px;">Thank you for using Designmodo</p>
                        </div>
                        
                        <div class="footer">
                            Copyright © 2025 Designmodo. All Rights Reserved.
                        </div>
                    </div>
                </body>
                </html>
            """;

            message.setContent(emailContent, "text/html; charset=utf-8");
            Transport.send(message);
            System.out.println("Password change confirmation email sent successfully to " + toEmail);

        } catch (MessagingException e) {
            System.err.println("Error sending confirmation email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Sends a custom HTML email using Mailtrap
     * @param recipientEmail the recipient's email address
     * @param subject the email subject
     * @param htmlContent the HTML content of the email
     */
    public static void sendHtmlEmail(String recipientEmail, String subject, String htmlContent) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_USERNAME, EMAIL_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("noreply@designmodo.com"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);
            message.setContent(htmlContent, "text/html; charset=utf-8");
            Transport.send(message);
            System.out.println("HTML email sent successfully to " + recipientEmail);
        } catch (MessagingException e) {
            System.err.println("Error sending HTML email: " + e.getMessage());
            e.printStackTrace();
        }
    }
}