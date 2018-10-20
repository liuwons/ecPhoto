package com.lwons.ecphoto.encry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
    private Cipher mCipher;

    SecureRandom mSecureRandom;

    public Encryptor(String username, String password) throws UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        mSecureRandom = new SecureRandom(); // should be the best PRNG

        mSecretKeySpec = generateSecretKeySpec(username, password);
        mCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    }

    public void encryptFile(File inputFile, File outputFile, EncryptorCallback callback) {
        processFile(inputFile, outputFile, MODE_ENCRYPT, callback);
    }

    public void decryptFile(File inputFile, File outputFile, EncryptorCallback callback) {
        processFile(inputFile, outputFile, MODE_DECRYPT, callback);
    }

    private void processFile(File inputFile, File outputFile, int mode, EncryptorCallback callback) {
        if (mode != MODE_DECRYPT && mode != MODE_ENCRYPT) {
            callback.onFail(new IllegalArgumentException("mode not specified"));
            return;
        }

        FileInputStream fileInputStream;
        try {
            fileInputStream = getFileInputStream(inputFile);
        } catch (Exception e) {
            callback.onFail(e);
            return;
        }

        long totalSize = inputFile.length();

        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = new FileOutputStream(outputFile);
        } catch (FileNotFoundException e) {
            callback.onFail(e);
            return;
        }

        IvParameterSpec parameterSpec;
        if (mode == MODE_ENCRYPT) {
            byte[] bytes = new byte[16];
            mSecureRandom.nextBytes(bytes);
            parameterSpec = new IvParameterSpec(bytes);
            try {
                fileOutputStream.write(bytes);
            } catch (Exception e) {
                callback.onFail(e);
                return;
            }
        } else {
            byte[] bytes = new byte[16];
            try {
                readInput(fileInputStream, bytes);
            } catch (Exception e) {
                callback.onFail(e);
                return;
            }
            parameterSpec = new IvParameterSpec(bytes);
        }

        int m = mode == MODE_ENCRYPT ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE;
        try {
            mCipher.init(m, mSecretKeySpec, parameterSpec);
        } catch (Exception e) {
            callback.onFail(e);
            return;
        }
        CipherOutputStream cipherOutputStream = new CipherOutputStream(fileOutputStream, mCipher);

        long totalRead = 0;
        int lastProgress = 0;
        byte[] buffer = new byte[1024];
        int bytesdRead;
        try {
            while ((bytesdRead = fileInputStream.read(buffer)) >= 0) {
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
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
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
