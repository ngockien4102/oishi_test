package doan.oishii_share_cong_thuc_nau_an.web.controller;

import doan.oishii_share_cong_thuc_nau_an.common.utils.Utility;
import doan.oishii_share_cong_thuc_nau_an.common.vo.ChangePasswordRequest;
import doan.oishii_share_cong_thuc_nau_an.common.vo.MessageVo;
import doan.oishii_share_cong_thuc_nau_an.common.vo.UserDetailsImpl;
import doan.oishii_share_cong_thuc_nau_an.service.AccountService;

import doan.oishii_share_cong_thuc_nau_an.web.entities.Account;
import net.bytebuddy.utility.RandomString;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.security.auth.login.AccountNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

@Controller
public class ForgotPasswordController {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private AccountService accountService;



//    @GetMapping(value = { "/", "/forgot_password" })
//    public String showForgotPasswordPage() {
//        return "forgot-password";
//    }


    //nút gửi mail
    @PostMapping("/forgot_password")
    public ResponseEntity<?> processForgotPassword(HttpServletRequest request) {
        String email = request.getParameter("email");
        String token = RandomString.make(30);
        //String password = RandomString.make(5);
        MessageVo message = new MessageVo();

        try {
            //accountService.updateResetPassword(password, email);
            accountService.updateResetPasswordToken(token, email);
            String resetPasswordLink = Utility.getSiteURL(request) + "/showResetPasswordPage?token=" + token;
            //sendEmail(email, token);
            sendEmail(email, resetPasswordLink);
            message.setMessContent("Chúng tôi đã gửi mật khẩu mới đến mail của bạn.");
            message.setMessType("success");

        } catch (AccountNotFoundException ex) {
            message.setMessContent("Không tìm thấy tài khoản đăng ký mail này");
            message.setMessType("error");
            return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            message.setMessContent("Gửi mail lỗi");
            message.setMessType("error");
            return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    public void sendEmail(String recipientEmail, String link) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom("oishiishare123@gmail.com", "OishiiShare");
        helper.setTo(recipientEmail);

        String subject = "Oishii Share gửi bạn mật khẩu mới cho tài khoản của bạn";

        String content = "<p>Xin chào,</p>"
                + "<p>Bạn đã sử dụng tính năng quên mật khẩu của OishiiShare.</p>"
                + "<p>Vui lòng bấm vào đường link dưới để thực hiện thay đổi mật khẩu:</p>"
                + "<p><a href=\"" + link + "\"> Thay đổi mật khẩu</a></p>"
                + "<br>"
                + "<p>Cảm ơn vì đã sử dụng Oishii Share <3</p>";

        helper.setSubject(subject);

        helper.setText(content, true);

        mailSender.send(message);
        System.out.println("mail send");
    }


    //bấm vào link trên mail show trang change pass
    @GetMapping("/showResetPasswordPage")
    public ResponseEntity<?> showResetPasswordPage(@Param(value = "token") String token, Model model) {
        MessageVo message = new MessageVo();
        Account account = accountService.getByResetPasswordToken(token);
        model.addAttribute("token", token);

        if (account == null) {
            message.setMessContent("token đã hết hạn");
            message.setMessType("error");
            return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(model, HttpStatus.OK);

    }

    @PostMapping("/resetPassword")
    public ResponseEntity<?> processResetPassword(HttpServletRequest request, Model model) {
        MessageVo message = new MessageVo();
        String token = request.getParameter("token");
        String password = request.getParameter("password");


        Account account = accountService.getByResetPasswordToken(token);
        model.addAttribute("title", "Reset your password");

        if (account == null) {
            message.setMessContent("token đã hết hạn");
            message.setMessType("error");
            return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
        } else {
            accountService.updatePassword(account, password);
            message.setMessContent("Bạn đã thay đổi mật khẩu thành công");
            message.setMessType("success");

        }
        return new ResponseEntity<>(message, HttpStatus.OK);
    }



    //thay đổi mật khẩu
    @PostMapping("/change_password")
    @PreAuthorize("hasRole('ROLE_ADMIN')or hasRole('ROLE_MOD')or hasRole('ROLE_USER')")
    public ResponseEntity<?> changePassword(Authentication authentication, @Valid @RequestBody ChangePasswordRequest changePasswordRequest)
            throws AccountNotFoundException {
        MessageVo message = new MessageVo();
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        try {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String userName = userDetails.getUsername();
            String password = userDetails.getPassword();
            boolean passChecker = passwordEncoder.matches(changePasswordRequest.getOldPassword(), password);
            if(passChecker) {
                accountService.changePassword(userName, changePasswordRequest.getNewPassword());
                message.setMessContent("Mật khẩu đã được thay đổi");
                message.setMessType("success");
            }else{
                message.setMessContent("Mật khẩu cũ không đúng, xin hãy nhập lại");
                message.setMessType("error");
                return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
            }

        } catch (AccountNotFoundException ex) {
            message.setMessContent("Account not found");
            message.setMessType("error");
            return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            message.setMessContent("Error while change password");
            message.setMessType("error");
            return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(message, HttpStatus.OK);


    }

}
