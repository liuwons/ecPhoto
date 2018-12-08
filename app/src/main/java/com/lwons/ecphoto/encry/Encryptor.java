package com.lwons.ecphoto.encry;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by liuwons on 2018/10/18
 */
public class Encryptor {
    public static final int MODE_DECRYPT = 1;
    public static final int MODE_ENCRYPT = 2;

    private SecretKeySpec mSecretKeySpec;

    SecureRandom mSecureRandom;

    public Encryptor(String username, String password) throws UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        mSecureRandom = new SecureRandom(); // should be the best PRNG

        mSecretKeySpec = generateSecretKeySpec(username, password);
    }

    public void encrypt(InputStream inputStream, OutputStream outputStream, EncryptorCallback callback) {
        processFile(inputStream, outputStream, MODE_ENCRYPT, callback);
    }

    public void decrypt(InputStream inputStream, OutputStream outputStream, EncryptorCallback callback) {
        processFile(inputStream, outputStream, MODE_DECRYPT, callback);
    }

    private void processFile(InputStream inputStream, OutputStream outputStream, int mode, EncryptorCallback callback) {
        if (mode != MODE_DECRYPT && mode != MODE_ENCRYPT) {
            callback.onFail(new IllegalArgumentException("mode not specified"));
            return;
        }

        long totalSize = 0;
        try {
            totalSize = inputStream.available();
        } catch (IOException e) {
            callback.onFail(e);
            return;
        }

        IvParameterSpec parameterSpec;
        if (mode == MODE_ENCRYPT) {
            byte[] bytes = new byte[16];
            mSecureRandom.nextBytes(bytes);
            parameterSpec = new IvParameterSpec(bytes);
            try {
                outputStream.write(bytes);
            } catch (Exception e) {
                callback.onFail(e);
                return;
            }
        } else {
            byte[] bytes = new byte[16];
            try {
                readInput(inputStream, bytes);
            } catch (Exception e) {
                callback.onFail(e);
                return;
            }
            parameterSpec = new IvParameterSpec(bytes);
        }

        int m = mode == MODE_ENCRYPT ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE;
        Cipher cipher;
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(m, mSecretKeySpec, parameterSpec);
        } catch (Exception e) {
            callback.onFail(e);
            return;
        }
        CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, cipher);

        long totalRead = 0;
        int lastProgress = 0;
        byte[] buffer = new byte[1024];
        int bytesdRead;
        try {
            while ((bytesdRead = inputStream.read(buffer)) >= 0) {
                cipherOutputStream.write(buffer, 0, bytesdRead);

                totalRead += bytesdRead;
                int progress = (int) (totalRead * 100 / totalSize);
                if (progress > lastProgress && progress <= 100) {
                    callback.onUpdate(progress);
                    lastProgress = progress;
                }
            }
        } catch (IOException e) {
            callback.onFail(e);
            return;
        }

        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            cipherOutputStream.flush();
            cipherOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        callback.onFinish();
    }

    private void readInput(InputStream inputStream, byte[] bytes) throws Exception{
        int total = bytes.length;
        int byteRead;
        int totalRead = 0;

        while ((byteRead = inputStream.read(bytes, totalRead, total - totalRead)) >= 0) {
            totalRead += byteRead;
            if (totalRead >= total) {
                break;
            }
        }
    }

    private FileInputStream getFileInputStream(String filepath) throws Exception {
        return getFileInputStream(new File(filepath));
    }

    private FileInputStream getFileInputStream(File file) throws Exception {
        if (file == null) {
            throw new IllegalArgumentException("input file cannot be null");
        }

        if (!file.exists()) {
            throw new IllegalArgumentException("input file not exists");
        }

        if (!file.isFile()) {
            throw new IllegalArgumentException("input file is not valid file");
        }

        return new FileInputStream(file);
    }

    private static SecretKeySpec generateSecretKeySpec(String username, String password) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        byte[] key = (username + password).getBytes("UTF-8");
        MessageDigest sha = MessageDigest.getInstance("SHA-1");
        key = sha.digest(key);
        key = Arrays.copyOf(key, 16);
        return new SecretKeySpec(key, "AES");
    }
}
