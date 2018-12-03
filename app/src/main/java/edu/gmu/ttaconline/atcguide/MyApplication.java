package edu.gmu.ttaconline.atcguide;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import edu.gmu.ttaconline.atcguide.ui.IEPGoals;
import edu.gmu.ttaconline.atcguide.ui.MainActivity;

/**
 * Created by yyang31 on 4/9/2018.
 */

public class MyApplication extends Application {
    
    public static Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }
    
    public static Context getInstance(){
        return context;
    }
    
    public void goHome(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
