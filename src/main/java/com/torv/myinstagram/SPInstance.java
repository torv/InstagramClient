package com.torv.myinstagram;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by lijian on 5/29/15.
 */
public class SPInstance {

    private static SPInstance instance;

    private SharedPreferences mSharedPreferences;


    private SPInstance(Context context){
        if(null == mSharedPreferences){
            mSharedPreferences = context.getSharedPreferences(context.getApplicationContext().toString(),Context.MODE_PRIVATE);
        }
    }

    public static synchronized SPInstance getInstance(Context context){
        if(null == instance){
            instance = new SPInstance(context);
        }
        return instance;
    }

    public SharedPreferences getSP(){
        return mSharedPreferences;
    }
}
