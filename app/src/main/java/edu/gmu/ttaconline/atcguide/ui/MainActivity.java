package edu.gmu.ttaconline.atcguide.ui;

import java.util.ArrayList;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import edu.gmu.ttaconline.atcguide.PDFLogic;
import edu.gmu.ttaconline.atcguide.PersistenceBean;
import edu.gmu.ttaconline.atcguide.R;
import edu.gmu.ttaconline.atcguide.RevisitFirstTrial;
import edu.gmu.ttaconline.atcguide.RevisitSecondTrial;
import edu.gmu.ttaconline.atcguide.StudentBean;

@SuppressLint("InflateParams")
public class MainActivity extends Activity {
	static Context context;
	static Activity activity = null;
	static boolean agree = false;

	ImageButton newBtn;
	ImageButton helpBtn;
	private static String tag = "";
	
	//check if student have trial record
	int trial;
	private static boolean isSample = false;

	@SuppressLint("InflateParams")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		context = getApplicationContext();
		activity = this;

		setContentView(R.layout.activity_main);
		setCustomActionBar();
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		if (!agree) {
			showAgreementDialog();
		}
	}
	
	/**
	 * display agreement of this app
	 */
	private void showAgreementDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(
				MainActivity.this);
		builder.setCancelable(true);
		builder.setTitle(getResources().getString(R.string.agreement_title));
		builder.setMessage(getResources().getString(
				R.string.agreement));
		builder.setPositiveButton("Agree",
				new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog,
										int which) {
						dialog.dismiss();
						agree = true;
					}
				});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				showForceDialog();
			}
		});
		builder.setCancelable(false);
		builder.show();
	}
	
	/**
	 * display agreement alert dialog:
	 * user must agree to the agreement
	 */
	private void showForceDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(
				MainActivity.this);
		builder.setCancelable(true);
		builder.setTitle("Alert");
		builder.setMessage(getResources().getString(
				R.string.agreement_alert));
		builder.setPositiveButton("Ok",
				new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog,
										int which) {
						dialog.dismiss();
						showAgreementDialog();
					}
				});
		builder.setCancelable(false);
		builder.show();
	}
	

	protected void setCustomActionBar() {
		helpBtn = (ImageButton) findViewById(R.id.helpbutton);
		newBtn = (ImageButton) findViewById(R.id.newrecord);
		helpBtn.setOnClickListener(listener);
		newBtn.setOnClickListener(listener);

	}

	OnClickListener listener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			switch (view.getId()){
				case R.id.newrecord:
					actionNew(view);
					break;
				case R.id.helpbutton:
					Intent intent = new Intent(MainActivity.this, HelpPage.class);
					startActivity(intent);
					break;
				default:
					break;
			}
		}
	};

	@Override
	protected void onResume() {
		super.onResume();
		getFragmentManager().beginTransaction()
				.add(R.id.container, new PlaceholderFragment()).commit();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		// recreate();
		getFragmentManager().beginTransaction()
				.add(R.id.container, new PlaceholderFragment()).commit();

	}

	public void actionNew(View v) {
		Intent i = new Intent(context, InputForm.class);
		Toast.makeText(context, "Enter student data", Toast.LENGTH_SHORT)
				.show();
		startActivity(i);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		private LayoutInflater inflater;
		private View rootView;
		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			this.inflater = inflater;
			rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			fillStudentData(inflater, rootView);
			return rootView;
		}

		public void actionNew(View b) {
			Intent i = new Intent(context, InputForm.class);
			Toast.makeText(context, "Enter student data", Toast.LENGTH_SHORT)
					.show();
			startActivity(i);
		}

		@SuppressLint("InflateParams")
		private void fillStudentData(LayoutInflater inflater, View mainFragment) {
			TableLayout allStudentsTable = (TableLayout) mainFragment
					.findViewById(R.id.studentData);
			ArrayList<StudentBean> studentList = PersistenceBean.getStudentList(context);
			if (allStudentsTable != null) {
				allStudentsTable.removeAllViews();
				for (StudentBean studentid : studentList) {
					int trial = 0;
					TableRow r = (TableRow) inflater
							.inflate(R.layout.row, null);
					// r.setLayoutParams(new LayoutParams(
					// LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
					r.setLayoutParams(new LayoutParams(
							LayoutParams.MATCH_PARENT,
							LayoutParams.WRAP_CONTENT));
					r.setPadding(0, 0, 0, 0);
					TextView tv = (TextView) r.findViewById(R.id.textView1);
					tv.setText(studentid.getRecordId());
					Button open = (Button) r.findViewById(R.id.open);
					open.setOnClickListener(clickListener);
					open.setTag(studentid.getStudentid());
					
					Button trial1 = (Button) r.findViewById(R.id.trial1);
					trial1.setTag(studentid.getStudentid());

					/*Button trial2 = (Button) r.findViewById(R.id.trial2);
					trial2.setTag(studentid.getStudentid());*/
					trial = PersistenceBean.getTrialRecord(studentid.getStudentid(), context);
					trial1.setText("Trial");
					if (trial == 2){
						/*trial2.setVisibility(View.VISIBLE);
						trial2.setOnClickListener(clickListener);*/
						trial1.setVisibility(View.VISIBLE);
						trial1.setOnClickListener(clickListener);
					}
					else if (trial == 1){
//						trial2.setVisibility(View.INVISIBLE);
						trial1.setVisibility(View.VISIBLE);
						trial1.setOnClickListener(clickListener);
					}
					else {
						trial1.setVisibility(View.INVISIBLE);
//						trial2.setVisibility(View.INVISIBLE);
					}
					Button preview = (Button) r.findViewById(R.id.preview);
					preview.setOnClickListener(clickListener);
					preview.setTag(studentid.getStudentid());
					
					Intent currentIntent = PersistenceBean.getExistingIntent(
							studentid.getStudentid(), context);
					isSample = currentIntent.getBooleanExtra("sample", false);
					if (isSample) PersistenceBean.deleteNavigatorRecord(context, currentIntent.getStringExtra("studentid"), "AEM Navigator");
					Button delete = (Button) r
							.findViewById(R.id.deleterecordbutton);
					if (!isSample) {
						delete.setTag(studentid.getStudentid());
						delete.setOnClickListener(clickListener);
					}
					else {
						delete.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View view) {
								Toast.makeText(context, "Sample cannot be deleted.", Toast.LENGTH_SHORT).show();
							}
						});
					}
					allStudentsTable.addView(r);
					allStudentsTable.bringToFront();
					r.bringToFront();
					// Button x = (Button) mainFragment.findViewById(R.id.);
					// x.setX(100);
					// x.setY(100);
					// x.setBackgroundColor(Color.RED);
				}
			}
		}

		OnClickListener clickListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				tag = (String) v.getTag();

				switch (v.getId()) {
				case R.id.open:
					Intent i = new Intent(context, InputForm.class);
					i.putExtra("open", true);
					if (tag != null)
						i.putExtra("studentid", tag);
					startActivity(i);
					break;
				case R.id.trial1:
					loadFirstTrial(tag);
					break;
				/*case R.id.trial2:
					loadSecondTrial(tag);
					break;*/
				case R.id.preview:
					checkPermission();
					if (permission) {
						//toast("Opening...");
						previewRecord(tag);
					}
					break;
				case R.id.deleterecordbutton:
					AlertDialog.Builder info = new AlertDialog.Builder(activity);
					info.setTitle("Alert");
					info.setMessage(getResources().getString(
							R.string.delete_notice).toString());
					info.setCancelable(true);
					info.setPositiveButton("YES",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
													int id) {
									PersistenceBean.deleteStudent(tag, context);
									fillStudentData(inflater, rootView);
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
					
				}
			}

		};

	}
	
	private static final int MY_PERMISSIONS_REQUEST_WRITE_FILE = 10000;
	private static boolean permission = false;
	
	private static void checkPermission(){
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
					toast("Opening...");
					previewRecord(tag);
					
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

	public static void log(String string) {
		Log.d("ATGUIDE", string);
	}

	public static void toast(String string) {
		Toast.makeText(context, string, Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public void onBackPressed() {
		moveTaskToBack(isTaskRoot());
	}
	
	public static void previewRecord(String studentId) {
		PDFLogic.activity = MainActivity.activity;
		PersistenceBean.persistCurrentId(studentId, context);
		Intent currentIntent = PersistenceBean.getExistingIntent(studentId,
				context);
		currentIntent.setClass(context, PDFLogic.class);
		currentIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		currentIntent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		android.widget.ProgressBar bar = new android.widget.ProgressBar(context);
		bar.setIndeterminate(true);
		bar.bringToFront();
		MainActivity.activity.startService(currentIntent);
	}

	public static void loadFirstTrial(String studentId) {
		// PDFLogic.activity = MainActivity.activity;
		PersistenceBean.persistCurrentId(studentId, context);
		Intent currentIntent = PersistenceBean.getExistingIntent(studentId,
				context);
		currentIntent.putExtra("studentid", studentId);
		// Intent pdfService= new
		currentIntent.putExtra("revisitTrial1", true);
		// Intent(activity.getApplicationContext(),PDFLogic.class);
		currentIntent.setClass(context, RevisitFirstTrial.class);
		// currentIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// currentIntent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		// Intent pdfService= new
		// Intent(getApplicationContext(),PDFLogic.class);
		// android.widget.ProgressBar bar = new
		// android.widget.ProgressBar(context);
		// bar.setIndeterminate(true);
		// bar.bringToFront();
		MainActivity.activity.startActivity(currentIntent);

	}

	public static void loadSecondTrial(String studentId) {

		PersistenceBean.persistCurrentId(studentId, context);
		Intent currentIntent = PersistenceBean.getExistingIntent(studentId,
				context);
		currentIntent.putExtra("open", true);
		currentIntent.putExtra("revisitTrial2", true);
		currentIntent.setClass(context, RevisitSecondTrial.class);
		MainActivity.activity.startActivity(currentIntent);
	}
}
