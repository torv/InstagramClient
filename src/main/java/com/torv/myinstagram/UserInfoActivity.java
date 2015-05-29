package com.torv.myinstagram;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;


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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        mNIHeader = (NetworkImageView) findViewById(R.id.ni_header);
        mTvUserName = (TextView) findViewById(R.id.tv_username);
        mTvFollows = (TextView)findViewById(R.id.tv_follows);
        mTvFollowBy = (TextView)findViewById(R.id.tv_followed_by);
        mTvMediaCount = (TextView)findViewById(R.id.tv_media_count);
        mTvBio = (TextView)findViewById(R.id.tv_bio);
        mTvWebsite = (TextView)findViewById(R.id.tv_website);

        initUser();

        updateUI();

    }

    private void updateUI() {

        mTvUserName.setText(user.name);
        mTvFollowBy.setText("Followed by:"+user.followed_by);
        mTvFollows.setText("Follows:"+user.follows);
        mTvBio.setText("Bio:"+user.bio);
        mTvMediaCount.setText("Media:"+user.media_count);
        mTvWebsite.setText("Website:"+user.website);

        mNIHeader.setImageUrl(user.imageUrl, VolleyInstance.getInstance(this).getImageLoader());
    }

    private void initUser() {

        access_token = SPInstance.getInstance(this).getSP().getString(JConstant.SP_KEY_ACCESS_TOKEN, null);

        String userString = SPInstance.getInstance(this).getSP().getString(JConstant.SP_KEY_INSTAGRAM_USER, null);
        if(null != userString){
            Gson gson = new Gson();
            user = gson.fromJson(userString, InstagramUser.class);
        }else {
            user = new InstagramUser();
        }

        requestUserInfo();
    }

    public void requestUserInfo(){

        String url = "https://api.instagram.com/v1/users/self/?access_token="+access_token;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                Log.e("torv", ""+jsonObject);
                handleUserInfoJson(jsonObject);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e("torv", "error");
            }
        });

        jsonObjectRequest.setTag("/users/");
        VolleyInstance.getInstance(this).addToRequestQueue(jsonObjectRequest);
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
            SPInstance.getInstance(this).getSP().edit().putString(JConstant.SP_KEY_INSTAGRAM_USER,userString).commit();

            updateUI();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}