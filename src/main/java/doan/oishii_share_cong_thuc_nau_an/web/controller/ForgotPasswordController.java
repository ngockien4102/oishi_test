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


    //n??t g???i mail
    @PostMapping("/forgot_password")
    public ResponseEntity<?> processForgotPassword(HttpServletRequest request) {
        String email = request.getParameter("email");
        String token = RandomString.make(30);
        //String password = RandomString.make(5);
        MessageVo message = new MessageVo();

        try {
            //accountService.updateResetPassword(password, email);
            accountService.updateResetPasswordToken(token, email);
            //String resetPasswordLink = Utility.getSiteURL(request) + "/showResetPasswordPage?token=" + token;
            String resetPasswordLink =  token;
            //sendEmail(email, token);
            sendEmail(email, resetPasswordLink);
            message.setMessContent("Ch??ng t??i ???? g???i m???t kh???u m???i ?????n mail c???a b???n.");
            message.setMessType("success");

        } catch (AccountNotFoundException ex) {
            message.setMessContent("Kh??ng t??m th???y t??i kho???n ????ng k?? mail n??y");
            message.setMessType("error");
            return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            message.setMessContent("G???i mail l???i");
            message.setMessType("error");
            return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    public void sendEmail(String recipientEmail, String link) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom("chantran16121999@gmail.com", "OishiiShare");
        helper.setTo(recipientEmail);

        String subject = "Oishii Share g???i b???n m???t kh???u m???i cho t??i kho???n c???a b???n";

        String content = "<p>Xin ch??o,</p>"
                + "<p>B???n ???? s??? d???ng t??nh n??ng qu??n m???t kh???u c???a OishiiShare.</p>"
                //+ "<p>Vui l??ng b???m v??o ???????ng link d?????i ????? th???c hi???n thay ?????i m???t kh???u:</p>"
               // + "<p><a href=\"" + link + "\"> Thay ?????i m???t kh???u</a></p>"
                + "<p>Vui l??ng nh???p token d?????i v??o m??n reset password ????? th???c hi???n thay ?????i m???t kh???u:</p>"
                + "<p><b> " + link + "</b></p>"
                + "<br>"
                + "<p>C???m ??n v?? ???? s??? d???ng Oishii Share <3</p>";

        helper.setSubject(subject);

        helper.setText(content, true);

        mailSender.send(message);
        System.out.println("mail send");
    }


    //b???m v??o link tr??n mail show trang change pass
    @GetMapping("/showResetPasswordPage")
    public ResponseEntity<?> showResetPasswordPage(@Param(value = "token") String token, Model model) {
        MessageVo message = new MessageVo();
        Account account = accountService.getByResetPasswordToken(token);
        model.addAttribute("token", token);

        if (account == null) {
            message.setMessContent("token ???? h???t h???n");
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
            message.setMessContent("token ???? h???t h???n");
            message.setMessType("error");
            return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
        } else {
            accountService.updatePassword(account, password);
            message.setMessContent("B???n ???? thay ?????i m???t kh???u th??nh c??ng");
            message.setMessType("success");

        }
        return new ResponseEntity<>(message, HttpStatus.OK);
    }



    //thay ?????i m???t kh???u
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
                message.setMessContent("M???t kh???u ???? ???????c thay ?????i");
                message.setMessType("success");
            }else{
                message.setMessContent("M???t kh???u c?? kh??ng ????ng, xin h??y nh???p l???i");
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
