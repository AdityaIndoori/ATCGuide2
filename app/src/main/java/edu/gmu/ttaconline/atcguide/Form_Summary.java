package edu.gmu.ttaconline.atcguide;

import android.Manifest;
import android.app.ActionBar;
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
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

import edu.gmu.ttaconline.atcguide.ui.HelpPage;
import edu.gmu.ttaconline.atcguide.ui.InstructionalAreas;
import edu.gmu.ttaconline.atcguide.ui.MainActivity;

public class Form_Summary extends Activity {
	Intent currentIntent;
	Context context;
	Activity activity=this;
	
	ImageButton saveBtn;
	ImageButton helpBtn;
	ImageButton homeBtn;
	
	private int trialNo = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_form__summary);
		setCustomActionBar();
		currentIntent =getIntent();
		context= getApplicationContext();
		
		setIntentFromId(PersistenceBean.getCurrentId(context));
		
		setNextListener();
		//Check the last activity name 
		//Change the display texts
		//Change PDF listener// not required 
		// Set Next Listener
	}
	
	/**
	 * Sets the Custom action bar to this view 
	 */
	protected void setCustomActionBar() {
		saveBtn = (ImageButton) findViewById(R.id.save_record);
		helpBtn = (ImageButton) findViewById(R.id.helpbutton);
		homeBtn = (ImageButton) findViewById(R.id.home);
		
		saveBtn.setOnClickListener(listener);
		helpBtn.setOnClickListener(listener);
		homeBtn.setOnClickListener(listener);
	}
	
	View.OnClickListener listener = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			switch (view.getId()) {
				case R.id.save_record:
					saveInfo();
					break;
				case R.id.helpbutton:
					Intent intent = new Intent(Form_Summary.this, HelpPage.class);
					startActivity(intent);
					break;
				case R.id.home:
					AlertDialog.Builder info = new AlertDialog.Builder(Form_Summary.this);
					info.setTitle("Alert");
					info.setMessage(getResources().getString(
							R.string.save_notice).toString());
					info.setCancelable(true);
					info.setPositiveButton("YES",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
													int id) {
									saveInfo();
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
	
	private void saveInfo(){
		currentIntent = PersistenceBean.getExistingIntent(
				PersistenceBean.getCurrentId(context), context);
		currentIntent.setClass(context, PDFLogic.class);
		currentIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		currentIntent
				.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		if (trialNo == 1) {
			currentIntent.putExtra("trial1", true);
		}
		else{
			currentIntent.putExtra("trial2", true);
		}
		PersistenceBean.persistIntent(
				PersistenceBean.getCurrentId(context), currentIntent,
				context);
	}
	
	/**
	 * @param givenClassName the name of the class from which this is called
	 */
	public void setDisplayTexts(String givenClassName){
		/**Requires: Class name of the last caller class*/
		/**Modifies: Current view*/
		/**Effects: Sets the text in current view according to the caller class*/
		switch (givenClassName){
		
		case "FirstTrial":
			trialNo = 1;
			((TextView)findViewById(R.id.summary_first)).setText(getResources().getString(R.string.firstTrialSummary1));
			((TextView)findViewById(R.id.summary_body)).setText(getResources().getString(R.string.firstTrialSummary2));
			((TextView)findViewById(R.id.summary_body2)).setText(getResources().getString(R.string.firstTrialSummary3));
			((TextView)findViewById(R.id.summary_end)).setText(getResources().getString(R.string.firstTrialSummary4));
			break;
		case "SecondTrial":
			trialNo = 2;
			((TextView)findViewById(R.id.summary_first)).setText(getResources().getString(R.string.secondTrialSummary1));
			((TextView)findViewById(R.id.summary_body)).setText(getResources().getString(R.string.secondTrialSummary2));
			((TextView)findViewById(R.id.summary_body2)).setText(getResources().getString(R.string.secondTrialSummary3));
			((TextView)findViewById(R.id.summary_end)).setText(getResources().getString(R.string.secondTrialSummary4));
			break;
			
		/*case "RevisitFirstTrial":
			trialNo = 3;
			
		case "RevisitSecondTrial":
			trialNo = 4;
			((TextView)findViewById(R.id.summary_first)).setText(getResources().getString(R.string.firstTrialSummary1));
			((TextView)findViewById(R.id.summary_body)).setText(getResources().getString(R.string.firstTrialSummary2));
			((TextView)findViewById(R.id.summary_body2)).setText(getResources().getString(R.string.firstTrialSummary3));
			((TextView)findViewById(R.id.summary_end)).setText(getResources().getString(R.string.firstTrialSummary4));
		*/
			
		}
	}
	
	
	
	/**
	 * @param currentID Current Student ID being used
	 */
	public void setIntentFromId(String currentID){
		
		currentIntent= PersistenceBean.getExistingIntent(PersistenceBean.getCurrentId(context), context);
		String activityName =currentIntent.getStringExtra("activity_name");
		
		if(null == activityName){
		activityName=this.getLocalClassName();
		Log.d("ATGUIDE","Class Name Set:"+activityName);
		}
		
		setDisplayTexts(activityName);
				
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.form__summary, menu);
		return true;
	}

	private void setNextListener() {
		/**
		 * Modifies: Next button in the view Effects: Sets listener to the next
		 * button
		 * */
		Button nextButton = (Button) findViewById(R.id.nextbutton);
		nextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				/*currentIntent = PersistenceBean.getExistingIntent(
						PersistenceBean.getCurrentId(context), context);
//				PersistenceBean.persistAreaObject(areaList, "trial1"
//						+ PersistenceBean.getCurrentId(context), context);
				//PDFLogic.activity = activity;
				currentIntent.setClass(context, PDFLogic.class);
				currentIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				currentIntent
						.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
				// Intent pdfService= new
				currentIntent.putExtra("trial1", true);
				PersistenceBean.persistIntent(
						PersistenceBean.getCurrentId(context), currentIntent,
						context);*/
				saveInfo();
				// Intent(getApplicationContext(),PDFLogic.class);
				android.widget.ProgressBar bar = new android.widget.ProgressBar(
						getApplicationContext());
				bar.setIndeterminate(true);
				bar.bringToFront();
				Log.d("ATGUIDE", "" + currentIntent.toString());
				
				checkPermission();
				if (permission) {
					Toast.makeText(context, "Please Wait", Toast.LENGTH_SHORT).show();
					Thread pdfThread = new Thread(new PDFLogic());
					pdfThread.start();
				}
				
			}
		});

	}
	
	private static final int MY_PERMISSIONS_REQUEST_WRITE_FILE = 10000;
	private boolean permission = false;
	
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
	public void onRequestPermissionsResult(int requestCode,
										   String permissions[], int[] grantResults) {
		switch (requestCode) {
			case MY_PERMISSIONS_REQUEST_WRITE_FILE: {
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					Toast.makeText(context, "Please Wait", Toast.LENGTH_SHORT).show();
					Thread pdfThread = new Thread(new PDFLogic());
					pdfThread.start();
					
				} else {
					permission = false;
					Toast.makeText(activity, "file permission denied.", Toast.LENGTH_SHORT).show();
				}
				return;
			}
			
			// other 'case' lines to check for other
			// permissions this app might request
		}
	}

	@Override
	protected void onRestart() {
	
		super.onRestart();
		Intent intents = new Intent(Form_Summary.this, MainActivity.class);
		intents.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
		            | Intent.FLAG_ACTIVITY_CLEAR_TOP
		            );
		startActivity(intents);
		finish();
		
		
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
