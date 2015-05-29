package com.torv.myinstagram;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {

    private WebView mWvLoginPage;

    private ProgressBar mPbLoadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.e("torv", "onCreate");

        if(SPInstance.getInstance(this).getSP().getString(JConstant.SP_KEY_ACCESS_TOKEN_JSON, null) != null){
            gotoMainPage();
            return;
        }

        setupWebView();
        startServerAuth();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {

        mPbLoadingBar = (ProgressBar) findViewById(R.id.pb_load_page);

        mWvLoginPage = (WebView) findViewById(R.id.wv_load_login_page);
        mWvLoginPage.setVerticalScrollBarEnabled(false);
        mWvLoginPage.setHorizontalScrollBarEnabled(false);
        mWvLoginPage.setWebViewClient(new LoginWebViewClient());
        mWvLoginPage.getSettings().setJavaScriptEnabled(true);
        mWvLoginPage.getSettings().setAppCacheEnabled(false);
        mWvLoginPage.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
    }

    private void startServerAuth() {
        String authUrl = JConstant.SERVER_AUTH_URL + "client_id=" + JConstant.CLIENT_ID + "&redirect_uri=" + JConstant.REDIRECT_URL + "&response_type=code";
        mWvLoginPage.loadUrl(authUrl);
        mPbLoadingBar.setVisibility(View.VISIBLE);
    }
    
    private void startClientAuth(){
        String authUrl = JConstant.CLIENT_AUTH_URL+"client_id="+JConstant.CLIENT_ID+"&redirect_uri="+JConstant.REDIRECT_URL+"&response_type=token";
        mWvLoginPage.loadUrl(authUrl);
        mPbLoadingBar.setVisibility(View.VISIBLE);
    }

    class LoginWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.e("torv", "shouldOverrideUrlLoading:" + url);

            if (url.contains("error")) {
                Toast.makeText(MainActivity.this, "User Cancelled", Toast.LENGTH_LONG).show();
                return true;
            }
            if (url.contains("success")) {
                Toast.makeText(MainActivity.this, "Logged", Toast.LENGTH_LONG).show();
                return true;
            }

            if (url.startsWith(JConstant.REDIRECT_URL)) {

                String temp[] = url.split("=");
                if (url.contains("code")) {

                    Log.e("torv", "code:" + temp[1]);
                    requestAccessTokenByVolley(temp[1]);

                } else if (url.contains("error")) {

                    Log.e("torv", "error:" + temp[1]);
                }
                return true;
            }

            return false;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            mPbLoadingBar.setVisibility(View.GONE);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            mPbLoadingBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            Log.e("torv", "onReceivedError:" + description);
        }
    }

    private void requestAccessTokenByVolley(String code) {

        Map<String, String> map = new HashMap<String, String>();
        map.put("client_id", JConstant.CLIENT_ID);
        map.put("client_secret", JConstant.CLIENT_SECRET);
        map.put("grant_type", "authorization_code");
        map.put("redirect_uri", JConstant.REDIRECT_URL);
        map.put("code", code);

        Request<JSONObject> request = new NormalPostRequest(JConstant.ACCESS_TOKEN_URL, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                Log.e("torv", "" + jsonObject);
                handleAccessToken(jsonObject);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e("torv", "error");
            }
        }, map);

        request.setTag("jsObjRequest");
        VolleyInstance.getInstance(this).addToRequestQueue(request);
    }

    private void handleAccessToken(JSONObject jsonObject){

        SPInstance.getInstance(this).getSP().edit().putString(JConstant.SP_KEY_ACCESS_TOKEN_JSON, jsonObject.toString()).commit();
        gotoMainPage();
    }

    public void gotoMainPage(){
        Intent intent = new Intent(MainActivity.this, UserInfoActivity.class);
        startActivity(intent);
        finish();
    }
}
