package com.yuanzhang.econexus.util;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class ExmailSendUtil {
    private static final Logger logger = LoggerFactory.getLogger(ExmailSendUtil.class);

    @Value("${spring.mail.frommail}")
    private String from;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private JavaMailSender javaMailSender;

    // 发送HTML邮件
    public String sendHtmlMail(String[] to, String subject, String content) {
        String[] validMails = filterEmptyEmails(to);

        if (validMails.length == 0) {
            logger.warn("无有效收件人邮箱，终止邮件发送");
            return "无有效收件人邮箱，邮件发送终止";
        }

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from); // 发件人（需与配置的username一致）
            helper.setTo(validMails); // 收件人
            helper.setSubject(subject); // 主题
            helper.setText(content, true); // true表示HTML格式

            javaMailSender.send(message);
            logger.info("邮件发送成功，有效收件人：{}", String.join(",", validMails));
            return "邮件发送成功，有效收件人：" + String.join(",", validMails);
        }catch (MessagingException e){
            logger.error("邮件发送失败，有效收件人：{}，错误信息：{}",
                    String.join(",", validMails), e.getMessage(), e);
            return "邮件发送失败：" + e.getMessage();
        }
    }

    /**
     * 过滤收件人数组中的空邮箱（null/空字符串/纯空格）
     * @param emails 原始收件人邮箱数组
     * @return 过滤后的有效邮箱数组（无有效邮箱则返回空数组）
     */
    public static String[] filterEmptyEmails(String[] emails) {
        if (emails == null || emails.length == 0) {
            return new String[0]; // 空数组直接返回
        }

        List<String> validEmails = new ArrayList<>();
        for (String email : emails) {
            // 过滤null、空字符串、纯空格的邮箱
            if (StringUtils.hasText(email)) {
                validEmails.add(email.trim()); // 去除首尾空格
            }
        }

        String[] mails = validEmails.toArray(new String[0]);

        // 转数组返回（无有效邮箱则返回空数组）
        return Arrays.stream(mails)
                    .distinct()
                    .toArray(String[]::new);
    }
}