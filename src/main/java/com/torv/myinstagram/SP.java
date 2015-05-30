package com.torv.myinstagram;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by lijian on 5/29/15.
 */
public enum SP {

    instance;

    public SharedPreferences mSharedPreferences;


    public void init(Context context){
        if(null == mSharedPreferences){
            mSharedPreferences = context.getSharedPreferences(context.getApplicationContext().toString(),Context.MODE_PRIVATE);
        }
    }

}
