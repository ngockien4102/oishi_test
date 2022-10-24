package doan.oishii_share_cong_thuc_nau_an.service;

import doan.oishii_share_cong_thuc_nau_an.common.vo.AccountManageVo;
import doan.oishii_share_cong_thuc_nau_an.common.vo.AccountVo;
import doan.oishii_share_cong_thuc_nau_an.web.entities.Account;
import org.springframework.data.domain.Page;

import javax.security.auth.login.AccountNotFoundException;
import java.util.List;

public interface AccountService {

     void updateResetPassword (String password, String email) throws AccountNotFoundException;

     void changePassword (String userName,  String newPass) throws AccountNotFoundException;

     Page<AccountManageVo> findAll(String searchData, Integer pageIndex, Integer pageSize);

     Account getByResetPasswordToken(String token);

     void updatePassword(Account account, String newPassword);

     void updateResetPasswordToken (String token, String email) throws AccountNotFoundException;
}
