package com.lwons.ecphoto.encry;

/**
 * Created by liuwons on 2018/10/18
 */
public class EncryptorNotInitException extends Exception {
    public EncryptorNotInitException(String prompt) {
        super(prompt);
    }
}
