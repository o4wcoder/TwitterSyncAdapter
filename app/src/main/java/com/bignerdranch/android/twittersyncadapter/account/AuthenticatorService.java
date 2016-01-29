package com.bignerdranch.android.twittersyncadapter.account;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Chris Hare on 10/22/2015.
 */
public class AuthenticatorService extends Service {
    private Authenticator mAuthenticator;
    public AuthenticatorService() {
        mAuthenticator = new Authenticator(this);
    }
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
