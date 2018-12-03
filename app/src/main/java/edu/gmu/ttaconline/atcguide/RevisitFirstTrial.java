package edu.gmu.ttaconline.atcguide;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import com.commonsware.cwac.merge.MergeAdapter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

import edu.gmu.ttaconline.atcguide.ui.HelpPage;
import edu.gmu.ttaconline.atcguide.ui.InstructionalAreas;

public class RevisitFirstTrial extends FragmentActivity {
	/*
	 * Overview: Type inheriting from activity for the first trial of the AT
	 * trials
	 */
	/* Instance variables */
	ArrayList<Area> areaList;// Store the lists of all areas
	ArrayList<String> trial1Texts = new ArrayList<String>();// store text only
	//only add the AT without solutions
	ArrayList<Area> trial2List = new ArrayList<Area>();;
	ArrayList<String> trial2TextList = new ArrayList<String>();
	String studentid = "";
	Calendar myCalendar = Calendar.getInstance();
	EditText datePick;
	MergeAdapter merge = new MergeAdapter();
	Context context;// store current context
	LayoutInflater inflater;// store the layout inflator to inflate rows
	int id = 1001;// default id
	static int clickedId = 9999;// current clicked id
	private ArrayList<CharSequence> selectedInstructional; // list of selected
	TextWatcher _ATNameWatcher, participantsWatcher, trialDateWatcher,
			_ActionToWatcher;
	android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
	static Activity activity;
	Intent currentIntent;
	boolean exploreVA = false;
	String exploringVA = "AEM Navigator";
	boolean open = false;
	boolean trial2 = false;
	String URI__REVISIT_FIRST_TRIAL = "http://revisitTrial1.atguide.com";
	
	ImageButton saveBtn;
	ImageButton helpBtn;
	ImageButton homeBtn;

	private boolean isSample = false;
	/* Methods */
	// Control start point
	/**
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_revisit_first_trial);
		setCustomActionBar();
		
		context = getApplicationContext();
		areaList = PersistenceBean.getPersistedAreaObjects("trial1"
				+ PersistenceBean.getCurrentId(context), context);
		inflater = getLayoutInflater();
		// Get existing useful data from intent and SQLite

		try {
			getData();
			checkUri();
			// if not open
			placeAreaFromDB();
			studentid = getIntent().getStringExtra("studentid");
			activity = this;
			datePick = (EditText) findViewById(R.id.date);
			// setATListener();
			setDatePickListener();
			clickFirstItem();
			setNextListener();
		} catch (Exception e) {
			Log.e("AT GUIDE", "Exception in FirstTrial.onCreate 104: *" + e);
		}
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
					if (!isSample){
						//save trial1 information
						PersistenceBean.persistAreaObject(areaList, "trial1"
								+ PersistenceBean.getCurrentId(context), context);
						PersistenceBean.persistAreaObject(
								trial2List, "trial2" + studentid,
								context);
						PersistenceBean.persistInstructionalAreas(
								"trial2" + studentid,
								trial2TextList, context);
						Toast.makeText(context, "Information has been saved.", Toast.LENGTH_LONG).show();
					}
					else {
						Toast.makeText(context, "Sample cannot be modified.", Toast.LENGTH_LONG).show();
					}
					break;
				case R.id.helpbutton:
					Intent intent = new Intent(RevisitFirstTrial.this, HelpPage.class);
					startActivity(intent);
					break;
				case R.id.home:
					//save trial1 information
					AlertDialog.Builder info = new AlertDialog.Builder(RevisitFirstTrial.this);
					info.setTitle("Alert");
					info.setMessage(getResources().getString(
							R.string.save_notice).toString());
					info.setCancelable(true);
					info.setPositiveButton("YES",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
													int id) {
									if (!isSample) {
										PersistenceBean.persistAreaObject(areaList, "trial1"
												+ PersistenceBean.getCurrentId(context), context);
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
	
	
	private void checkUri() {
		Uri uri = getIntent().getData();
		if (uri != null) {
			try {
				exploringVA = ""
						+ getIntent().getStringExtra("dataFromAIMVANavigator");
				Area exploreArea = getAreaByName("AEM Navigator");
				if (null != exploreArea && null != exploreArea.tasks
						&& exploreArea.tasks.size() != 0) {
					Task exploreTask = exploreArea.tasks.get(0);
					if (exploreTask != null && exploreTask.ats != null
							&& exploreTask.ats.size() != 0) {
						AT exploreAT = exploreTask.ats.get(0);
						exploreAT.setATName(exploringVA);
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Set Listener for the Next Button
	 */
	private void setNextListener() {
		/**
		 * Modifies: Next button in the view Effects: Sets listener to the next
		 * button
		 * */

		currentIntent = PersistenceBean.getExistingIntent(
				PersistenceBean.getCurrentId(context), context);
		Button nextButton = (Button) findViewById(R.id.nextbutton);
		nextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Alert About trial 2
				validateSolutions();
				if (trial2) {
					boolean infoCheck = true;
					for (Area area : areaList) {
						for (Task task : area.getTasks()) {
							if (!task.solutions) {
								for (AT at : task.ats) {
									if (at.solutionWorking.equals("NO")){
										if (TextUtils.isEmpty(at.trial1Persons) ||
												TextUtils.isEmpty(at.trial1Action) ||
												TextUtils.isEmpty(at.trial1CompletionDate)) {
											infoCheck = false;
											Toast.makeText(RevisitFirstTrial.this, getResources().getString(R.string.field_uncomplete), Toast.LENGTH_LONG).show();
											break;
										}
									}
								}
							}
						}
					}
					if (infoCheck){
						/*AlertDialog.Builder info = new AlertDialog.Builder(activity);
						info.setMessage(getResources()
								.getString(R.string.trial1nav).toString());
						info.setCancelable(true);
						info.setPositiveButton("Ok",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
														int id) {
										PersistenceBean.persistAreaObject(areaList, "trial1"
												+ PersistenceBean.getCurrentId(context), context);
										// FWD to trial 2
										currentIntent.putExtra("trial2", trial2);
										currentIntent.putExtra("open", false);
										currentIntent.setClass(context,
												SecondTrial.class);
										PersistenceBean.persistAreaObject(
												trial2List, "trial2" + studentid,
												context);
										Log.d("ATGUIDE",
												"student id on next persist"
														+ studentid);
										PersistenceBean.persistInstructionalAreas(
												"trial2" + studentid,
												trial2TextList, context);
										startActivity(currentIntent);
									}
								});
						info.setNeutralButton("Cancel",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
														int id) {
										dialog.cancel();
									}
								});
						AlertDialog infoAlert = info.create();
						infoAlert.setCanceledOnTouchOutside(false);
						infoAlert.setCancelable(false);
						infoAlert.show();*/
						if (!isSample) {
							PersistenceBean.persistAreaObject(areaList, "trial1"
									+ PersistenceBean.getCurrentId(context), context);
							PersistenceBean.persistAreaObject(
									trial2List, "trial2" + studentid,
									context);
							PersistenceBean.persistInstructionalAreas(
									"trial2" + studentid,
									trial2TextList, context);
						}
						View view = View.inflate(RevisitFirstTrial.this, R.layout.dialog_link, null);
						TextView link = (TextView) view.findViewById(R.id.link);
						link.setMovementMethod(LinkMovementMethod.getInstance());
						AlertDialog.Builder info = new AlertDialog.Builder(activity);
						info.setView(view);
						info.setCancelable(true);
						info.setPositiveButton("Display Form",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
														int id) {
										checkPermission();
										if (permission) {
											//display form
											Toast.makeText(context, "Please Wait", Toast.LENGTH_SHORT).show();
											PDFLogic.activity = activity;
											// Intent pdfService= new
											// Intent(activity.getApplicationContext(),PDFLogic.class);
											currentIntent.setClass(context, PDFLogic.class);
											currentIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
											currentIntent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
											// Intent pdfService= new
											// Intent(getApplicationContext(),PDFLogic.class);
											android.widget.ProgressBar bar = new android.widget.ProgressBar(getApplicationContext());
											bar.setIndeterminate(true);
											bar.bringToFront();
											// startService(currentIntent);
											Thread t = new Thread(new PDFLogic());
											t.start();
											finish();
										}
									}
								});
						info.setNeutralButton("Cancel",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
														int id) {
										dialog.cancel();
									}
								});
						AlertDialog infoAlert = info.create();
						infoAlert.setCanceledOnTouchOutside(false);
						infoAlert.setCancelable(false);
						infoAlert.show();
						WindowManager m = getWindowManager();
						Display d = m.getDefaultDisplay();  //为获取屏幕宽、高
						android.view.WindowManager.LayoutParams p = infoAlert.getWindow().getAttributes();  //获取对话框当前的参数值
						p.height = (int) (d.getHeight() * 0.5);   //高度设置为屏幕的0.3
						p.width = (int) (d.getWidth() * 0.4);    //宽度设置为屏幕的0.5
						infoAlert.getWindow().setAttributes(p);     //设置生效
					}
					else {
						Toast.makeText(RevisitFirstTrial.this, getResources().getString(R.string.field_uncomplete), Toast.LENGTH_LONG).show();
					}
				} else {
					if (!isSample) {
						PersistenceBean.persistAreaObject(areaList, "trial1"
								+ PersistenceBean.getCurrentId(context), context);
					}
					AlertDialog.Builder info = new AlertDialog.Builder(activity);
					info.setMessage(getResources().getString(
							R.string.adequatesolution).toString());
					info.setCancelable(true);
					info.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									Toast.makeText(context, "Please Wait",
											Toast.LENGTH_SHORT).show();
									PDFLogic.activity = activity;
									currentIntent.setClass(context, PDFLogic.class);
									currentIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									currentIntent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
									android.widget.ProgressBar bar = new android.widget.ProgressBar(getApplicationContext());
									bar.setIndeterminate(true);
									bar.bringToFront();
									Thread t = new Thread(new PDFLogic());
									t.start();
									finish();
								}
							});
					info.setNeutralButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							});
					AlertDialog infoAlert = info.create();
					infoAlert.setCanceledOnTouchOutside(false);
					infoAlert.setCancelable(false);
					infoAlert.show();
					// setContentView(layoutResID);
				}
			}
		});

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
					//display form
					Toast.makeText(context, "Please Wait",
							Toast.LENGTH_SHORT).show();
					PDFLogic.activity = activity;
					currentIntent.setClass(context,
							PDFLogic.class);
					currentIntent
							.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					currentIntent
							.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
					android.widget.ProgressBar bar = new android.widget.ProgressBar(
							getApplicationContext());
					bar.setIndeterminate(true);
					bar.bringToFront();
					Thread t = new Thread(new PDFLogic());
					t.start();
					finish();
					
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

	/**
	 * Click the first item in the Adapter
	 */
	private void clickFirstItem() {

		ListView lv = (ListView) findViewById(R.id.instructionalAreasList);
		MergeAdapter m = (MergeAdapter) lv.getAdapter();
		m.getCount();
		View first = (View) m.getItem(0); // Linear Layout
		if (first != null && first instanceof LinearLayout) {
			LinearLayout taskLay = (LinearLayout) ((LinearLayout) first)
					.getChildAt(1);
			LinearLayout taskChild = (LinearLayout)taskLay.getChildAt(1);
			taskChild.getChildAt(0).callOnClick();
		}
	}

	/**
	 * Set Listener for Assistive technology. Select the listener
	 */
	private void setATListener() {
		ImageButton add = (ImageButton) findViewById(R.id.addat);
		add.setImageResource(R.drawable.plusstrategy);
		add.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AT_List atlist = new AT_List();
				atlist.setFirstTrail(activity);
				atlist.show(fragmentManager, "AT List");
			}
		});

	}
	
	@Override
	protected void onNewIntent(Intent intent) {

		// super.onNewIntent(intent);
		toast("On New Intent Called");

		// super.onNewIntent(intent);
		Toast.makeText(context, "New Intent Called", Toast.LENGTH_SHORT).show();
		EditText ATname = (EditText) findViewById(R.id.at);
		Uri uri = intent.getData();
		if (uri != null) {
			try {
				exploringVA = ""
						+ intent.getStringExtra("dataFromAIMVANavigator");
				log("Data from AIMVA Navigator:" + exploringVA);
				ATname.setText("" + exploringVA);
			} catch (Exception e) {
				Log.e("ATGUIDE", "Exception in checkUri caught at line 135: "
						+ e);

				log("Exception" + e);
			}
		}

	}

	@Override
	protected void onRestart() {
		super.onRestart();
		Toast.makeText(context, "Restart called", Toast.LENGTH_SHORT).show();
	}

	/**
	 * Place the elements: Area, Task and ATs from database
	 */
	@SuppressLint("InflateParams")
	private void placeAreaFromDB() {
		try {
			// Initializing data
			// Set params
			LayoutParams textViewParams = new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			// The list view on left side
			ListView instructional = (ListView) findViewById(R.id.instructionalAreasList);
			// The list of area
			// int atCount = -1;
			String iepAlt = currentIntent.getStringExtra("iepalt");
			if (null != iepAlt && iepAlt.equalsIgnoreCase("Yes")) {
				// Add Aim Navigator to area and create special view
				// Add AreaName as Exploring AT
				// Add OnClick Listener: OnClick: Add extra button to call
				// application
				// add check to the data for incoming URL
				// Auto Fill from the data
				Area nav;
				Area navOld = getAreaByName("AEM Navigator");
				if (null != navOld) {
					nav = navOld;
				} else {
					nav = new Area("AEM Navigator");
				}
				// nav.addTask();
				nav.parentId = id++;
				// nav.tasks.clear();
				AT exploreAT = null;
				Task t;
				if (null == nav.tasks || nav.tasks.size() == 0) {
					t = null;

				} else {
					t = nav.tasks.get(0);
				}

				if (null != t) {
					if (t.ats.size() == 0) {
						exploreAT = null;
					} else {
						exploreAT = t.ats.get(0);
					}
				}

				if (null == exploreAT) {
					exploreAT = new AT();
					exploreAT.ATName = exploringVA;
					exploreAT.participants = "";
					exploreAT.firstTrialDate = "";
					exploreAT.task = "AEM Navigator";
				}
				Task explorer = null;

				if (null == t) {
					explorer = new Task();
					explorer.solutions = false;
					explorer.taskid = id++;
					explorer.setAreaname(nav.getAreaName());
					explorer.taskname = "AEM Navigator";
				} else {
					explorer = t;
				}
				explorer.ats.clear();
				explorer.ats.add(exploreAT);
				nav.tasks.clear();
				nav.addTask(explorer);
				if (null == navOld)
					areaList.add(nav);
			}
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			lp.setMargins(0,5,0,0);
			
			LinearLayout.LayoutParams tvlp = new LinearLayout.LayoutParams(
					ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
			LinearLayout.LayoutParams imglp = new LinearLayout.LayoutParams(
					ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			for (int i=0; i<areaList.size(); i++) {
				if (areaList.get(i).getAreaName().equals("AEM Navigator"))
					continue;
				// For each area get a Row
				LinearLayout areaRow = (LinearLayout) inflater.inflate(
						R.layout.areataskrow, null);
				areaRow.setId(id += 2);
				// Add the area name
				TextView areaTextView = (TextView) areaRow
						.findViewById(R.id.areatextview);
				// area.setBackground(getResources().getDrawable(R.drawable.textviewback));
				areaTextView.setText(areaList.get(i).getAreaName());
				areaTextView
						.setTextAlignment(TextView.TEXT_ALIGNMENT_TEXT_START);
				areaTextView.setLayoutParams(textViewParams);
				areaTextView.setId(areaList.get(i).parentId);
				boolean addArea = false;
				// Set onClick listener to the area name
				// append current task for 1st trial
				for (int j =0; j<areaList.get(i).getTasks().size(); j++) {
					// If solutions aren't working
					if (!areaList.get(i).getTasks().get(j).solutions) {
						// atCount++;
						addArea = true;
						LinearLayout taskLayout = new LinearLayout(context);
						taskLayout.setOrientation(LinearLayout.VERTICAL);
						taskLayout.setLayoutParams(textViewParams);
						TextView tasktextView = new TextView(context);
						tasktextView.setText(areaList.get(i).getTasks().get(j).getTaskname());
						taskLayout.setTag(areaList.get(i).getAreaName());
						tasktextView.setLayoutParams(textViewParams);
						tasktextView.setId(areaList.get(i).getTasks().get(j).taskid);
						tasktextView.setTextColor(Color.BLACK);
						tasktextView.setTypeface(null, Typeface.BOLD);
						tasktextView.setPadding(30, 0, 0, 0);
						taskLayout.addView(tasktextView);
						
						if (areaList.get(i).getTasks().get(j).ats.size() > 0) {
							for (AT at : areaList.get(i).getTasks().get(j).ats) {
								if (null != at) {
									LinearLayout atLinear = new LinearLayout(this);
									atLinear.setLayoutParams(lp);
									atLinear.setOrientation(LinearLayout.HORIZONTAL);
									atLinear.setGravity(Gravity.CENTER);
									
									//star icon
									final ImageView star = new ImageView(context);
									star.setLayoutParams(imglp);
									star.setImageResource(R.drawable.star);
									star.setPadding(5,5,5,5);
									//at text
									TextView assistiveTech = new TextView(context);
									assistiveTech.setLayoutParams(tvlp);
									assistiveTech.setPadding(55, 0, 0, 0);
									assistiveTech.setText(at.getATName());
									assistiveTech.setTextColor(Color.BLACK);
									assistiveTech.setId(at.id);
									assistiveTech.setOnClickListener(getATListener());
									if (checkStar(at)) {
										star.setVisibility(View.VISIBLE);
									}
									else{
										star.setVisibility(View.INVISIBLE);
									}
									atLinear.addView(assistiveTech);
									atLinear.addView(star);
									taskLayout.addView(atLinear);
								}
							}
						} else {
							LinearLayout atLinear = new LinearLayout(this);
							atLinear.setLayoutParams(lp);
							atLinear.setOrientation(LinearLayout.HORIZONTAL);
							atLinear.setGravity(Gravity.CENTER);
							//star icon
							final ImageView star = new ImageView(context);
							star.setLayoutParams(imglp);
							star.setImageResource(R.drawable.star);
							star.setPadding(5,5,5,5);
							star.setVisibility(View.INVISIBLE);
							TextView assistiveTech = new TextView(context);
							assistiveTech.setLayoutParams(tvlp);
							assistiveTech.setPadding(55, 0, 0, 0);
							assistiveTech.setTextColor(Color.BLACK);
							assistiveTech.setId(id++);
							assistiveTech.setOnClickListener(getATListener());
							atLinear.addView(assistiveTech);
							atLinear.addView(star);
							taskLayout.addView(atLinear);
						}
						areaRow.addView(taskLayout);
					}
				}

				// Linear layout task
				// append Assistive technology to this task
				if (addArea) {
					merge.addView(areaRow);
				}
			}
			instructional.setAdapter(merge);
		} catch (Exception e) {
			Log.e("ATGUIDE",
					"Error retrieving data  FirstTrial->placeAreaFromDB() \n"
							+ e);
		}
	}
	
	private boolean checkStar(AT at){
		if (!TextUtils.isEmpty(at.getATName().trim()) &&
				!TextUtils.isEmpty(at.trial1Action.trim()) &&
				!TextUtils.isEmpty(at.trial1CompletionDate.trim()) &&
				!TextUtils.isEmpty(at.trial1Persons.trim())){
			return true;
		}
		return false;
	}
	
	
	/**
	 * Get Data from intent and the persistence layer.
	 */
	private void getData() {

		try {
			selectedInstructional = PersistenceBean.getPersistedAreaList(
					"trial1" + PersistenceBean.getCurrentId(context), context);
			open = getIntent().getBooleanExtra("open", false);
			currentIntent = PersistenceBean.getExistingIntent(
					PersistenceBean.getCurrentId(context), context);
			isSample = currentIntent.getBooleanExtra("sample", false);
			for (CharSequence cs : selectedInstructional) {
				trial1Texts.add(cs.toString());
			}
		} catch (Exception e) {
			Log.e("ATGUIDE", "Error retrieving data  firsttrial->getData() "
					+ e);
		}
	}

	/**
	 * Allows a pop-up calendar on click of EditTextView of the Date
	 */
	public void setDatePickListener() {

		final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear,

			int dayOfMonth) {

				myCalendar.set(Calendar.YEAR, year);
				myCalendar.set(Calendar.MONTH, monthOfYear);
				myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
				updateLabel();
			}

		};

		datePick.setOnClickListener(new View.OnClickListener() {

			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View v) {

				DatePickerDialog dialog = new DatePickerDialog(
						RevisitFirstTrial.this, date, myCalendar
								.get(Calendar.YEAR), myCalendar
								.get(Calendar.MONTH), myCalendar
								.get(Calendar.DAY_OF_MONTH));
				dialog.getDatePicker().setSpinnersShown(true);
				dialog.getDatePicker().setCalendarViewShown(false);
				dialog.show();
			}
		});
	}

	/**
	 * Update the date label to the calendar object
	 */
	private void updateLabel() {
		String myFormat = "MM/dd/yy";
		SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
		datePick.setText(sdf.format(myCalendar.getTime()));
	}
	
	//highlight the current task item
	private void highLightLayout(View v){
		//eliminate other items bg color
		ListView lv = (ListView) findViewById(R.id.instructionalAreasList);
		MergeAdapter m = (MergeAdapter) lv.getAdapter();
		int childrenNum = m.getCount();
		for (int i=0; i<childrenNum; i++){
			View areaView = (View)m.getItem(i);
			if (areaView!=null && areaView instanceof LinearLayout){
				LinearLayout areaLayout = (LinearLayout)areaView;
				for (int j=1; j<areaLayout.getChildCount(); j++){
					//task name
					LinearLayout taskLayout = (LinearLayout) areaLayout.getChildAt(j);
					taskLayout.getChildAt(0).setBackgroundResource(0);
					for (int k=1; k<taskLayout.getChildCount(); k++){
						//at name + star
						LinearLayout atLayout = (LinearLayout) taskLayout.getChildAt(k);
						atLayout.setBackgroundResource(0);
					}
				}
			}
		}
		LinearLayout atLinear = (LinearLayout)v.getParent();
		atLinear.setBackgroundColor(getResources().getColor(R.color.highlight_blue));
		((LinearLayout)atLinear.getParent()).getChildAt(0).setBackgroundColor(getResources().getColor(R.color.highlight_blue));
	}
	
	/**
	 * 
	 * @return OnClickListener for the AT
	 */
	public OnClickListener getATListener() {

		OnClickListener atL = new OnClickListener() {
			@Override
			public void onClick(View v) {
				highLightLayout(v);
				// Remove Previous Listeners if set

				EditText atname = (EditText) findViewById(R.id.atTriedEdit);
				atname.removeTextChangedListener(_ATNameWatcher);

				EditText ataction = (EditText) findViewById(R.id.at);
				ataction.removeTextChangedListener(_ActionToWatcher);
				// atname.setText("Choose AT");//Default
				EditText participantView = (EditText) findViewById(R.id.participants);
				participantView.removeTextChangedListener(participantsWatcher);
				// participantView.setText("");//
				EditText dateView = (EditText) findViewById(R.id.date);
				dateView.removeTextChangedListener(trialDateWatcher);

				// AT View
				TextView aTView = (TextView) v;
				CharSequence atName = trimATName((String) aTView.getText());
				clickedId = v.getId();
				// Parent
				LinearLayout parent = (LinearLayout) v.getParent().getParent().getParent();
				TextView area = (TextView) parent.getChildAt(0);
				CharSequence areaText = area.getText();
				TextView taskView = (TextView) ((LinearLayout) v.getParent().getParent())
						.getChildAt(0);
				Area areaobj = getAreaByName(areaText);
				Task t = areaobj.getTaskById(taskView.getId());
				if (t.ats.size() == 0) {
					AT at = new AT();
					at.task = t.taskname;
					at.ATName = atName.toString();
					at.instructionalArea = area.getText().toString();
					at.id = clickedId;
					setAtToView(at, v);
					t.ats.add(at);
				} else {
					setAtToView(t.getATById(clickedId), v);
				}
				setATListener();
			}
		};
		return atL;
	}

	/**
	 * Sets the given AT to the view.
	 * also c
	 * 
	 * @param at
	 *            assitive technology object to be set to view
	 * @param currentClicked
	 *            view selected on left panel
	 */
	public void setAtToView(final AT at, View currentClicked) {
		/*
		 * Requires : the AT Listener to the change in participants clicked
		 * view. Modifies: Current view and this. Effects: Sets the given AT to
		 * the view.
		 */
		final Task parentTask = getAreaByName(at.instructionalArea).getTaskByName(at.task);
		setATNameListener(at, currentClicked);
		setPartcipantListener(at, currentClicked);
		setCompletionDateListener(at, currentClicked);
		setATActionListener(at, currentClicked);
		TextView instructionalTitle = ((TextView) findViewById(R.id.areatitle));
		instructionalTitle.setText(at.getInstructionalArea());
		instructionalTitle.setTextColor(Color.BLACK);
		instructionalTitle.setTypeface(null, Typeface.BOLD);
		((TextView) findViewById(R.id.taskname)).setText(at.task);
		((EditText) findViewById(R.id.atTriedEdit)).setText(at.getATName());

		((EditText) findViewById(R.id.at)).setText(at.trial1Action);
		((EditText) findViewById(R.id.participants)).setText(at.trial1Persons);
		((EditText) findViewById(R.id.date)).setText(at.getTrial1CompletionDate());
		RadioGroup solutions = (RadioGroup) findViewById(R.id.solutionradiogroup);
		if (at.getSolutionWorking().equals("NA")){
			at.setSolutionWorking("YES");
		}
		solutions
				.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						RadioButton checkedSolution = (RadioButton) group
								.findViewById(checkedId);
						if (checkedId == R.id.solutionyes) {
							Log.i("solution ", "yes");
							// t.solutions = true;
							View action = (View) findViewById(R.id.trialActionLayout);
							action.setVisibility(View.INVISIBLE);
							View personLayout = (View) findViewById(R.id.actionPersonLayout);
							personLayout.setVisibility(View.INVISIBLE);
							at.setSolutionWorking("YES");
							parentTask.setTrial1solutions(true);
							View dateLayout = (View) findViewById(R.id.actionCompletionLayout);
							dateLayout.setVisibility(View.INVISIBLE);

						} else {
							Log.i("solution ", "no");
							// t.solutions = false;
							// make next fields visible
							View action = (View) findViewById(R.id.trialActionLayout);
							action.setVisibility(View.VISIBLE);
							setChildrenVisibility((LinearLayout) action,
									View.VISIBLE);
							View personLayout = (View) findViewById(R.id.actionPersonLayout);
							personLayout.setVisibility(View.VISIBLE);
							setChildrenVisibility((LinearLayout) personLayout,
									View.VISIBLE);
							at.setSolutionWorking("NO");
							parentTask.setTrial1solutions(false);

							View dateLayout = (View) findViewById(R.id.actionCompletionLayout);
							dateLayout.setVisibility(View.VISIBLE);
							setChildrenVisibility((LinearLayout) dateLayout,
									View.VISIBLE);
						}

					}
				});

		if (at.getSolutionWorking().equals("YES")) {
			((RadioButton) solutions.findViewById(R.id.solutionyes))
					.setChecked(true);
			View action = (View) findViewById(R.id.trialActionLayout);
			action.setVisibility(View.INVISIBLE);
			View personLayout = (View) findViewById(R.id.actionPersonLayout);
			personLayout.setVisibility(View.INVISIBLE);

			View dateLayout = (View) findViewById(R.id.actionCompletionLayout);
			dateLayout.setVisibility(View.INVISIBLE);
		} else {
			((RadioButton) solutions.findViewById(R.id.solutionno))
					.setChecked(true);

		}
//		areaList.get(position[0]).getTasks().get(position[1]).getAts().set(position[2], at);
	}

	/**
	 * Recursive Definition to set Visibility to all children of Linear Layout
	 * 
	 * @param parent
	 * @param visibility
	 */
	public void setChildrenVisibility(LinearLayout parent, int visibility) {
		int totalChildren = parent.getChildCount();
		for (int childNum = 0; childNum < totalChildren; childNum++) {
			View currentView = parent.getChildAt(childNum);
			currentView.setVisibility(visibility);
			if (currentView instanceof LinearLayout) {
				setChildrenVisibility((LinearLayout) currentView, visibility);
			}
			currentView.setVisibility(visibility);

		}

	}

	/**
	 * Validate solutions for second trial
	 */
	private void validateSolutions() {
		// Abstract function:
		// Get all area
		// Get each task
		// Add the solution not working to separate list
		// save the list
		// For all selected / persisted Area
		trial2 = false;
		trial2List.clear();
		trial2TextList.clear();
		for (Area checkArea : areaList) {
			Area areaTrial2 = new Area();
			areaTrial2.copyArea(checkArea);
			// For each task in this area
			for (Task task : checkArea.tasks) {
				// If any task does have second trial, add this area for second trial
				task.trial1solutions=true;
				for(int i=0; i< task.ats.size(); i++){
					//as long as there is one solution working, the task is fine
					if(task.getAts().get(i).getSolutionWorking().equals("YES")){
						break;
					}
					else {
						//not find any solution until end
						if (i >= task.ats.size()-1){
							Task taskTrial2 = new Task();
							taskTrial2.copyTask(task);
							taskTrial2.trial1solutions = false;
							taskTrial2.solutions = false;
							areaTrial2.addTask(taskTrial2);
							trial2 = true;
							break;
						}
					}
				}
			}
			if(areaTrial2.getTasks()!=null && areaTrial2.getTasks().size() >=0){
				trial2List.add(areaTrial2);
				trial2TextList.add("" + areaTrial2.getAreaName());
			}
		}
	}

	

	/**
	 * Listener to the change in AT Name
	 *
	 *            Assistive Technology type instance
	 */
	public void setATNameListener(final AT at, final View currentClicked) {
		EditText atname = (EditText) findViewById(R.id.atTriedEdit);
		atname.removeTextChangedListener(_ATNameWatcher);

		_ATNameWatcher = new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				at.setATName(s.toString());
				((TextView) currentClicked).setText(trimATName(s.toString()));
				LinearLayout parent = (LinearLayout) currentClicked.getParent();
				if (checkStar(at)){
					parent.getChildAt(1).setVisibility(View.VISIBLE);
				}else {
					parent.getChildAt(1).setVisibility(View.INVISIBLE);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		};

		atname.addTextChangedListener(_ATNameWatcher);
	}

	/**
	 * Listener to the action to be taken after this trial
	 *
	 *            the current AT
	 *
	 */
	public void setATActionListener(final AT at, final View currentClicked) {
		EditText atname = (EditText) findViewById(R.id.at);
		atname.removeTextChangedListener(_ActionToWatcher);
		// atname.setEnabled(false);
		// atname.setFocusable(false);

		_ActionToWatcher = new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				at.trial1Action = (trimATName(s.toString()));
				LinearLayout parent = (LinearLayout) currentClicked.getParent();
				if (checkStar(at)){
					parent.getChildAt(1).setVisibility(View.VISIBLE);
				}else {
					parent.getChildAt(1).setVisibility(View.INVISIBLE);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		};

		atname.addTextChangedListener(_ActionToWatcher);
	}

	/**
	 * Listener to the change in participants
	 * 
	 * @param at
	 *            Assistive Technology type instance
	 * 
	 */
	public void setPartcipantListener(final AT at, final View currentClicked) {
		/*
		 * Requires: AT to be binded to the participants. Modifies: this,
		 * current view and AT. Effects: Listener to the change in participants
		 */
		EditText participantView = (EditText) findViewById(R.id.participants);
		participantView.removeTextChangedListener(participantsWatcher);
		participantsWatcher = new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// Text Changed , bind to AT
				at.trial1Persons = (s.toString());
				LinearLayout parent = (LinearLayout) currentClicked.getParent();
				if (checkStar(at)){
					parent.getChildAt(1).setVisibility(View.VISIBLE);
				}else {
					parent.getChildAt(1).setVisibility(View.INVISIBLE);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}

		};
		participantView.addTextChangedListener(participantsWatcher);

	}

	/**
	 * Listener to the change in completion date
	 * 
	 * @param at
	 *            Assistive Technology type instance
	 */
	public void setCompletionDateListener(final AT at, final View currentClicked) {
		/* Requires: AT to be binded to the participants. */
		/* Modifies: this, current view and AT */
		/* Effects: Listener to the change in dateview */
		EditText dateView = (EditText) findViewById(R.id.date);
		dateView.removeTextChangedListener(trialDateWatcher);
		trialDateWatcher = new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// Text Changed , bind to AT
				at.trial1CompletionDate = (s.toString());
				LinearLayout parent = (LinearLayout) currentClicked.getParent();
				if (checkStar(at)){
					parent.getChildAt(1).setVisibility(View.VISIBLE);
				}else {
					parent.getChildAt(1).setVisibility(View.INVISIBLE);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		};
		dateView.addTextChangedListener(trialDateWatcher);
	}
	
	/**
	 * To get area object by name from the list
	 * 
	 * @param areaname
	 * @return
	 */
	public Area getAreaByName(CharSequence areaname) {
		if (areaname != null)
			for (Area area : areaList) {
				if (area != null
						&& area.getAreaName().trim()
								.equalsIgnoreCase(areaname.toString().trim())) {
					return area;
				}
			}
		return null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.first_trial, menu);
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	public String trimATName(String atName) {
		if (atName.contains("(e.g.") || atName.contains("( e.g.")) {
			return atName.replaceFirst("[(][e][.][g][.](.+?)[)]", " ");
		}
		return atName;
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

	public void log(String string) {
		Log.d("ATGUIDE", string);
	}

	public void toast(String string) {
		Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT)
				.show();
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		if (!isSample) {
			//save trial1 information
			PersistenceBean.persistAreaObject(areaList, "trial1"
					+ PersistenceBean.getCurrentId(context), context);
		}
	}

	public String solution(String S, int K) {
		String temp = S.replace("-","");
		int N = temp.length();
		int charsInFirstGroup = N%K;
		int noOfGroups = charsInFirstGroup==0?N/K:(N/K)+1;

		String groups[] = new String[noOfGroups];

		for (int i = 0; i<noOfGroups;i++){
			if (i == 0 && charsInFirstGroup > 0)
				groups[i] = temp.substring(0,charsInFirstGroup);
			else
				groups[i] = temp.substring(charsInFirstGroup+(4*(i-1)),charsInFirstGroup+(4*i));
		}

		String output = "";
		for (String group : groups)
			output = output + "-" + group;
		return output.toUpperCase();
	}
}
