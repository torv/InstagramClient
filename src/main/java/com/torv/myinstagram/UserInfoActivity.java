package com.torv.myinstagram;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;

import org.json.JSONException;
import org.json.JSONObject;


public class UserInfoActivity extends Activity {

    private TextView mTvUserName;
    private NetworkImageView mNIHeader;
    private TextView mTvFollows;
    private TextView mTvFollowBy;

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

        initUser();

        updateUI();

        requestUserInfo();
    }

    private void updateUI() {

        mTvUserName.setText(user.name);

        mNIHeader.setImageUrl(user.imageUrl, VolleyInstance.getInstance(this).getImageLoader());
    }

    private void initUser() {
        user = new InstagramUser();
        try {
            JSONObject jsonObject = new JSONObject(SPInstance.getInstance(this).getSP().getString(JConstant.SP_KEY_ACCESS_TOKEN_JSON, null));
            access_token = jsonObject.getString("access_token");
            JSONObject userJson = jsonObject.getJSONObject("user");

            user.id = userJson.getString("id");
            user.name = userJson.getString("username");
            user.full_name = userJson.getString("full_name");
            user.imageUrl = userJson.getString("profile_picture");

        } catch (JSONException e) {
            e.printStackTrace();
        }
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
            JSONObject counts = json.getJSONObject("counts");

            user.followed_by = counts.getInt("followed_by");
            user.follows = counts.getInt("follows");
            mTvFollowBy.setText("Followed by:"+user.followed_by);
            mTvFollows.setText("Follows:"+user.follows);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}