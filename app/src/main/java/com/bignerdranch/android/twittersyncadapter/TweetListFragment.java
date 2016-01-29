package com.bignerdranch.android.twittersyncadapter;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bignerdranch.android.twittersyncadapter.account.Authenticator;

import java.io.IOException;

/**
 * A placeholder fragment containing a simple view.
 */
public class TweetListFragment extends Fragment {

    private static final String TAG = "TweetListFragment";

    private String mAccessToken;
    private Account mAccount;

    private TextView mAuthTokenTextView;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tweet_list, container, false);
        mAuthTokenTextView = (TextView) view.findViewById(
                R.id.fragment_tweet_list_auth_token_text_view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.e(TAG,"onStart(): fetchAccessToken()");
        fetchAccessToken();
    }

    private void fetchAccessToken() {
        AccountManager accountManager = AccountManager.get(getActivity());
        Log.e(TAG,"Authenticator account name: " + Authenticator.ACCOUNT_NAME);

        mAccount = new Account(Authenticator.ACCOUNT_NAME,
                Authenticator.ACCOUNT_TYPE);

        Log.e(TAG,"Created new Account " + mAccount.name);
        Log.e(TAG,"Acount More Info: " + mAccount.toString());

        accountManager.getAuthToken(
                mAccount, Authenticator.AUTH_TOKEN_TYPE, null, getActivity(),
                new AccountManagerCallback<Bundle>() {
                    @Override
                    public void run(AccountManagerFuture<Bundle> future) {
                        try {
                            Log.e(TAG,"run() Iniside");
                            Bundle bundle = future.getResult();
                            mAccessToken = bundle.getString(
                                    AccountManager.KEY_AUTHTOKEN);
                            Log.e(TAG,"Have token: " + mAccessToken);
                            mAuthTokenTextView.setText(
                                    "Have access token: " + mAccessToken);

                        } catch (AuthenticatorException |
                                OperationCanceledException |
                                IOException e) {
                            Log.e(TAG, "Got an exception", e);
                        }
                    }
                }, null);
    }

}
