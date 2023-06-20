package com.disposableemail.core.service.impl.mail;

import com.disposableemail.config.TestConfig;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@ContextConfiguration(classes = TestConfig.class)
class MailServiceTest {

    private JavaMailSenderImpl mailSender;

    @BeforeEach
    void setUp() {
        this.mailSender = new JavaMailSenderImpl();
        mailSender.setHost("localhost");
        mailSender.setPort(25);
        mailSender.setUsername("t1@example.com");
        mailSender.setPassword("password");

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");
    }

    @Disabled
    @RepeatedTest(1)
    void shouldSendMail() throws MessagingException {
        MimeMessage mailMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mailMessage, true);
        helper.setFrom("t6@example.com");
        helper.setTo("test6@example.com");
        helper.setSubject("Hello Java Mail");
        helper.setText("<html> <body><h1>Hello </h1> </body></html>", true);
        String htmlContent = """
                <html>
                  <head>
                                
                    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
                  </head>
                  <body>
                    <p><br>
                    </p>
                    <div class="moz-forward-container"><br>
                      <br>
                      -------- Forwarded Message --------
                      <table class="moz-email-headers-table" cellspacing="0"
                        cellpadding="0" border="0">
                        <tbody>
                          <tr>
                            <th valign="BASELINE" nowrap="nowrap" align="RIGHT">Subject:
                            </th>
                            <td>Java Mail 7</td>
                          </tr>
                          <tr>
                            <th valign="BASELINE" nowrap="nowrap" align="RIGHT">Date: </th>
                            <td>Fri, 20 Jan 2023 00:55:38 +0300 (MSK)</td>
                          </tr>
                          <tr>
                            <th valign="BASELINE" nowrap="nowrap" align="RIGHT">From: </th>
                            <td><a class="moz-txt-link-abbreviated" href="mailto:t1@example.com">t1@example.com</a></td>
                          </tr>
                          <tr>
                            <th valign="BASELINE" nowrap="nowrap" align="RIGHT">To: </th>
                            <td><a class="moz-txt-link-abbreviated" href="mailto:test6@example.com">test6@example.com</a></td>
                          </tr>
                        </tbody>
                      </table>
                      <br>
                      <br>
                      Java test mail. No attachments<br>
                    </div>
                  </body>
                </html>
                """;
        //   helper.setText(htmlContent, true);
        mailSender.send(mailMessage);
        assertThat(mailSender).isNotNull();
    }
}
