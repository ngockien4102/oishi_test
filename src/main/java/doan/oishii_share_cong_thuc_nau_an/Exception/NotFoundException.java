package doan.oishii_share_cong_thuc_nau_an.Exception;

public class NotFoundException extends RuntimeException{
    public NotFoundException(int Errorcode,String message){
        super(message);
    }
}
