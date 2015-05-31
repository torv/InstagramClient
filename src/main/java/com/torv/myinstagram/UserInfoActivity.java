package com.torv.myinstagram;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class UserInfoActivity extends Activity {

    private TextView mTvUserName;
    private NetworkImageView mNIHeader;
    private TextView mTvFollows;
    private TextView mTvFollowBy;
    private TextView mTvMediaCount;
    private TextView mTvBio;
    private TextView mTvWebsite;

    private InstagramUser user;

    private String access_token;

    private Button mBtnLogout;

    private GridView mGvMedias;
    private GridMediaAdapter mGridAdapter;
    private List<String> mUrlList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        mNIHeader = (NetworkImageView) findViewById(R.id.ni_header);
        mTvUserName = (TextView) findViewById(R.id.tv_username);
        mTvFollows = (TextView) findViewById(R.id.tv_follows);
        mTvFollowBy = (TextView) findViewById(R.id.tv_followed_by);
        mTvMediaCount = (TextView) findViewById(R.id.tv_media_count);
        mTvBio = (TextView) findViewById(R.id.tv_bio);
        mTvWebsite = (TextView) findViewById(R.id.tv_website);

        mBtnLogout = (Button) findViewById(R.id.btn_logout);
        mBtnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleLogout();
            }
        });

        initUser();

        initGridMedia();
    }

    private void initUser() {

        access_token = SP.instance.mSharedPreferences.getString(JConstant.SP_KEY_ACCESS_TOKEN, null);

        String userString = SP.instance.mSharedPreferences.getString(JConstant.SP_KEY_INSTAGRAM_USER, null);
        if (null != userString) {
            Gson gson = new Gson();
            user = gson.fromJson(userString, InstagramUser.class);
        } else {
            user = new InstagramUser();
        }

        updateUI();

        requestUserInfo();
    }

    private void initGridMedia() {

        mGvMedias = (GridView) findViewById(R.id.gv_medias);

        String strMedaList = SP.instance.mSharedPreferences.getString(JConstant.SP_KEY_MEDIA_LIST, null);
        if (strMedaList != null) {

            Gson gson = new Gson();
            Type type = new TypeToken<List<String>>() {
            }.getType();

            mUrlList = gson.fromJson(strMedaList, type);
            mGridAdapter = new GridMediaAdapter(this, mUrlList);
            mGvMedias.setAdapter(mGridAdapter);
        }

        requestMediaInfo();
    }

    public void requestUserInfo() {

        String url = "https://api.instagram.com/v1/users/self/?access_token=" + access_token;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                Log.e("torv", "" + jsonObject);
                handleUserInfoJson(jsonObject);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e("torv", "error");
            }
        });

        jsonObjectRequest.setTag("/users/");
        VolleyInstance.instance.addToRequestQueue(jsonObjectRequest);
    }

    private void handleUserInfoJson(JSONObject jsonObject) {

        try {
            Log.e("torv", "handleUserInfo");
            JSONObject json = jsonObject.getJSONObject("data");
            user.id = json.getString("id");
            user.name = json.getString("username");
            user.full_name = json.getString("full_name");
            user.imageUrl = json.getString("profile_picture");
            user.bio = json.getString("bio");
            user.website = json.getString("website");

            JSONObject counts = json.getJSONObject("counts");
            user.media_count = counts.getInt("media");
            user.followed_by = counts.getInt("followed_by");
            user.follows = counts.getInt("follows");

            Gson gson = new Gson();
            String userString = gson.toJson(user);
            SP.instance.mSharedPreferences.edit().putString(JConstant.SP_KEY_INSTAGRAM_USER, userString).commit();

            updateUI();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void requestMediaInfo() {

        String url = "https://api.instagram.com/v1/users/self/media/recent/?access_token=" + access_token;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                Log.e("torv", "" + jsonObject);
                handleMediaInfoJson(jsonObject);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e("torv", "error");
            }
        });

        jsonObjectRequest.setTag("/media/");
        VolleyInstance.instance.addToRequestQueue(jsonObjectRequest);
    }

    private void handleMediaInfoJson(JSONObject jsonObject) {

        try {
            JSONArray jsonArray = jsonObject.getJSONArray("data");
            int length = jsonArray.length();

            if (null == mUrlList) {
                mUrlList = new ArrayList<String>();
            }

            if (length > 0) {
                mUrlList.clear();
            }

            for (int i = 0; i < length; i++) {
                Log.e("torv", ""+i);
                JSONObject json = jsonArray.getJSONObject(i);
                JSONObject jsonImage = json.getJSONObject("images");
                JSONObject jsonLowReso = jsonImage.getJSONObject("low_resolution");
                mUrlList.add(jsonLowReso.getString("url"));
            }

            if(null == mGridAdapter){
                mGridAdapter = new GridMediaAdapter(this, mUrlList);
                mGvMedias.setAdapter(mGridAdapter);
            }else{
                Log.e("torv", "notifyDataSetChanged");
                mGridAdapter.notifyDataSetChanged();
            }

            Gson gson = new Gson();
            String strMediaList = gson.toJson(mUrlList);
            SP.instance.mSharedPreferences.edit().putString(JConstant.SP_KEY_MEDIA_LIST, strMediaList).commit();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handleLogout() {

        CookieSyncManager.createInstance(this);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();

        SP.instance.mSharedPreferences.edit().putString(JConstant.SP_KEY_INSTAGRAM_USER, null).commit();
        SP.instance.mSharedPreferences.edit().putString(JConstant.SP_KEY_ACCESS_TOKEN, null).commit();
        SP.instance.mSharedPreferences.edit().putString(JConstant.SP_KEY_MEDIA_LIST, null).commit();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void updateUI() {

        mTvUserName.setText(user.name);
        mTvFollowBy.setText("Followed by:" + user.followed_by);
        mTvFollows.setText("Follows:" + user.follows);
        mTvBio.setText("Bio:" + user.bio);
        mTvMediaCount.setText("Media:" + user.media_count);
        mTvWebsite.setText("Website:" + user.website);

        mNIHeader.setImageUrl(user.imageUrl, VolleyInstance.instance.getImageLoader());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("torv", "onDestroy");

    }
}