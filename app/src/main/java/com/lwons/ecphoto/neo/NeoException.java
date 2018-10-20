package com.lwons.ecphoto.neo;

/**
 * Created by liuwons on 2018/10/20
 */
public class NeoException extends Exception {
    private String mReason;

    public NeoException(String reason) {
        super(reason);
        mReason = reason;
    }

    public String getReason() {
        return mReason;
    }
}
