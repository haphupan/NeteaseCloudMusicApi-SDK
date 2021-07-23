package api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sun.org.apache.xpath.internal.operations.Bool;
import util.NetWorkUtil;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UserApi {
    private JSONObject userInfo;
    private String password;
    private String phoneNumber;
    private String email;
    private String key;

    public UserApi(){
        userInfo = new JSONObject();
        password = null;
        phoneNumber = null;
        email = null;
        key = null;
    }

    /**
     * ͨ���ֻ��ŵ�¼
     * ���ڵ�¼ǰ�ֶ��������뼰�ֻ���
     * @return ��¼�Ƿ�ɹ�
     */
    public int loginByPhoneNumber(){
        if(password==null||phoneNumber==null){
            return 400;
        }
        String result = NetWorkUtil.sendByGetUrl(String.format("/login/cellphone?phone=%s&md5_password=%s", phoneNumber,password),null);
        if(result==null){
            return -1;
        }
        userInfo = JSON.parseObject(result);
        return userInfo.getInteger("code");
    }

    /**
     * ͨ���ֻ��ŵ�¼
     * ���ڵ�¼ǰ�ֶ��������뼰����
     * @return ��¼�Ƿ�ɹ�
     */
    public int loginByEmail(){
        if(password==null||email==null){
            return 400;
        }
        String result = NetWorkUtil.sendByGetUrl(String.format("/login?email=%s&md5_password=%s", email,password),null);
        if(result==null){
            return -1;
        }
        userInfo = JSON.parseObject(result);
        return userInfo.getInteger("code");
    }

    /**
     * �����ֻ�����
     * ����ʹ���ֻ���¼ǰִ��
     * @param phoneNumber �ֻ����루+86��
     */
    public void setPhoneNumber(String phoneNumber){
        this.phoneNumber = phoneNumber;
    }

    /**
     * ��������
     * ����ʹ�������¼ǰִ��
     * @param email ����
     */
    public void setEmail(String email){
        this.email = email;
    }

    /**
     * ��������
     * ���ڵ�¼ǰִ��
     * @param password ����
     * @param md5 �Ƿ��ѽ���md5���ܣ���Ϊ�����Զ����ܣ�
     */
    public void setPassword(String password,Boolean md5){
        if(!md5){
            this.password = encodeByMD5(password);
        }
        else{
            this.password = password;
        }
    }

    /**
     * �����ά����Կ
     * @return �Ƿ��ȡ�ɹ�
     */
    public Boolean getKey(){
        String result = NetWorkUtil.sendByGetUrl("/login/qr/key?timestamp=" + System.currentTimeMillis(),null);
        if(result==null||JSON.parseObject(result).getJSONObject("data").getString("unikey")==null){
            return false;
        }
        else{
            key = JSON.parseObject(result).getJSONObject("data").getString("unikey");
            return true;
        }
    }

    /**
     * ������ά��
     * @return base64��ά��
     */
    public String createQRCode(){
        String result = NetWorkUtil.sendByGetUrl(String.format("/login/qr/create?key=%s&qrimg=1&timestamp=" + System.currentTimeMillis(),key),null);
        if(result==null||JSON.parseObject(result).getJSONObject("data").getString("qrimg")==null){
            return null;
        }
        else{
            return JSON.parseObject(result).getJSONObject("data").getString("qrimg");
        }
    }

    /**
     * ����ά��״̬
     * @return ״̬��
     */
    public int checkQRStatus(){
        String result = NetWorkUtil.sendByGetUrl(String.format("https://music.wearbbs.cn/login/qr/check?key=%s&timestamp=" + System.currentTimeMillis(),key),"");
        if(result==null){
            return -1;
        }
        else{
            JSONObject tmp = JSON.parseObject(result);
            int code = JSON.parseObject(result).getInteger("code");
            if(code==803){
                userInfo.put("cookie",tmp.getString("cookie"));
            }
            return code;
        }
    }

    /**
     * ��ȡCookie
     * ���ȵ�¼
     * @return cookie
     */
    public String getCookie(){
        return userInfo.getString("cookie");
    }

    /**
     * ��ȡ�û���Ϣ
     * ���ȵ�¼����ά���¼���ֶ���ȡ
     * @return �û���Ϣ
     */
    public String getProfile(){
        return userInfo.getString("profile");
    }
    /**
     * md5����
     * @param str �������ַ���
     */
    public static String encodeByMD5(String str) {
        byte[] secretBytes;
        try {
            secretBytes = MessageDigest.getInstance("md5").digest(
                    str.getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("md5 error");
        }
        StringBuilder md5code = new StringBuilder(new BigInteger(1, secretBytes).toString(16));
        for (int i = 0; i < 32 - md5code.length(); i++) {
            md5code.insert(0, "0");
        }
        return md5code.toString();
    }

}