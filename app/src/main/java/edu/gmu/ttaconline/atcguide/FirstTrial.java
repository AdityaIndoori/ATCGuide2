package edu.gmu.ttaconline.atcguide;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import com.commonsware.cwac.merge.MergeAdapter;
import com.itextpdf.text.pdf.parser.Line;

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
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.MutableLong;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;

import org.w3c.dom.Text;

import edu.gmu.ttaconline.atcguide.ui.HelpPage;
import edu.gmu.ttaconline.atcguide.ui.InstructionalAreas;
import edu.gmu.ttaconline.atcguide.ui.MainActivity;

/**
 * Activity for first trial of the AT Inputs the additional data for the first
 * trial
 *
 * @author animesh jain
 */
public class FirstTrial extends FragmentActivity {
    /*
     * Overview: Type inheriting from activity for the first trial of the AT
     * trials
     */
    /* Instance variables */
    ArrayList<Area> areaList;// Store the lists of all areas
    ArrayList<String> trial1Texts = new ArrayList<String>();// store text only
    Calendar myCalendar = Calendar.getInstance();
    EditText datePick;
    MergeAdapter merge = new MergeAdapter();
    Context context;// store current context
    LayoutInflater inflater;// store the layout inflator to inflate rows
    int id = 1001;// default id
    static int clickedId = 9999;// current clicked id
    private ArrayList<CharSequence> selectedInstructional; // list of selected
    TextWatcher _ATNameWatcher, participantsWatcher, trialDateWatcher;
    android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
    Activity activity;
    Intent currentIntent;
    OnClickListener addATCLick;
    String aemNavigator = "AEM Navigator";
    String vaExploring = "VA Explorer";
    boolean open = false;
    final String URI_FIRST_TRIAL = "http://trial1.atguide.com/";


    ImageButton saveBtn;
    ImageButton helpBtn;
    ImageButton homeBtn;
    TextView trialTitle;
    //check if all fields are filled
    boolean infoCheck = true;

    boolean isSample = false;

    /**
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_trial);
        setCustomActionBar();
        context = getApplicationContext();

        areaList = PersistenceBean.getPersistedAreaObjects("trial1" + PersistenceBean.getCurrentId(context), context);
//        areaList = PersistenceBean.getPersistedAreaObjects(PersistenceBean.getCurrentId(context), context);
        inflater = getLayoutInflater();
        // Get existing useful data from intent and SQLite

        try {
            try {
                getData();
                checkUri();
            } catch (Exception e) {
                Log.e("AT GUIDE", "Exception in FirstTrial.onCreate 97: *" + e);
            }
            // toast("data string"+ getIntent().getDataString());
            // if not open
            if (!open) {
                try {
                    Log.i("first", "not open");
                    placeArea();
                } catch (Exception e) {
                    Log.e("AT GUIDE", "Exception in FirstTrial.onCreate 108: *"
                            + e);
                }

            } else {
                try {
                    Log.i("first", "open");
                    placeAreaFromDB();
                } catch (Exception e) {
                    Log.e("AT GUIDE", "Exception in FirstTrial.onCreate 116: *"
                            + e);
                }
            }
            activity = this;
            datePick = (EditText) findViewById(R.id.date);
            // setATListener();
            setDatePickListener();

            try {
                clickFirstItem();
            } catch (Exception e) {
                Log.e("AT GUIDE",
                        "Exception in FirstTrial.onCreate.clickFirstItem 129: *"
                                + e);
            }

            setNextListener();
        } catch (Exception e) {
            Log.e("AT GUIDE", "Exception in FirstTrial.onCreate 127: *" + e);
        }
    }

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.save_record:
                    if (!isSample) {
                        saveInfo();
                        Toast.makeText(context, "Information has been saved.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(context, "Sample cannot be modified.", Toast.LENGTH_LONG).show();
                    }
                    break;
                case R.id.helpbutton:
                    Intent intent = new Intent(FirstTrial.this, HelpPage.class);
                    startActivity(intent);
                    break;
                case R.id.home:
                    AlertDialog.Builder info = new AlertDialog.Builder(FirstTrial.this);
                    info.setTitle("Alert");
                    info.setMessage(getResources().getString(
                            R.string.save_notice).toString());
                    info.setCancelable(true);
                    info.setPositiveButton("YES",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    if (!isSample) {
                                        saveInfo();
                                    } else {
                                        Toast.makeText(context, "Sample cannot be modified.", Toast.LENGTH_LONG).show();
                                    }
                                    ((MyApplication) getApplication()).goHome();
                                }
                            });
                    info.setNeutralButton("NO",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    ((MyApplication) getApplication()).goHome();
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

    private void saveInfo() {
        PDFLogic.activity = activity;
        currentIntent.setClass(context, Form_Summary.class);
        currentIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        currentIntent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        currentIntent.putExtra("trial1", true);
        currentIntent.putExtra("activity_name", activity.getLocalClassName());
        if (!isSample) {
            PersistenceBean.persistAreaObject(areaList, PersistenceBean.getCurrentId(context), context);
            PersistenceBean.persistAreaObject(areaList, "trial1" + PersistenceBean.getCurrentId(context), context);
            PersistenceBean.persistIntent(PersistenceBean.getCurrentId(context), currentIntent, context);
            PersistenceBean.persistTrial(currentIntent.getStringExtra("studentid"), context, 1);
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
        trialTitle = (TextView) findViewById(R.id.trial_title);
        trialTitle.setText(getResources().getString(R.string.trial));
    }

    /**
     * Check if the current intent has URI from AIMNavigator
     */
    private void checkUri() {
        /**Requires: Current Intent to be available by getIntent()**/
        /**Modifies: Area Object "AEM Navigator"**/
        /**Effects: Sets the value form AIM VA Navigator into the Area Object of "AEM Navigator"**/

        Uri uri = getIntent().getData();
        if (uri != null) {
            try {
                vaExploring = "" + getIntent().getStringExtra("dataFromAIMVANavigator");
                //log("Data from AIMVA Navigator:" + exploringVA);
                Area exploreArea = getAreaByName("AEM Navigator");
                if (null != exploreArea && null != exploreArea.tasks
                        && exploreArea.tasks.size() != 0) {
                    Task exploreTask = exploreArea.tasks.get(0);
                    if (exploreTask != null && exploreTask.ats != null
                            && exploreTask.ats.size() != 0) {
                        AT exploreAT = exploreTask.ats.get(0);
                        exploreAT.setATName(vaExploring);
                    }
                }

            } catch (Exception e) {
                Log.e("ATGUIDE", "Exception in FirstTrial.checkUri(): "
                        + e);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        saveInfo();
    }

    /**
     * Set Listener for the Next Button
     */
    private void setNextListener() {
        /**
         * Modifies: Next button in the view Effects: Sets listener to the next
         * button
         * */
        currentIntent = PersistenceBean.getExistingIntent(PersistenceBean.getCurrentId(context), context);
        Button nextButton = (Button) findViewById(R.id.nextbutton);
        nextButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                infoCheck = true;
                for (Area area : areaList) {

                    for (Task task : area.getTasks()) {
                        if (!task.solutions) {//i deleted record already, should not be needed && !task.areaname.equals("AEM Navigator")
                            for (AT at : task.ats) {
                                Log.i("AT", "has AT");
                                if (TextUtils.isEmpty(at.getATName()) ||
                                        at.getATName().equals("Choose AT") ||
                                        TextUtils.isEmpty(at.getParticipants()) ||
                                        TextUtils.isEmpty(at.getFirstTrialDate())) {
                                    infoCheck = false;
                                    Toast.makeText(FirstTrial.this, getResources().getString(R.string.field_uncomplete), Toast.LENGTH_LONG).show();
                                    break;
                                }
                            }
                        }
                    }
                }
                if (infoCheck) {
                    saveInfo();
                    startActivity(currentIntent);
                }

            }
        });

    }

    /**
     * Click the first item in the Adapter
     */
    private void clickFirstItem() {
        ListView lv = (ListView) findViewById(R.id.instructionalAreasList);
        if (null == lv) {
            toast("ListView is null");
            Log.e("ATGUIDE", "ListView null");
        }
        MergeAdapter m = (MergeAdapter) lv.getAdapter();
        if (null == m) {
            toast("merge adapter is null");
            Log.e("ATGUIDE", "Merge adapter null");
        }
        m.getCount();
        View first = (View) m.getItem(0); // Linear Layout
        if (first != null && first instanceof LinearLayout) {
            LinearLayout taskLay = (LinearLayout) ((LinearLayout) first).getChildAt(1);
            if (taskLay == null) {
                Log.e("ATGUIDE", "task Layout is null");
            } else {
                LinearLayout taskChild = (LinearLayout) taskLay.getChildAt(1);
                if (taskChild != null) {
                    taskChild.getChildAt(0).callOnClick();
                } else {
                    Log.e("ATGUIDE", "task Child at 1 is null");
                }
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // super.onNewIntent(intent);
        Toast.makeText(context, "New Intent Called", Toast.LENGTH_SHORT).show();
        EditText ATname = (EditText) findViewById(R.id.at);
        Uri uri = intent.getData();
        if (uri != null) {
            try {
                vaExploring = "" + intent.getStringExtra("dataFromAIMVANavigator");
//				log("Data from AIMVA Navigator:" + exploringVA);
                ATname.setText("" + vaExploring);
            } catch (Exception e) {
                Log.e("ATGUIDE", "Exception in checkUri caught at line 135: " + e);
                log("Exception" + e);
            }
        }
    }

    /**
     * Set Listener for Assistive technology. Select the listener
     */
    private void setATListener() {

        ImageButton add = (ImageButton) findViewById(R.id.addat);
        add.setImageResource(R.drawable.plusstrategy);
        // add.setLayoutParams(new LinearLayout.LayoutParams());
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AT_List atlist = new AT_List();
                atlist.setFirstTrail(activity);
                atlist.show(fragmentManager, "AT List");
            }
        });
    }

    /**
     * Place the special listener to call navigator app
     */
    private void setSpecialATListener() {

        ImageButton add = (ImageButton) findViewById(R.id.addat);
        add.setImageResource(R.drawable.navigator);
        add.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String stuId = currentIntent.getStringExtra("studentid");
                String data = stuId.substring(0, stuId.length() - 14)
                        + "," + currentIntent.getStringExtra("studentgrade")
                        + "," + currentIntent.getStringExtra("studentparticipant")
                        + "," + URI_FIRST_TRIAL;

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, data);
                sendIntent.putExtra("open", true);
                sendIntent.setType("text/plain");

                PackageManager packageManager = getPackageManager();
                List<ResolveInfo> activities = packageManager.queryIntentActivities(sendIntent, 0);
                boolean isIntentSafe = activities.size() > 0;
                boolean installed = false;
                if (isIntentSafe) {
                    for (ResolveInfo resolveInfo : activities) {
                        if (resolveInfo.activityInfo.packageName.contains("aemnavigator")) {
                            installed = true;
                            sendIntent.setPackage(resolveInfo.activityInfo.packageName);
                            break;
                        }
                    }
                }
                if (installed) {
                    startActivity(sendIntent);
                } else {
                    Toast.makeText(context, "AIM Nav not installed", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    /**
     * Place all areas in the view
     */
    @SuppressLint("InflateParams")
    private void placeArea() {

        /** Modifies: this view */
        /**
         * Effects: places the area, task and AT on the left panels & sets their
         * respective listeners
         */
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
                // Add On click Listener: OnClick: Add extra button to call
                // application
                // add check to the data for incoming URL
                // Auto Fill from the data
                Area nav = getAreaByName(aemNavigator);

                if (null == nav) {
                    nav = new Area(aemNavigator);
                    // nav.addTask();
                    nav.parentId = id++;
                    Task explorer = new Task();
                    explorer.solutions = false;
                    explorer.taskid = id++;
                    explorer.setAreaname(nav.getAreaName());
                    explorer.taskname = vaExploring;
                    nav.addTask(explorer);
                    areaList.add(0, nav);
                }
            }

            for (Area area : areaList) {
                if (area.getAreaName().equals(aemNavigator))
                    continue;
                // For each area get a Row
                LinearLayout areaRow = (LinearLayout) inflater.inflate(R.layout.areataskrow, null);
                areaRow.setId(id += 2);
                // Add the area name
                TextView areaTextView = (TextView) areaRow.findViewById(R.id.areatextview);
                // area.setBackground(getResources().getDrawable(R.drawable.textviewback));
                areaTextView.setText(area.getAreaName());
                areaTextView.setTextAlignment(TextView.TEXT_ALIGNMENT_TEXT_START);
                areaTextView.setLayoutParams(textViewParams);
                areaTextView.setId(area.parentId);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                lp.setMargins(0, 3, 0, 0);

                LinearLayout.LayoutParams tvlp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
                LinearLayout.LayoutParams imglp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

                boolean addArea = false;
                // Set onClick listener to the area name
                // append current task for 1st trial
                for (Task task : area.tasks) {
                    if (task.taskname.equals(vaExploring))
                        continue;
                    Log.i("first ", task.getTaskname() + task.solutions);
                    // If solutions aren't working
                    if (!task.solutions) {
                        addArea = true;
                        // atCount++;
                        LinearLayout taskLayout = new LinearLayout(context);
                        taskLayout.setOrientation(LinearLayout.VERTICAL);
                        taskLayout.setLayoutParams(textViewParams);
                        TextView tasktextView = new TextView(context);
                        tasktextView.setText(task.getTaskname());
                        taskLayout.setTag(area.getAreaName());
                        tasktextView.setLayoutParams(textViewParams);
                        tasktextView.setId(task.taskid);
                        tasktextView.setTextColor(Color.BLACK);
                        tasktextView.setTypeface(null, Typeface.BOLD);
                        tasktextView.setPadding(30, 0, 0, 0);
                        taskLayout.addView(tasktextView);
                        if (task.ats.size() > 0) {
                            for (AT at : task.ats) {
                                if (null != at) {
                                    LinearLayout atLinear = new LinearLayout(this);
                                    atLinear.setLayoutParams(lp);
                                    atLinear.setOrientation(LinearLayout.HORIZONTAL);
                                    atLinear.setGravity(Gravity.CENTER);
                                    //star icon
                                    final ImageView star = new ImageView(context);
                                    star.setLayoutParams(imglp);
                                    star.setImageResource(R.drawable.star);
                                    star.setPadding(5, 5, 5, 5);
                                    TextView assistiveTech = new TextView(context);
                                    assistiveTech.setLayoutParams(tvlp);
                                    assistiveTech.setPadding(55, 0, 0, 0);
                                    assistiveTech.setText(at.getATName());
                                    assistiveTech.setTextColor(Color.BLACK);
                                    assistiveTech.setId(at.id);
                                    if (area.getAreaName().equalsIgnoreCase(aemNavigator)) {
                                        assistiveTech.setOnClickListener(getExplorerATListener());
                                    } else {
                                        assistiveTech.setOnClickListener(getATListener());
                                    }
                                    if (checkStar(at)) {
                                        star.setVisibility(View.VISIBLE);
                                    } else {
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
                            star.setPadding(5, 5, 5, 5);
                            star.setVisibility(View.INVISIBLE);
                            TextView assistiveTech = new TextView(context);
                            assistiveTech.setLayoutParams(tvlp);
                            assistiveTech.setTextColor(Color.BLACK);
                            assistiveTech.setPadding(55, 0, 0, 0);
                            assistiveTech.setId(id++);
                            if (area.getAreaName().equalsIgnoreCase(aemNavigator)) {
                                assistiveTech.setOnClickListener(getExplorerATListener());
                            } else {
                                assistiveTech.setOnClickListener(getATListener());
                            }
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
                    "Error retrieving data  FirstTrial->placeArea() \n" + e);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
//		Toast.makeText(context, "Restart called", Toast.LENGTH_SHORT).show();
    }

    /**
     * Place the elements: Area, Task and ATs from database
     */
    @SuppressLint("InflateParams")
    private void placeAreaFromDB() {
        // Modifies: this view
        /*
         * Effects: places the area, task and AT on the left panels & sets their
         * respective listeners from the database
         */
        try {
            // Initializing data
            // Set params
            LayoutParams textViewParams = new LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            // The list view on left side
            ListView instructional = (ListView) findViewById(R.id.instructionalAreasList);
            // The list of area
            // int atCount = -1;
            if (!isSample) {
                String iepAlt = currentIntent.getStringExtra("iepalt");
                if (null != iepAlt && iepAlt.equalsIgnoreCase("Yes")) {
                    Area nav;
                    Area navOld = getAreaByName(aemNavigator);
                    if (null != navOld) {
                        nav = navOld;
                    } else {
                        nav = new Area(aemNavigator);
                    }
                    if (nav.parentId == 0)
                        nav.parentId = id++;

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
//					exploreAT.ATName = "";
                        exploreAT.participants = "";
                        exploreAT.firstTrialDate = "";
                        exploreAT.task = vaExploring;
                        exploreAT.instructionalArea = aemNavigator;
                    }

                    Task explorer = null;

                    if (null == t) {
                        explorer = new Task();
                        explorer.solutions = false;
                        explorer.taskid = id++;
                        explorer.setAreaname(nav.getAreaName());
                        explorer.taskname = vaExploring;
                    } else {
                        explorer = t;
                    }
                    // explorer.ats.clear();
                    if (explorer.ats.size() == 0)
                        explorer.ats.add(exploreAT);
                    if (nav.tasks.size() == 0)
                        nav.addTask(explorer);
                    if (null == navOld)
                        areaList.add(0, nav);
                }
            }
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 5, 0, 0);

            LinearLayout.LayoutParams tvlp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
            LinearLayout.LayoutParams imglp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            for (Area area : areaList) {
                if (area.getAreaName().equals(aemNavigator))
                    continue;
                // For each area get a Row
                LinearLayout areaRow = (LinearLayout) inflater.inflate(
                        R.layout.areataskrow, null);
                areaRow.setId(id += 2);
                // Add the area name
                TextView areaTextView = (TextView) areaRow
                        .findViewById(R.id.areatextview);
                // area.setBackground(getResources().getDrawable(R.drawable.textviewback));
                areaTextView.setText(area.getAreaName());
                areaTextView
                        .setTextAlignment(TextView.TEXT_ALIGNMENT_TEXT_START);
                areaTextView.setLayoutParams(textViewParams);
                areaTextView.setId(area.parentId);
                boolean addArea = false;

                // Set onClick listener to the area name
                // append current task for 1st trial
                for (Task task : area.tasks) {
                    Log.i("first ", task.getTaskname() + " " + task.solutions);
                    // If solutions aren't working
                    if (!task.solutions) {
                        addArea = true;
                        // atCount++;
                        LinearLayout taskLayout = new LinearLayout(context);
                        taskLayout.setOrientation(LinearLayout.VERTICAL);
                        taskLayout.setLayoutParams(textViewParams);
                        TextView tasktextView = new TextView(context);
                        tasktextView.setText(task.getTaskname());
                        taskLayout.setTag(area.getAreaName());
                        tasktextView.setLayoutParams(textViewParams);
                        tasktextView.setId(task.taskid);
                        tasktextView.setTextColor(Color.BLACK);
                        tasktextView.setTypeface(null, Typeface.BOLD);
                        tasktextView.setPadding(30, 0, 0, 0);
                        taskLayout.addView(tasktextView);

                        if (task.ats.size() > 0) {
                            for (AT at : task.ats) {
                                if (null != at) {
                                    LinearLayout atLinear = new LinearLayout(this);
                                    atLinear.setLayoutParams(lp);
                                    atLinear.setOrientation(LinearLayout.HORIZONTAL);
                                    atLinear.setGravity(Gravity.CENTER);
                                    //star icon
                                    final ImageView star = new ImageView(context);
                                    star.setLayoutParams(imglp);
                                    star.setImageResource(R.drawable.star);
                                    star.setPadding(5, 5, 5, 5);
                                    TextView assistiveTech = new TextView(context);
                                    assistiveTech.setLayoutParams(tvlp);
                                    assistiveTech.setPadding(55, 0, 0, 0);
                                    assistiveTech.setText(at.getATName());
                                    assistiveTech.setTextColor(Color.BLACK);
                                    assistiveTech.setId(at.id);
                                    if (area.getAreaName().equalsIgnoreCase(
                                            aemNavigator)) {
                                        assistiveTech
                                                .setOnClickListener(getExplorerATListener());
                                    } else {
                                        assistiveTech
                                                .setOnClickListener(getATListener());
                                    }
                                    if (checkStar(at)) {
                                        star.setVisibility(View.VISIBLE);
                                    } else {
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
                            star.setPadding(5, 5, 5, 5);
                            star.setVisibility(View.INVISIBLE);
                            TextView assistiveTech = new TextView(context);
                            assistiveTech.setLayoutParams(tvlp);
                            assistiveTech.setTextColor(Color.BLACK);
                            assistiveTech.setPadding(55, 0, 0, 0);
                            assistiveTech.setId(id++);
                            if (area.getAreaName().equalsIgnoreCase(aemNavigator)) {
                                assistiveTech
                                        .setOnClickListener(getExplorerATListener());

                            } else {
                                assistiveTech.setOnClickListener(getATListener());
                            }
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

                DatePickerDialog dialog = new DatePickerDialog(FirstTrial.this,
                        date, myCalendar.get(Calendar.YEAR), myCalendar
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
        String myFormat = "MM/dd/yy"; // In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        datePick.setText(sdf.format(myCalendar.getTime()));
    }


    //highlight the current task item
    private void highLightLayout(View v) {
        //eliminate other items bg color
        ListView lv = (ListView) findViewById(R.id.instructionalAreasList);
        MergeAdapter m = (MergeAdapter) lv.getAdapter();
        int childrenNum = m.getCount();
        for (int i = 0; i < childrenNum; i++) {
            View areaView = (View) m.getItem(i);
            if (areaView != null && areaView instanceof LinearLayout) {
                LinearLayout areaLayout = (LinearLayout) areaView;
                for (int j = 1; j < areaLayout.getChildCount(); j++) {
                    //task name
                    LinearLayout taskLayout = (LinearLayout) areaLayout.getChildAt(j);
                    taskLayout.getChildAt(0).setBackgroundResource(0);
                    for (int k = 1; k < taskLayout.getChildCount(); k++) {
                        //at name + star
                        LinearLayout atLayout = (LinearLayout) taskLayout.getChildAt(k);
                        atLayout.setBackgroundResource(0);
                    }
                }
            }
        }
        LinearLayout atLinear = (LinearLayout) v.getParent();
        atLinear.setBackgroundColor(getResources().getColor(R.color.highlight_blue));
        ((LinearLayout) atLinear.getParent()).getChildAt(0).setBackgroundColor(getResources().getColor(R.color.highlight_blue));
    }

    /**
     * Provide the onclick listener to the special AIM Navigator AT
     *
     * @return Listener to special AT
     */
    public OnClickListener getExplorerATListener() {

        OnClickListener atL = new OnClickListener() {
            @Override
            public void onClick(View v) {
                highLightLayout(v);
                // Remove Previous Listeners if set
                EditText atname = (EditText) findViewById(R.id.at);
                atname.removeTextChangedListener(_ATNameWatcher);
                atname.setHint("Enter service here or click “AIM” button to choose service from AEM Navigator");
//				atname.setText("AEM Navigator");// Default
                EditText participantView = (EditText) findViewById(R.id.participants);
                participantView.removeTextChangedListener(participantsWatcher);
                participantView.setText("");//
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
                setSpecialATListener();
                setAddATListener(v);
                setDeleteATListener(v);
            }
        };
        return atL;

    }

    /**
     * @return OnClickListener for the AT
     */
    public OnClickListener getATListener() {

        OnClickListener atL = new OnClickListener() {

            @Override
            public void onClick(View v) {
                highLightLayout(v);
                // Remove Previous Listeners if set
                EditText atname = (EditText) findViewById(R.id.at);
                atname.removeTextChangedListener(_ATNameWatcher);
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
                setAddATListener(v);
                setDeleteATListener(v);
            }
        };
        return atL;
    }

    /**
     * Sets the given AT to the view.
     *
     * @param at             assitive technology object to be set to view
     * @param currentClicked view selected on left panel
     */
    public void setAtToView(AT at, View currentClicked) {
        /*
         * Requires : the AT Listener to the change in participants clicked
         * view. Modifies: Current view and this. Effects: Sets the given AT to
         * the view.
         */
        TextView instructionalTitle = ((TextView) findViewById(R.id.areatitle));
        instructionalTitle.setText(at.getInstructionalArea());
        instructionalTitle.setTextColor(Color.BLACK);
        instructionalTitle.setTypeface(null, Typeface.BOLD);
        ((TextView) findViewById(R.id.taskname)).setText(at.task);
        ((EditText) findViewById(R.id.at)).setText(at.getATName());
        ((EditText) findViewById(R.id.participants)).setText(at
                .getParticipants());
        ((EditText) findViewById(R.id.date)).setText(at.getFirstTrialDate());
        setATNameListener(at, currentClicked);
        setPartcipantListener(at);
        setCompletionDateListener(at);

    }

    /**
     * Listener to the change in AT Name
     *
     * @param at Assistive Technology type instance
     */
    public void setATNameListener(final AT at, final View currentClicked) {
        EditText atname = (EditText) findViewById(R.id.at);
        atname.removeTextChangedListener(_ATNameWatcher);
        _ATNameWatcher = new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                at.setATName(trimATName(s.toString()));
                ((TextView) currentClicked).setText(trimATName(s.toString()));
                View clickView = (View) findViewById(clickedId);
                LinearLayout parent;
                if (clickView instanceof LinearLayout) {
                    parent = (LinearLayout) ((LinearLayout)
                            ((LinearLayout) clickView).getChildAt(1)).getChildAt(1);
                } else {
                    parent = (LinearLayout) clickView.getParent();
                }
                if (checkStar(at)) {
                    parent.getChildAt(1).setVisibility(View.VISIBLE);
                } else {
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

    private boolean checkStar(AT at) {
        if (!TextUtils.isEmpty(at.getATName().trim()) &&
                !TextUtils.isEmpty(at.getParticipants().trim()) &&
                !TextUtils.isEmpty(at.getFirstTrialDate().trim())) {
            return true;
        }
        return false;
    }

    /**
     * Listener to the change in participants
     *
     * @param at Assistive Technology type instance
     */
    public void setPartcipantListener(final AT at) {
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
                at.setParticipants(s.toString());
                View clickView = (View) findViewById(clickedId);
                LinearLayout parent;
                if (clickView instanceof LinearLayout) {
                    parent = (LinearLayout) ((LinearLayout)
                            ((LinearLayout) clickView).getChildAt(1)).getChildAt(1);
                } else {
                    parent = (LinearLayout) clickView.getParent();
                }
                //int indx=at.id-id;
                if (checkStar(at)) {
                    parent.getChildAt(1).setVisibility(View.VISIBLE);
                } else {
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
     * @param at Assistive Technology type instance
     */
    public void setCompletionDateListener(final AT at) {
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
                at.setFirstTrialDate(s.toString());
                View clickView = (View) findViewById(clickedId);
                LinearLayout parent;
                if (clickView instanceof LinearLayout) {
                    parent = (LinearLayout) ((LinearLayout)
                            ((LinearLayout) clickView).getChildAt(1)).getChildAt(1);
                } else {
                    parent = (LinearLayout) clickView.getParent();
                }
                if (checkStar(at)) {
                    parent.getChildAt(1).setVisibility(View.VISIBLE);
                } else {
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

    //check all at fields are filled before adding new at for current task
    private boolean checkAT() {
        View aTView = (View) findViewById(clickedId);
        LinearLayout parent;
        if (aTView instanceof LinearLayout) {
            parent = (LinearLayout) ((LinearLayout)
                    ((LinearLayout) aTView).getChildAt(1)).getChildAt(1);
        } else {
            parent = (LinearLayout) aTView.getParent();
        }
        TextView atTv = (TextView) parent.getChildAt(0);
        // Parent
        LinearLayout grandParent = (LinearLayout) atTv.getParent().getParent().getParent();
        TextView area = (TextView) grandParent.getChildAt(0);
        CharSequence areaText = area.getText();
        TextView taskView = (TextView) ((LinearLayout) parent.getParent())
                .getChildAt(0);
        Area areaobj = getAreaByName(areaText);
        Task t = areaobj.getTaskById(taskView.getId());
        for (AT at : t.ats) {
            if (!checkStar(at)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Sets the Listener to the ADD New AT Button
     */
    public void setAddATListener(final View op) {
        try {
            // ADD Listener
            OnClickListener addCLick = new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (!isSample) {
                        if (!checkAT()) {
                            Toast.makeText(context, getResources().getString(R.string.field_new_at), Toast.LENGTH_SHORT).show();
                        } else {
                            EditText atname = (EditText) findViewById(R.id.at);
                            atname.removeTextChangedListener(_ATNameWatcher);
                            atname.setText("");
                            EditText participantView = (EditText) findViewById(R.id.participants);
                            participantView.removeTextChangedListener(participantsWatcher);
                            participantView.setText("");
                            EditText dateView = (EditText) findViewById(R.id.date);
                            dateView.removeTextChangedListener(trialDateWatcher);
                            dateView.setText("");
                            LinearLayout taskLayout = (LinearLayout) op.getParent().getParent();
                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                            lp.setMargins(0, 5, 0, 0);

                            LinearLayout.LayoutParams tvlp = new LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
                            LinearLayout.LayoutParams imglp = new LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT);

                            LinearLayout atLinear = new LinearLayout(context);
                            atLinear.setLayoutParams(lp);
                            atLinear.setOrientation(LinearLayout.HORIZONTAL);
                            atLinear.setGravity(Gravity.CENTER);
                            TextView assistiveTech = new TextView(context);
                            assistiveTech.setLayoutParams(tvlp);
                            assistiveTech.setPadding(55, 0, 0, 0);
                            assistiveTech.setTextColor(Color.BLACK);
                            //moved down
                            //assistiveTech.setId(id++);
                            //star icon
                            final ImageView star = new ImageView(context);
                            star.setLayoutParams(imglp);
                            star.setImageResource(R.drawable.star);
                            star.setPadding(5, 5, 5, 5);
                            star.setVisibility(View.INVISIBLE);
                            atLinear.addView(assistiveTech);
                            atLinear.addView(star);

                            LinearLayout areaLayout = (LinearLayout) taskLayout
                                    .getParent();
                            String areaText = ((TextView) areaLayout.getChildAt(0))
                                    .getText().toString();
                            Area areaobj = getAreaByName(areaText);
                            Task t = areaobj.getTaskById(taskLayout.getChildAt(0)
                                    .getId());
                            AT at = new AT();
                            at.task = t.taskname;
//					at.ATName = "Choose AT";
                            int ntasks = t.ats.size();
                            int lstId = t.ats.get(ntasks - 1).id;

                            assistiveTech.setId(lstId + 1);//++

                            at.instructionalArea = areaText;
                            at.id = assistiveTech.getId();

                            t.ats.add(at);
                            setAtToView(at, v);

                            if (t.areaname.equals(aemNavigator)) {
                                assistiveTech.setOnClickListener(getExplorerATListener());
                            } else {
                                assistiveTech.setOnClickListener(getATListener());
                            }
                            Log.d("ATGuide", "Area Text: " + areaText);
                            Log.d("ATGuide", "Area Text: " + areaText);
                            taskLayout.addView(atLinear);
                            assistiveTech.callOnClick();
                        }
                    } else {
                        Toast.makeText(context,
                                "Sample cannot be modified.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            };
            ((Button) findViewById(R.id.addnewat)).setOnClickListener(addCLick);
        } catch (ClassCastException e) {
            Log.e("ATGuide", "" + e);
        } catch (Exception e) {
            Log.e("ATGuide", "" + e);
        }
    }

    /**
     * Deletes the AT from logic and View
     *
     * @param op
     */
    public void setDeleteATListener(final View op) {
        // Requires: current selected AT
        // Modifies: this, current view, area and task
        // Effects: Deletes the AT from logic and View
        try {
            OnClickListener addCLick = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Remove All listeners
                    EditText atname = (EditText) findViewById(R.id.at);
                    atname.removeTextChangedListener(_ATNameWatcher);
                    EditText participantView = (EditText) findViewById(R.id.participants);
                    participantView
                            .removeTextChangedListener(participantsWatcher);
                    EditText dateView = (EditText) findViewById(R.id.date);
                    dateView.removeTextChangedListener(trialDateWatcher);
                    // Get Layouts
                    LinearLayout taskLayout = (LinearLayout) op.getParent().getParent();
                    TextView assistiveTech = (TextView) op;
                    LinearLayout atLinear = (LinearLayout) op.getParent();
                    LinearLayout areaLayout = (LinearLayout) taskLayout
                            .getParent();
                    String areaText = ((TextView) areaLayout.getChildAt(0))
                            .getText().toString();
                    if (!isSample) {
                        // If deletion invalid
                        if (taskLayout.getChildCount() <= 2) {
                            Toast.makeText(context,
                                    "At least one AT is required for a task",
                                    Toast.LENGTH_SHORT).show();
                        } else {// getObjects
                            Area areaobj = getAreaByName(areaText);
                            Task t = areaobj.getTaskById(taskLayout.getChildAt(0)
                                    .getId());
                            // Delete from Logics
                            t.ats.remove(t.getATById(assistiveTech.getId()));
                            PersistenceBean.deleteATObject(context, "trial1"
                                    + PersistenceBean.getCurrentId(context), areaobj.parentId + "", t.taskid, assistiveTech.getId());
                            // Delete from view
                            taskLayout.removeView(atLinear);
                            // Select other AT
                            ((LinearLayout) taskLayout.getChildAt(1)).getChildAt(0).callOnClick();
                        }
                    } else {
                        Toast.makeText(context,
                                "Sample cannot be modified.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            };
            ((Button) findViewById(R.id.deleteat)).setOnClickListener(addCLick);
        } catch (ClassCastException e) {
            Log.e("ATGuide", "" + e);
        } catch (Exception e) {
            Log.e("ATGuide", "" + e);
        }

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

}
