package com.torv.myinstagram;

import android.app.Application;
import android.util.Log;

/**
 * Created by lijian on 5/30/15.
 */
public class InApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        
        SP.instance.init(this);
        VolleyInstance.instance.init(this);
    }
}
