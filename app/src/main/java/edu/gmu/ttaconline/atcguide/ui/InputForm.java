package edu.gmu.ttaconline.atcguide.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

import edu.gmu.ttaconline.atcguide.PDFLogic;
import edu.gmu.ttaconline.atcguide.PersistenceBean;
import edu.gmu.ttaconline.atcguide.R;

/**
 * InputForm Activity to begin the fill data transaction for this student
 * 
 * @author Animesh Jain
 * 
 */
public class InputForm extends Activity {

	Context context;
	Intent currentIntent;
	String studentid;
	String studentgrade;
	String studentschool;
	String studentparticipant;
	int day;
	int month;
	int year;
	boolean isSample;
	
	ImageButton saveBtn;
	ImageButton helpBtn;
	ImageButton homeBtn;
	Intent saveIntent;
	
	boolean open = false;
	//if all information has been filled, can go to next page
	boolean fullInfo = false;
	//if studentId format incorrect
	boolean idFormatRight = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = getApplicationContext();
		setContentView(R.layout.activity_input_form);
		setCustomActionBar();
		currentIntent = getIntent();
		open = currentIntent.getBooleanExtra("open", false);
		// case open : or if exists PersistedIntent(currentStudentId)
		// PersistenceBean.getExistingIntent(currentIntent.getStringExtra("studentid"),
		// context);
		// fill form
		if(open)
		{	currentIntent = PersistenceBean.getExistingIntent(currentIntent.getStringExtra("studentid"), context);
			PersistenceBean.persistCurrentId(currentIntent.getStringExtra("studentid"), context);
		    currentIntent.putExtra("open",true);
		    Log.i("open"," in InputForm success");
		    fillDataFromIntent();
		}
		isSample = currentIntent.getBooleanExtra("sample", false);
		// else blank form
		// case Preview : fillPDF(Intent)
		Button b = (Button) findViewById(R.id.nextbutton);
		b.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				saveData(v);
				if (fullInfo) {
					startActivity(saveIntent);
				}
				else {
					Toast.makeText(InputForm.this, getResources().getString(R.string.field_uncomplete), Toast.LENGTH_SHORT).show();
				}
			}
		});
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
        public void onClick(final View view) {
            switch (view.getId()){
                case R.id.save_record:
                	if (!isSample) {
						saveData(view);
						Toast.makeText(context, "Information has been saved.", Toast.LENGTH_LONG).show();
					}
					else {
						Toast.makeText(context, "Sample cannot be modified.", Toast.LENGTH_LONG).show();
					}
                    break;
                case R.id.helpbutton:
					Intent intent = new Intent(InputForm.this, HelpPage.class);
					startActivity(intent);
                    break;
                case R.id.home:
					AlertDialog.Builder info = new AlertDialog.Builder(InputForm.this);
					info.setTitle("Alert");
					info.setMessage(getResources().getString(
							R.string.save_notice).toString());
					info.setCancelable(true);
					info.setPositiveButton("YES",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
													int id) {
									if (!isSample) {
										saveData(view);
										if (!idFormatRight){
											dialog.dismiss();
											savingFailedDialog();
										}
										else {
											finish();
										}
									}
									else {
										Toast.makeText(context, "Sample cannot be modified.", Toast.LENGTH_LONG).show();
										finish();
									}
								}
							});
					info.setNeutralButton("NO",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
													int id) {
									finish();
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
	
	private void savingFailedDialog(){
		AlertDialog.Builder info = new AlertDialog.Builder(InputForm.this);
		info.setTitle("Error");
		info.setMessage(getResources().getString(
				R.string.save_id_failed).toString());
		info.setCancelable(true);
		info.setPositiveButton("OK",
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,
									int id) {
					finish();
				}
			});
		AlertDialog infoAlert = info.create();
		infoAlert.setCanceledOnTouchOutside(false);
		infoAlert.setCancelable(false);
		infoAlert.show();
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}
	
	/**
	 * 
	 */
	private void fillDataFromIntent() {
		studentid =currentIntent.getStringExtra("studentidrecord");
		studentgrade=currentIntent.getStringExtra("studentgrade");
		studentschool = currentIntent.getStringExtra("studentschool");
		studentparticipant = currentIntent.getStringExtra("studentparticipant");
		day = currentIntent.getIntExtra("day",31);
		month = currentIntent.getIntExtra("month",1);
		year = currentIntent.getIntExtra("year",2017);
		setFormData();
	}

	/**
	 * Set form data from the businessLogic to the view.
	 */
	private void setFormData() {
		EditText sid = ((EditText) findViewById(R.id.studentid));
		sid.setText(studentid);
		Spinner studentgrade = ((Spinner) (findViewById(R.id.studentgrade)));
		int i=0;
		for(String grades:getResources().getStringArray(R.array.gradearray)){
				if(grades.equalsIgnoreCase(this.studentgrade))
					break;
			i++;
		}
		studentgrade.setSelection(i);
		EditText school = ((EditText) (findViewById(R.id.studentschool)));
		school.setText(studentschool);
		EditText participant = ((EditText) (findViewById(R.id.participants)));
		participant.setText(studentparticipant);
		DatePicker datePicker = (DatePicker) findViewById(R.id.date);
		datePicker.updateDate(year, month, day);
		datePicker.refreshDrawableState();
	}

	
	/**
	 *Save Data from the view v
	 */
	private void saveData(View v) {
		idFormatRight = true;
		v = v.getRootView();
		String studentid = ((EditText) (v.findViewById(R.id.studentid)))
				.getText().toString();
		String studentgrade = ((Spinner) (v.findViewById(R.id.studentgrade)))
				.getSelectedItem().toString();
		String studentschool = ((EditText) (v.findViewById(R.id.studentschool)))
				.getText().toString();
		String studentparticipant = ((EditText) (v
				.findViewById(R.id.participants))).getText().toString();
		DatePicker datePicker = (DatePicker) v.findViewById(R.id.date);
		int day = datePicker.getDayOfMonth();
		int month = datePicker.getMonth() + 1;
		int year = datePicker.getYear();
		// Date date = new java.util.Date(year, month, day);
		String date = month + "-" + day + "-" + year;
		if(studentid == null || TextUtils.isEmpty(studentid) || studentid.length() <3 || studentid.length() >9){
			idFormatRight = false;
			Toast.makeText(InputForm.this, getResources().getString(R.string.field_student_id), Toast.LENGTH_LONG).show();
		}
		else {
			int letterNum = 0;
			for(int i=0; i<studentid.length(); i++){
				if(Character.isLetter(studentid.charAt(i))){
					letterNum++;
				}
			}
			if(letterNum > 2){
				idFormatRight = false;
				Toast.makeText(InputForm.this, getResources().getString(R.string.field_student_id_info), Toast.LENGTH_LONG).show();
			}
		}
		if (idFormatRight){
			if (studentschool == null || TextUtils.isEmpty(studentschool)){
				fullInfo = false;
			}
			else if (studentparticipant == null || TextUtils.isEmpty(studentparticipant)){
				fullInfo = false;
			}
			else {
				fullInfo = true;
			}
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");// HH:mm:ss
			Date realDate = new Date(System.currentTimeMillis());
			saveIntent = currentIntent;
			if (currentIntent.getStringExtra("studentid")==null){
				saveIntent.putExtra("studentid", studentid+simpleDateFormat.format(realDate));
			}
			else {
				saveIntent.putExtra("studentid",currentIntent.getStringExtra("studentid"));
			}
			saveIntent.putExtra("studentidrecord", studentid);
			saveIntent.putExtra("studentgrade", studentgrade);
			saveIntent.putExtra("studentparticipant", studentparticipant);
			saveIntent.putExtra("studentschool", studentschool);
			saveIntent.putExtra("date", date.toString());
			saveIntent.putExtra("day", day);
			saveIntent.putExtra("month", month);
			saveIntent.putExtra("year", year);
			Log.d(PDFLogic.LOG_TAG, "Date: " + date);
			saveIntent.setClass(context, IEPGoals.class);
			if (!isSample) {
				PersistenceBean.persistIntent(saveIntent.getStringExtra("studentid"),
						saveIntent, context);
				PersistenceBean.persistCurrentId(saveIntent.getStringExtra("studentid"), context);
			}
			/*else {
				Toast.makeText(InputForm.this, "Sample cannot be modified.", Toast.LENGTH_SHORT).show();
			}*/
		}
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.input_form, menu);
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
