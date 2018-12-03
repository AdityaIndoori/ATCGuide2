package edu.gmu.ttaconline.atcguide.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import edu.gmu.ttaconline.atcguide.MyApplication;
import edu.gmu.ttaconline.atcguide.PDFLogic;
import edu.gmu.ttaconline.atcguide.PersistenceBean;
import edu.gmu.ttaconline.atcguide.R;

/**
 * The activity that determines if the student have IEP Goals that require Assistive Technology solutions in any instructional areas
 *
 * @author Animesh Jain
 **/
public class IEPGoals extends Activity {
    
    Intent currentIntent;
    String iepGoal = "";
    RadioGroup radioIepGroup;
    private Context context;
    boolean isSample = false;
    
    ImageButton saveBtn;
    ImageButton helpBtn;
    ImageButton homeBtn;
    static Activity activity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Set current view
        setContentView(R.layout.activity_iepgoals);
        activity = this;
        setCustomActionBar();
        //set Context
        context = getApplicationContext();
        //Set current view
        setCurrentIntent();
        isSample = currentIntent.getBooleanExtra("sample", false);
        //set iep goal
        //setIepGoal();
        //set listener
        setNextListener();
        //Calls for next activity
        
    }
    
    /**
     * Sets the Custom action bar to this view
     */
    protected void setCustomActionBar() {
        /*getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
		ActionBar action = getActionBar();
		getActionBar().setDisplayUseLogoEnabled(false);
		getActionBar().setDisplayShowHomeEnabled(false);
		action.setDisplayShowCustomEnabled(true);

		getActionBar().setHomeButtonEnabled(false);
		View v = getLayoutInflater().inflate(R.layout.action_main, null);
//		v.findViewById(R.id.newrecord).setOnClickListener(
//				new OnClickListener() {
//					@Override
//					public void onClick(View v) {
//						//actionNew(v);
//					}
//				});
		//v.findViewById(R.id.helpbutton).setOnClickListener(getHelpListener());
		v.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT));
		getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		getActionBar().setCustomView(v);*/
        saveBtn = (ImageButton) findViewById(R.id.save_record);
        helpBtn = (ImageButton) findViewById(R.id.helpbutton);
        homeBtn = (ImageButton) findViewById(R.id.home);
        
        saveBtn.setOnClickListener(listener);
        helpBtn.setOnClickListener(listener);
        homeBtn.setOnClickListener(listener);
    }
    
    /**
     * Sets the currentIntent from the running intent
     */
    private void setCurrentIntent() {
        //current intent
        currentIntent = getIntent();
        //OR get Intent from database
        currentIntent = PersistenceBean.getExistingIntent(PersistenceBean.getCurrentId(context), context);
        //if it is already set
        setSelected();
        
    }
    
    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.save_record:
                    if (!isSample) {
                        persistIEPGoals();
                        Toast.makeText(context, "Information has been saved.", Toast.LENGTH_LONG).show();
                    }
                    else {
                        Toast.makeText(context, "Sample cannot be modified.", Toast.LENGTH_LONG).show();
                    }
                    break;
                case R.id.helpbutton:
                    Intent intent = new Intent(IEPGoals.this, HelpPage.class);
                    startActivity(intent);
                    break;
                case R.id.home:
                    AlertDialog.Builder info = new AlertDialog.Builder(IEPGoals.this);
                    info.setTitle("Alert");
                    info.setMessage(getResources().getString(
                            R.string.save_notice).toString());
                    info.setCancelable(true);
                    info.setPositiveButton("YES",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    if (!isSample) {
                                        persistIEPGoals();
                                    }
                                    else {
                                        Toast.makeText(context, "Sample cannot be modified.", Toast.LENGTH_LONG).show();
                                    }
                                    ((MyApplication)getApplication()).goHome();
                                }
                            });
                    info.setNeutralButton("NO",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    ((MyApplication)getApplication()).goHome();
                                }
                            });
                    info.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            dialog.cancel();
                        }
                    });
                    AlertDialog infoAlert = info.create();
                    infoAlert.setCanceledOnTouchOutside(false);
                    infoAlert.setCancelable(false);
                    infoAlert.show();
                    break;
            }
        }
    };
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        persistIEPGoals();
    }
    
    /**
     * To set the selected data from the view to the controller layer
     */
    private void setSelected() {
        if (currentIntent.hasExtra("iepGoal")) {
            iepGoal = currentIntent.getStringExtra("iepgoal");
            if (iepGoal.equalsIgnoreCase("yes")) {
                RadioButton r = (RadioButton) findViewById(R.id.iepyes);
                r.setSelected(true);
            } else {
                RadioButton r = (RadioButton) findViewById(R.id.iepno);
                r.setSelected(true);
            }
        }
    }
    
    /**
     * Persists the given IEPGoal answer to the persistence layer as well as to the intent
     */
    private void persistIEPGoals() {
        int selectedId = radioIepGroup.getCheckedRadioButtonId();
        RadioButton radio = (RadioButton) findViewById(selectedId);
        iepGoal = (String) radio.getText();
        //Persisting IEP Goals to current intent
        currentIntent.putExtra("iepgoal", iepGoal);
        if (!isSample) {
            PersistenceBean.persistIntent(currentIntent.getStringExtra("studentid"), currentIntent, context);
        }/* else
            Toast.makeText(context, "Warning: sample data will not be saved", Toast.LENGTH_SHORT).show();*/
    }
    
    /**
     * Set listener to the next button click in the view
     */
    private void setNextListener() {
        radioIepGroup = (RadioGroup) findViewById(R.id.iep);
        Button next = (Button) findViewById(R.id.nextbutton);
        next.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                persistIEPGoals();
                if (iepGoal.equalsIgnoreCase("Yes")) {
                    currentIntent.setClass(context, InstructionalAreas.class);
                    if (getIntent().getBooleanExtra("open", false)){
                        currentIntent.putExtra("open", true);
                    }
                    startActivity(currentIntent);
                } else {
                    if (!isSample) {
                        showDialog();
                    }
                    else {
                        Toast.makeText(IEPGoals.this, "Sample cannot be modified.", Toast.LENGTH_SHORT).show();
                    }
                    //Display alert
                    //PDFBean.fillFormData(PersistenceBean.getPDFData(),this.getData())
                }
            }
        });
    }
    
    private void showDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(
                IEPGoals.this);
        builder.setCancelable(true);
        builder.setTitle("Alert");
        builder.setMessage(getResources().getString(
                R.string.iepgoal_no));
        builder.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        dialog.dismiss();
                        checkPermission();
                        if (permission) {
                            PDFLogic.activity = activity;
                            Toast.makeText(context, "Please Wait", Toast.LENGTH_SHORT).show();
                            ((MyApplication)getApplication()).goHome();
                            Thread pdfThread = new Thread(new PDFLogic());
                            pdfThread.start();
                        }
                    }
                });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }
    
    private static final int MY_PERMISSIONS_REQUEST_WRITE_FILE = 10000;
    private static boolean permission = false;
    
    private void checkPermission(){
        if (ContextCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_FILE);
        }
        else {
            permission = true;
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.iepgoals, menu);
        
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}