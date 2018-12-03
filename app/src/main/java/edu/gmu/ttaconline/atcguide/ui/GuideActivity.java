package edu.gmu.ttaconline.atcguide.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import edu.gmu.ttaconline.atcguide.IEPReading;
import edu.gmu.ttaconline.atcguide.PersistenceBean;
import edu.gmu.ttaconline.atcguide.R;
import edu.gmu.ttaconline.atcguide.TaskForm;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class GuideActivity extends Activity {
   
    TextView screen;
    long delay1 = 3000, delay2 = 8000;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
        screen = (TextView) findViewById(R.id.fullscreen_content);
        screen.setBackground(getResources().getDrawable(R.drawable.guide1));
        //if sample doesn't exist, insert sample data
        if (!PersistenceBean.checkSample(this)){
            PersistenceBean.insertSample(this);
        }
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Message msg = new Message();
                msg.what = 100001;
                handler.sendMessage(msg);
            }
        };
        timer.schedule(task, delay1);
        Timer timer2 = new Timer();
        TimerTask task2 = new TimerTask() {
            @Override
            public void run() {
                Intent intent = new Intent(GuideActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        };
        timer2.schedule(task2, delay2);
    }
    
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 100001){
                screen.setBackground(getResources().getDrawable(R.drawable.guide2));
            }
        }
    };
    
}
