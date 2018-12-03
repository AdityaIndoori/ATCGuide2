package edu.gmu.ttaconline.atcguide.ui;

import java.util.ArrayList;
import java.util.Arrays;

import com.commonsware.cwac.merge.MergeAdapter;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

import edu.gmu.ttaconline.atcguide.CheckBoxBean;
import edu.gmu.ttaconline.atcguide.IEPReading;
import edu.gmu.ttaconline.atcguide.MyApplication;
import edu.gmu.ttaconline.atcguide.PersistenceBean;
import edu.gmu.ttaconline.atcguide.R;
import edu.gmu.ttaconline.atcguide.SecondTrial;

/**
 * For the selection of Instructional areas
 * 
 * @author ajain13
 * 
 */

public class InstructionalAreas extends Activity {
	/** current running intent */
	Intent currentIntent;
	/** This context */
	Context context;
	/** Selected instructional areas */
	ArrayList<String> selectedInstructionalAreas = new ArrayList<String>();
	/** @deprecated logic for checkBox (Deprecated: use intent data instead) */
	CheckBoxBean instructionalCheck;
	String otherText = "";
	boolean isSample = false;
	boolean otherSelected = false;
	MergeAdapter merge = new MergeAdapter();
	String firstList[];
	String secondList[];
	
	ImageButton saveBtn;
    ImageButton helpBtn;
    ImageButton homeBtn;

   
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = getApplicationContext();
		setContentView(R.layout.activity_instructional_areas);
        setCustomActionBar();
		setCurrentIntent(); // Retrieve current intent
		instructionalCheck = new CheckBoxBean();
		setInstuctionalAreas(); // set the view of instructional areas
		checkAreaFromDB();// check areas from db
		setNextListener(); // Persist selection and go to next activity
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
                    Toast.makeText(context, "Information has been saved.", Toast.LENGTH_LONG).show();
                    break;
                case R.id.helpbutton:
					Intent intent = new Intent(InstructionalAreas.this, HelpPage.class);
					startActivity(intent);
                    break;
                case R.id.home:
					AlertDialog.Builder info = new AlertDialog.Builder(InstructionalAreas.this);
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
	
	/**
	 * Click listener to the Next button
	 */
	private void setNextListener() {
		Button next = (Button) findViewById(R.id.nextbutton);
		next.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				saveInfo();
				if (otherSelected && TextUtils.isEmpty(otherText.trim())){
					Toast.makeText(InstructionalAreas.this, getResources().getString(R.string.specify_other), Toast.LENGTH_LONG).show();
				}
				else {
					if (selectedInstructionalAreas.size() <= 0){
						Toast.makeText(InstructionalAreas.this, getResources().getString(R.string.field_instru_area), Toast.LENGTH_LONG).show();
					}
					else {
						if (getIntent().getBooleanExtra("open", false)){
							currentIntent.putExtra("open", true);
						}
						startActivity(currentIntent);
					}
				}

			}
		});

		// ArrayList<Integer>
		// checkedIDList=instructionalCheck.getAllCheckedIds();

	}
	
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		saveInfo();
	}
	
	/**
     * save information before next page
     */
	private void saveInfo(){
		if (!isSample) {
			if (otherSelected && !TextUtils.isEmpty(otherText.trim())) {
				selectedInstructionalAreas.add("" + otherText.trim());
			}
			PersistenceBean.persistInstructionalAreas(currentIntent.getStringExtra("studentid"),selectedInstructionalAreas, context);
			currentIntent.putStringArrayListExtra("selectedareas",
					selectedInstructionalAreas);
			currentIntent.putExtra("otherSelected", otherSelected);
			currentIntent.putExtra("othertext", otherText);
			PersistenceBean.persistCurrentId(currentIntent.getStringExtra("studentid"), context);
			PersistenceBean.persistIntent(currentIntent.getStringExtra("studentid"),currentIntent, context);
		}
		currentIntent.setClass(context, IEPReading.class);
    }
	
	/**
	 * 
	 * @param instructionalAreas
	 *            List of areas to be checked
	 */
	public void CheckValuesFromPersistence(ArrayList<String> instructionalAreas) {

	}

	/**
	 * Set the instructional areas.
	 * 
	 */
	private void setInstuctionalAreas() {

		LinearLayout list1 = null;
		LinearLayout list2 = null;

		try {
			list1 = (LinearLayout) this.findViewById(R.id.list1);
			list2 = (LinearLayout) this.findViewById(R.id.list2);
			firstList = getResources().getStringArray(R.array.list1);
			secondList = getResources().getStringArray(R.array.list2);
			addListToView(firstList, list1);
			addListToView(secondList, list2);
			addOtherArea();
		} catch (Exception e) {
			Log.e("ATGUIDE", "Other Exception: " + e.getMessage());
		}
	}

	/**
	 * Checkbox Area from the database.
	 */
	private void checkAreaFromDB() {
		try {
			ArrayList<CharSequence> persistedList = PersistenceBean
					.getPersistedAreaList(
							PersistenceBean.getCurrentId(context), context);
			if (persistedList != null)
				for (CharSequence areaName : persistedList) {
					LinearLayout list1 = (LinearLayout) findViewById(R.id.list1);
					for (int i = 0; i < list1.getChildCount(); i++) {
						if (list1.getChildAt(i) instanceof LinearLayout) {
							LinearLayout checkLayout = (LinearLayout) list1
									.getChildAt(i);
							CheckBox cb = (CheckBox) checkLayout.getChildAt(0);
							String checkText = (String) cb.getText();
							if (checkText.contains(areaName)) {
								cb.setChecked(true);
							}
							if (isSample){
								cb.setClickable(false);
							}
						}
					}
					
					//if two lists don't contain area, it must be other field.
					if (!Arrays.asList(firstList).contains(areaName) && !Arrays.asList(secondList).contains(areaName)){
						LinearLayout other = (LinearLayout) this.findViewById(R.id.otherlayout);
						CheckBox otherBox = (CheckBox) other.getChildAt(0);
						otherBox.setChecked(true);
						EditText specify = (EditText) other.getChildAt(1);
						specify.setText(areaName);
						if (isSample){
							otherBox.setClickable(false);
							specify.setFocusable(false);
						}
					}
					else {
						LinearLayout other = (LinearLayout) this.findViewById(R.id.otherlayout);
						CheckBox otherBox = (CheckBox) other.getChildAt(0);
						EditText specify = (EditText) other.getChildAt(1);
						if (isSample){
							otherBox.setClickable(false);
							specify.setFocusable(false);
						}
					}
					
					
					LinearLayout list2 = (LinearLayout) findViewById(R.id.list2);
					for (int i = 0; i < list2.getChildCount(); i++) {
						if (list1.getChildAt(i) instanceof LinearLayout) {
							LinearLayout checkLayout = (LinearLayout) list2
									.getChildAt(i);
							CheckBox cb = (CheckBox) checkLayout.getChildAt(0);
							String checkText = (String) cb.getText();
							if (checkText.contains(areaName)) {
								cb.setChecked(true);
							}
							if (isSample){
								cb.setClickable(false);
							}
						}
					}
				}
		} catch (Exception e) {
			Log.e("ATGUIDE", "EX: " + e);
		}
	}

	/**
	 * Add the instructional area 'Other' and set the listeners
	 */
	private void addOtherArea() {
		try {
			LinearLayout.LayoutParams layoutparams = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			// layoutparams.weight=50;
			final LinearLayout other = (LinearLayout) this
					.findViewById(R.id.otherlayout);
			other.setLayoutDirection(LinearLayout.LAYOUT_DIRECTION_LTR);
			final CheckBox otherCheck = new CheckBox(context);
			otherCheck.setLayoutParams(layoutparams);
			otherCheck.setText("Other");
			otherCheck.setTextColor(Color.DKGRAY);
			otherCheck.setButtonDrawable(getResources().getDrawable(
					R.drawable.custom_checkbox));
			final EditText specify = new EditText(context);
			specify.setLayoutParams(layoutparams);
			specify.setEnabled(false);
			specify.setId(102456);
			specify.setFocusable(false);
			specify.clearFocus();
			specify.setBackground(getResources().getDrawable(
					R.drawable.textviewback));
			specify.setTextColor(getResources().getColor(R.color.text_black));
			specify.setMaxLines(1);
			other.addView(otherCheck);
			specify.setHint("Please specify");
			specify.setHintTextColor(getResources().getColor(R.color.text_hint_title));
			other.addView(specify);
			otherCheck
					.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

						@Override
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							View v = (View) buttonView.getParent();
							final EditText other = (EditText) v.findViewById(102456);
							otherSelected = isChecked;
							// other.setBackgroundColor(0);
							if (isChecked) {
								other.setEnabled(true);
								other.requestFocus();

								InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
								imm.showSoftInput(other,
										InputMethodManager.SHOW_FORCED);

								other.setFocusable(true);
								other.setFocusableInTouchMode(true);
								other.setCursorVisible(true);

								other.addTextChangedListener(new TextWatcher() {
									@Override
									public void onTextChanged(CharSequence s,
											int start, int before, int count) {
										// TODO Auto-generated method stub
									}

									@Override
									public void beforeTextChanged(
											CharSequence s, int start,
											int count, int after) {
										// TODO Auto-generated method stub
									}

									@Override
									public void afterTextChanged(Editable s) {
										// TODO Auto-generated method stub
										otherText = new String(s + "");
									}
								});
							} else {
								AlertDialog.Builder info = new AlertDialog.Builder(InstructionalAreas.this);
								info.setTitle("Deselecting area alert");
								info.setMessage(getResources().getString(
										R.string.deselect_area_alert).toString());
								info.setCancelable(true);
								info.setPositiveButton("OK",
										new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog,
																int id) {
												other.setEnabled(false);
												other.setFocusable(false);
												other.getText().clear();
//												PersistenceBean.deleteAreaRelatedRecords(currentIntent.getStringExtra("studentid"), specify.getText().toString(), context);
												String otherName = PersistenceBean.deleteOtherAreaRecord(currentIntent.getStringExtra("studentid"), context, firstList, secondList);
												if (selectedInstructionalAreas.contains(otherName)){
													selectedInstructionalAreas.remove(otherName);
												}
											}
										});
								info.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                        otherCheck.setChecked(true);
                                        otherSelected = true;
                                    }
                                });
								AlertDialog infoAlert = info.create();
								infoAlert.setCanceledOnTouchOutside(false);
								infoAlert.setCancelable(false);
								infoAlert.show();
							}
						}
					});
			other.clearFocus();
		} catch (Exception e) {
			Log.e("ATGUIDE", e.getMessage());
		}
	}

	/**
	 * Adds the given list of instructional areas to the view
	 * 
	 * @param listArray
	 * @param listLayout
	 */
	private void addListToView(String listArray[], LinearLayout listLayout) {

		LinearLayout.LayoutParams layoutparams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		for (int i = 0; i < listArray.length; i++) {

			LinearLayout currentListItem = new LinearLayout(context);
			currentListItem.setLayoutParams(layoutparams);
			final CheckBox currentCheck = new CheckBox(context);
			currentCheck.setLayoutParams(layoutparams);
			currentCheck.setText(listArray[i]);

			currentCheck.setTextColor(Color.BLACK);
			currentCheck.setId(instructionalCheck.getNextCheckID());

			currentCheck
					.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

						@Override
						public void onCheckedChanged(final CompoundButton buttonView,

						boolean isChecked) {
							if (isChecked) {
								selectedInstructionalAreas
										.add((String) buttonView.getText());
								// instructionalCheck.addChecked(buttonView.getId());
							} else {
								// instructionalCheck.removeChecked(buttonView.getId());
								// when deselecting, need to delete related content in the database
								AlertDialog.Builder info = new AlertDialog.Builder(InstructionalAreas.this);
								info.setTitle("Deselecting area alert");
								info.setMessage(getResources().getString(
										R.string.deselect_area_alert).toString());
								info.setCancelable(true);
								info.setPositiveButton("OK",
										new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog,
																int id) {
												selectedInstructionalAreas
														.remove((String) buttonView.getText());
												PersistenceBean.deleteAreaRelatedRecords(currentIntent.getStringExtra("studentid"), currentCheck.getText().toString(),context);
											}
										});
                                info.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                        currentCheck.setChecked(true);
                                    }
                                });
								AlertDialog infoAlert = info.create();
								infoAlert.setCanceledOnTouchOutside(false);
								infoAlert.setCancelable(false);
								infoAlert.show();
							}
						}
					});
			currentCheck.setButtonDrawable(getResources().getDrawable(
					R.drawable.custom_checkbox));
			currentListItem.addView(currentCheck);
			Space space = new Space(context);
			space.setLayoutParams(new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT, 10));
			try {
				listLayout.addView(currentListItem);
				listLayout.addView(space);
			} catch (Exception e) {
				Log.e("ATGUIDE", "Other Exception: " + e.getMessage());
			}
		}
	}

	/**
	 * Set the value of current intent from current running student id
	 */
	private void setCurrentIntent() {
		currentIntent = PersistenceBean.getExistingIntent(
				PersistenceBean.getCurrentId(context), context);
		isSample = currentIntent.getBooleanExtra("sample", false);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.instructional_areas, menu);
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
