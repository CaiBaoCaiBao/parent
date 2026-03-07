package notification.service.impl;

import common.enums.RedisKey;
import common.enums.ResultCode;
import common.exception.BusinessException;
import common.utils.RedisUtil;
import common.utils.Result;
import common.utils.StrGenerator;
import common.utils.Verification;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import notification.enums.MailTemplate;
import notification.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Description;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@Description("邮件服务")
public class MailServiceImpl implements MailService {
    @Autowired
    Verification verification;
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    JavaMailSender mailSender;
    @Autowired
    TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public Result<?> sendOtpMail(String email, String templateCode) {
        // 参数校验
        if (email == null || email.isEmpty()) {
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "邮箱不能为空");
        }
        if (templateCode == null || templateCode.isEmpty()) {
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "验证码类型错误");
        }
        if (!verification.isEmail(email)) {
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "邮箱格式不正确");
        }
        // 生成验证码并存储到Redis
        String otp = StrGenerator.generateAlphanumericStr(6);
        // 根据模板类型获取邮件主题和模板路径
        MailTemplate template = MailTemplate.fromCode(templateCode);
        String subject = template.getSubject();
        String templatePath = template.getTemplatePath();
        String redisKey = template.getRedisKey() + email;
        String limitKey = redisKey + ":limit";
        if (redisUtil.get(limitKey) != null) {
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "发送过于频繁，请60秒后再试");
        }
        redisUtil.set(redisKey, otp, 300); // 5分钟过期
        // 5. 设置60秒发送限制
        redisUtil.set(limitKey, "1", 60);
        sendMailTemplate(email,subject,templatePath,otp);
        return Result.success("验证码发送成功");
    }

    @Description("发送模板邮件")
    private void sendMailTemplate(String toEmail,String subject,String templatePath,String code){
        MimeMessage message = mailSender.createMimeMessage();
        try{
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            // 替换模板中的占位符
            Context context = new Context();
            context.setVariable("code", code);
            String emailContent = templateEngine.process(templatePath, context);

            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setFrom(fromEmail);
            helper.setText(emailContent, true);
            mailSender.send(message);
        }catch (MessagingException e){
            log.error("发送邮件失败: {}", e.getMessage());
            throw new BusinessException(ResultCode.ERROR.getCode(), "发送邮件失败");
        }
    }
}
