package com.bignerdranch.android.twittersyncadapter;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.bignerdranch.android.twittersyncadapter.account.Authenticator;
import com.bignerdranch.android.twittersyncadapter.web.AuthenticationInterface;
import com.bignerdranch.android.twittersyncadapter.web.AuthorizationInterceptor;
import com.bignerdranch.android.twittersyncadapter.web.TwitterOauthHelper;
import com.squareup.okhttp.OkHttpClient;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;

public class AuthenticationActivity extends AccountAuthenticatorActivity {

    /**********************************************************************/
    /*                             Constants                              */
    /**********************************************************************/
    private static final String EXTRA_ACCOUNT_TYPE =
            "com.bignerdranch.android.twittersyncadapter.ACCOUNT_TYPE";
    private static final String EXTRA_AUTH_TYPE =
            "com.bignerdranch.android.twittersyncadapter.AUTH_TYPE";
    private static final String TWITTER_ENDPOINT = "https://api.twitter.com";

    private static final String TAG = "AuthenticationActivity";
    private static final String TWITTER_OAUTH_ENDPOINT =
            "https://api.twitter.com/oauth/authorize";
    private static final String CALLBACK_URL = "http://www.bignerdranch.com";
    private static final String OAUTH_TOKEN_SECRET_KEY =
            "com.bignerdranch.android.twittersyncadapter.OAUTH_TOKEN_SECRET";

    private static final String OAUTH_TOKEN = "oauth_token";
    private static final String OAUTH_VERIFIER = "oauth_verifier";
    private static final String OAUTH_TOKEN_SECRET = "oauth_token_secret";

    /**********************************************************************/
    /*                              Local Data                            */
    /**********************************************************************/
    private WebView mWebView;
    private RestAdapter mRestAdapter;

    private TwitterOauthHelper mTwitterOauthHelper;
    private AuthenticationInterface mAuthenticationInterface;



    public static Intent newIntent(
            Context context, String accountType, String authTokenType) {
        Intent intent = new Intent(context, AuthenticationActivity.class);
        intent.putExtra(EXTRA_ACCOUNT_TYPE, accountType);
        intent.putExtra(EXTRA_AUTH_TYPE, authTokenType);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mWebView = new WebView(this);
        setContentView(mWebView);
        mWebView.setWebViewClient(mWebViewClient);

        mTwitterOauthHelper = TwitterOauthHelper.get();
        mTwitterOauthHelper.resetOauthToken();

        OkHttpClient client = new OkHttpClient();
        client.interceptors().add(new AuthorizationInterceptor());

        mRestAdapter = new RestAdapter.Builder()
                .setEndpoint(TWITTER_ENDPOINT)
                .setClient(new OkClient(client))
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();

        mAuthenticationInterface =
                mRestAdapter.create(AuthenticationInterface.class);
        mAuthenticationInterface.fetchRequestToken("", new Callback<Response>() {
            @Override
            public void success(Response o, Response response) {
                Log.e(TAG,"success(): Got response " + response.toString());
                Uri uri = getResponseUri(response);

                //Parse out the token and verifier
                String oauthToken = uri.getQueryParameter(OAUTH_TOKEN);
                Uri twitterOauthUri = Uri.parse(TWITTER_OAUTH_ENDPOINT).buildUpon()
                        .appendQueryParameter(OAUTH_TOKEN, oauthToken)
                        .build();
                mWebView.loadUrl(twitterOauthUri.toString());
            }
            @Override
            public void failure(RetrofitError error) {
                Log.e(TAG, "Failed to fetch request token", error);
            }
        });
    }
    private Uri getResponseUri(Response response) {
        String responseBody =
                new String(((TypedByteArray) response.getBody()).getBytes());
        String parseUrl = "http://localhost?" + responseBody;
        return Uri.parse(parseUrl);
    }

    private WebViewClient mWebViewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView webView, String url) {
           Log.e(TAG,"mWebViewClien(): Inside with url " + url.toString());

            if(!url.contains(CALLBACK_URL)) {
                return false;
            }


            Uri callbackUri = Uri.parse(url);
            Log.e(TAG,"Got callbackUri " + callbackUri.toString());
            String oauthToken = callbackUri.getQueryParameter(OAUTH_TOKEN);
            String oauthVerifier = callbackUri.getQueryParameter(OAUTH_VERIFIER);
            Log.e(TAG,"OauthToke: " + oauthToken + " oauthVerifier: " + oauthVerifier);
            mTwitterOauthHelper.setOauthToken(oauthToken,null);

            mAuthenticationInterface.fetchAccessToken(
                    oauthVerifier, new Callback<Response>() {
                        @Override
                        public void success(Response response, Response response2) {
                            Uri uri = getResponseUri(response);
                            String oauthToken = uri.getQueryParameter(OAUTH_TOKEN);
                            String oauthTokenSecret = uri.getQueryParameter(OAUTH_TOKEN_SECRET);

                            mTwitterOauthHelper.setOauthToken(oauthToken, oauthTokenSecret);
                            setupAccount(oauthToken,oauthTokenSecret);

                            final Intent intent = createAccountManagerIntent(oauthToken);

                            setAccountAuthenticatorResult(intent.getExtras());
                            setResult(RESULT_OK,intent);
                        }

                        @Override
                        public void failure(RetrofitError error) {
                        }
                    });
            return true;
        }
    };

    private void setupAccount(String oauthToken, String oauthTokenSecret) {
        String accountType = getIntent().getStringExtra(EXTRA_ACCOUNT_TYPE);
        final Account account = new Account(Authenticator.ACCOUNT_NAME, accountType);
        String authTokenType = getIntent().getStringExtra(EXTRA_AUTH_TYPE);

        AccountManager accountManager =
                AccountManager.get(AuthenticationActivity.this);

        accountManager.addAccountExplicitly(account, null, null);
        accountManager.setAuthToken(account, authTokenType, oauthToken);
        accountManager.setUserData(account, OAUTH_TOKEN_SECRET_KEY, oauthTokenSecret);
    }

    private Intent createAccountManagerIntent(String oauthToken) {
        final Intent intent = new Intent();
        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, Authenticator.ACCOUNT_NAME);
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE,
                getIntent().getStringExtra(EXTRA_ACCOUNT_TYPE));
        intent.putExtra(AccountManager.KEY_AUTHTOKEN, oauthToken);
        return intent;
    }

}
