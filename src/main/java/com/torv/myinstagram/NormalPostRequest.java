package com.torv.myinstagram;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONObject;

import java.util.Map;

/**
 * Created by lijian on 5/29/15.
 */
public class NormalPostRequest extends Request<JSONObject>{

    private Map<String, String> mMap;
    private Response.Listener<JSONObject> mListener;

    public NormalPostRequest(String url, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener, Map<String, String> map){
        super(Method.POST, url, errorListener);

        mListener = listener;
        mMap = map;
    }
    @Override
    protected void deliverResponse(JSONObject jsonObject) {
        mListener.onResponse(jsonObject);
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {

        try {
            String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            return Response.success(new JSONObject(jsonString), HttpHeaderParser.parseCacheHeaders(response));
        }catch (Exception e){
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return mMap;
    }
}
