package edu.gmu.ttaconline.atcguide;

import java.io.Serializable;
import java.util.ArrayList;

import com.commonsware.cwac.merge.MergeAdapter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import edu.gmu.ttaconline.atcguide.ui.HelpPage;
import edu.gmu.ttaconline.atcguide.ui.InstructionalAreas;
import edu.gmu.ttaconline.atcguide.ui.MainActivity;

public class TaskForm extends Activity implements Serializable {
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;
    /**
     * Log Tag
     */
    private static final String TAG = "ATGUIDE";
    Intent currentIntent;
    Intent persisted;
    //    String studentid = "";
    Context context;
    TextWatcher watcher[] = new TextWatcher[1000];
    boolean open = false;
    static int id = 3000;
    static int strategyRowid = 0;// to 20
    ArrayList<Integer> areaIds = new ArrayList<Integer>();

    static int clickedId = id;
    LayoutInflater inflater;
    /**
     * if not enough to solve, then go to first trial
     * check solutions twice: first when open this page
     * second when save info
     */
    boolean trial1 = false;
    ArrayList<CharSequence> selectedInstructional;
    ArrayList<String> selectedList;
    ArrayList<String> trial1TextList = new ArrayList<String>();
    ArrayList<Area> areasList = new ArrayList<Area>();
    ArrayList<Area> trial1List = new ArrayList<Area>();
    //after saving, need to delete from trial1id record because trial1 only record data which has no solution
    ArrayList<Area> solutionList = new ArrayList<>();
    MergeAdapter merge = new MergeAdapter();
    TextWatcher taskWatcher;
    Area currentSelection = null;
    TextView currentText;
    Activity activity;

    ImageButton saveBtn;
    ImageButton helpBtn;
    ImageButton homeBtn;

    //check if there are some missing text
    boolean infoCheck = true;
    InputMethodManager imm;

    boolean isSample = false;

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_task_form);
            setCustomActionBar();
            // get from db
            activity = this;
            context = getApplicationContext();
            imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            try {
                currentIntent = getIntentFromDb();// getIntent();//getIntentFromDb();}
            } catch (Exception e) {
                Log.d("ATGuide", "Exception getting Intent from Db" + e);
            }
            isSample = currentIntent.getBooleanExtra("sample", false);
//            studentid = currentIntent.getStringExtra("studentid");
            currentIntent.setData(null);
            inflater = getLayoutInflater();

            // trial for any case ! using current intent
            getData();

            placeAreaFromDB();
            // retrieveArea()if it is old
            LinearLayout first = (LinearLayout) merge.getItem(0);
            if (first.getChildAt(0) != null) {
                first.getChildAt(0).callOnClick();
                ((LinearLayout) (first.getChildAt(1))).getChildAt(0).callOnClick();
                clickedId = ((LinearLayout) (first.getChildAt(1))).getChildAt(0).getId();
            } else {
                first.callOnClick();
            }
            addPlusButtonListener();
            setNextListener();
//            setLogListener();
            validateSolutions();
        } catch (Exception unknown) {
            Log.e("ATGUIDE",
                    "Exception Unknown in Task Form" + unknown.getMessage());
            Toast.makeText(context, "Exception" + unknown, Toast.LENGTH_SHORT)
                    .show();
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
                    Intent intent = new Intent(TaskForm.this, HelpPage.class);
                    startActivity(intent);
                    break;
                case R.id.home:
                    AlertDialog.Builder info = new AlertDialog.Builder(TaskForm.this);
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


    /**
     * Checks if first trial is required for any tasks, identifies the tasks for
     * first trial
     */
    private void validateSolutions() {
        // Abstract function:
        // Get all area
        // Get each task
        // Add the solution not working to separate list
        // save the list
        // For all selected / persisted Area
        trial1 = false;
        trial1List.clear();
        trial1TextList.clear();
        for (Area checkArea : areasList) {
            trial1List.add(checkArea);
            trial1TextList.add("" + checkArea.getAreaName());
            // For each task in this area
            for (Task task : checkArea.tasks) {
                // If any of the strategies in current task are not working
                if (!task.solutions) {
                    trial1 = true;
                }
                //if any task has solution, need to be deleted from db table.
                else {
                    solutionList.add(checkArea);
                    break;
                }
            }
        }
        if (!isSample) {
            PersistenceBean.deleteSolutionRecords(PersistenceBean.getCurrentId(context), "trial1" + PersistenceBean.getCurrentId(context), solutionList, context);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onBackPressed()
     */
    @Override
    public void onBackPressed() {
        // Save Data
        super.onBackPressed();
        saveInfo();

    }

    /**
     * Intent From DB
     *
     * @return Intent fetched from database
     */
    private Intent getIntentFromDb() {
        return PersistenceBean.getExistingIntent(
                PersistenceBean.getCurrentId(getApplicationContext()),
                getApplicationContext());
    }

    /**
     * save information about area/task
     */
    private void saveInfo() {
        if (!isSample) {
            PersistenceBean
                    .persistAreaObject(areasList, PersistenceBean.getCurrentId(getApplicationContext()), context);
            PersistenceBean.persistIntent(
                    PersistenceBean.getCurrentId(getApplicationContext()),
                    currentIntent, context);
        }
        validateSolutions();
    }

    /**
     * Sets listener for the click of next button of this view
     */
    private void setNextListener() {
        Button next = (Button) findViewById(R.id.nextbutton);
        next.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                saveInfo();
                if (trial1) {
                    infoCheck = true;
                    for (Area area : areasList) {
                        if (area.getTasks() == null || area.getTasks().size() <= 0) {
                            infoCheck = false;
                            Toast.makeText(TaskForm.this, getResources().getString(R.string.field_uncomplete), Toast.LENGTH_LONG).show();
                            break;
                        }
                        for (Task task : area.getTasks()) {
                            if (TextUtils.isEmpty(task.getTaskname()) || isStrategyEmpty(v)) {
                                infoCheck = false;
                                Toast.makeText(TaskForm.this, getResources().getString(R.string.field_uncomplete), Toast.LENGTH_LONG).show();
                                break;
                            }
                        }
                    }
                    if (infoCheck) {
                        // Alert About trial 1
                        AlertDialog.Builder info = new AlertDialog.Builder(activity);
                        info.setMessage(getResources()
                                .getString(R.string.trial1nav).toString());
                        info.setCancelable(true);
                        info.setPositiveButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        // FWD to trial 1
                                        currentIntent.setClass(context,
                                                FirstTrial.class);
                                        if (!isSample) {
                                            currentIntent.putExtra("trial1", trial1);
                                            PersistenceBean.persistAreaObject(
                                                    trial1List, "trial1" + PersistenceBean.getCurrentId(context),
                                                    context);
                                            PersistenceBean.persistInstructionalAreas(
                                                    "trial1" + PersistenceBean.getCurrentId(context),
                                                    trial1TextList, context);
                                        }
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
                        infoAlert.show();
                    }
                } else {
                    AlertDialog.Builder info = new AlertDialog.Builder(activity);
                    info.setMessage(getResources().getString(
                            R.string.adequatesolution).toString());
                    info.setCancelable(true);
                    info.setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {

                                    PDFLogic.activity = activity;
                                    // Intent pdfService= new
                                    // Intent(activity.getApplicationContext(),PDFLogic.class);
                                    currentIntent.setClass(context,
                                            PDFLogic.class);
                                    currentIntent
                                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    currentIntent
                                            .setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                    checkPermission();
                                    if (permission) {
                                        Toast.makeText(context, "Please Wait", Toast.LENGTH_SHORT).show();
                                        ((MyApplication) getApplication()).goHome();
                                        Thread pdfThread = new Thread(new PDFLogic());
                                        pdfThread.start();
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
                    // setContentView(layoutResID);
                }
            }
        });

    }

    private static final int MY_PERMISSIONS_REQUEST_WRITE_FILE = 10000;
    private static boolean permission = false;

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_FILE);
        } else {
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
                    ((MyApplication) getApplication()).goHome();
                    Thread pdfThread = new Thread(new PDFLogic());
                    pdfThread.start();
                    ;

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
    
    /*private void setLogListener() {
        Button log = (Button) findViewById(R.id.logbutton);
        log.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Data Logged on Console",
                        Toast.LENGTH_SHORT).show();
                // Display DATA
                Log.d("ATGUIDE",
                        "AREALIST SIZE before retrieving" + areasList.size());
                ArrayList<Area> persistedarea = PersistenceBean
                        .getPersistedAreaObjects( *//*currentIntent.getStringExtra("studentid")*//*PersistenceBean.getCurrentId(getApplicationContext()), context);
                Log.d("ATGUIDE",
                        "starting looop areacount: " + persistedarea.size());
                for (Area area : persistedarea) {
                    ATGuideLogger.LogIt(area);
                    area.logTasks();
                }
            }
        });
        
    }*/

    /**
     * Used to check if there is an empty strategy, used to avoid adding
     * strategy in case of empty strategy
     *
     * @return true if any of the strategy is empty
     */
    public boolean isStrategyEmpty(View v) {
        LinearLayout main = (LinearLayout) findViewById(R.id.strategylayout);
        for (int i = 0; i < main.getChildCount(); i++) {
            View inside = main.getChildAt(i);
            if (inside instanceof LinearLayout) {
                View incepted = ((LinearLayout) inside).getChildAt(0);
                if (incepted instanceof EditText) {
                    EditText strategy = (EditText) incepted;
                    Editable s = strategy.getText();
                    if (s == null) {
                        return true;
                    }
                    if (s.toString().equals("")
                            || s.toString().trim().equals("")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * delete task
     *
     * @param v
     */
    private void setDeleteTaskListener(View v) {
        Button deletetask = (Button) findViewById(R.id.deletetask);
        deletetask.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final LinearLayout area = ((LinearLayout) findViewById(clickedId)
                        .getParent().getParent());
                if (!isSample) {
                    if (area.getChildCount() > 2) {
                        AlertDialog.Builder info = new AlertDialog.Builder(activity);
                        info.setMessage(getResources()
                                .getString(R.string.delete_task_warning).toString());
                        info.setCancelable(true);
                        info.setPositiveButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        View taskLinear = ((LinearLayout) findViewById(clickedId)
                                                .getParent());
                                        area.removeView(taskLinear);
                                        Area areaObj = getAreaByName(((TextView) area.getChildAt(0)).getText());
                                        areaObj.tasks.remove(areaObj.getTaskById(clickedId));
                                        area.getChildAt(0).callOnClick();
                                        merge.notifyDataSetChanged();
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
                    } else {
                        Toast.makeText(context,
                                "At least one task is required for an area",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context,
                            "Sample cannot be modified.",
                            Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void addPlusButtonListener() {
        ImageButton plusButton = (ImageButton) findViewById(R.id.addstrategy);
        plusButton.setOnClickListener(new OnClickListener() {

            @SuppressLint("InflateParams")
            @Override
            public void onClick(View v) {
                if (!isSample) {
                    // Validate if current is not empty
                    if (isStrategyEmpty(v)) {
                        try {
                            AlertDialog.Builder builder = new AlertDialog.Builder(
                                    TaskForm.this);
                            builder.setCancelable(true);
                            builder.setTitle("Empty strategy");
                            builder.setMessage("Please fill out the empty strategy first!");
                            builder.setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog,
                                                            int which) {
                                            dialog.dismiss();
                                        }
                                    });
                            builder.setCancelable(false);
                            builder.show();
                        } catch (Exception unknown) {
                            Log.e(TAG, "Ex !!" + unknown.getMessage());
                        }
                        return;
                    }
                    // Add new row
                    final LinearLayout row = (LinearLayout) getLayoutInflater()
                            .inflate(R.layout.strategyrow, null);
                    row.setId(strategyRowid++);
                    // TextView task = (TextView) findViewById(clickedId);
                    final TextView taskView = (TextView) findViewById(clickedId);
                    TextView area = (TextView) (((LinearLayout) taskView
                            .getParent().getParent()).getChildAt(0));
                    Area localObj = getAreaByName(area.getText());
                    final Task t = localObj.getTaskById(taskView.getId());
                    EditText strategyText = (EditText) row
                            .findViewById(R.id.strategyedittext);
                    strategyText.setId(strategyRowid++);
                    strategyText.requestFocus();
                    imm.showSoftInput(strategyText, InputMethodManager.SHOW_FORCED);
                    final int id = strategyText.getId();
                    watcher[id] = new TextWatcher() {
                        @Override
                        public void onTextChanged(CharSequence s, int start,
                                                  int before, int count) {
                        }

                        @Override
                        public void beforeTextChanged(CharSequence s, int start,
                                                      int count, int after) {
                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            t.strategies.put("" + id, new String(s.toString()));
                            if (!TextUtils.isEmpty(s.toString()) && checkStar(t)) {
                                (((LinearLayout) taskView
                                        .getParent()).getChildAt(1)).setVisibility(View.VISIBLE);
                            } else {
                                (((LinearLayout) taskView
                                        .getParent()).getChildAt(1)).setVisibility(View.INVISIBLE);
                            }
                        }
                    };
                    strategyText.addTextChangedListener(watcher[id]);
                    ((LinearLayout) findViewById(R.id.strategylayout)).addView(row);
                    // -----------------------------------------------------------
                    ImageButton delete = (ImageButton) row
                            .findViewById(R.id.deletethisstrategy);
                    delete.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!isSample) {
                                imm.hideSoftInputFromWindow(v.getWindowToken(), 0); //强制隐藏键盘
                                ((LinearLayout) findViewById(R.id.strategylayout))
                                        .removeView(row.findViewById(row.getId()));
                                t.strategies.remove(id + "");
                            } else {
                                Toast.makeText(context,
                                        "Sample cannot be modified.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }

                    });
                } else {
                    Toast.makeText(context,
                            "Sample cannot be modified.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    void onDeleteFirstStrategy() {
        ImageButton deletefirststrategy = (ImageButton) findViewById(R.id.deletestrategy0);
        deletefirststrategy.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "You cannot delete this strategy !",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getData() {
        try {
            selectedInstructional = PersistenceBean.getPersistedAreaList(
                    PersistenceBean.getCurrentId(getApplicationContext()),
//                    currentIntent.getStringExtra("studentid"),
                    getApplicationContext());
            areasList = PersistenceBean.getPersistedAreaObjects(
                    PersistenceBean.getCurrentId(getApplicationContext()),
//                    currentIntent.getStringExtra("studentid"),
                    getApplicationContext());
            Log.d("task ", PersistenceBean.getCurrentId(getApplicationContext()));
            selectedList = new ArrayList<String>();
            open = currentIntent.getBooleanExtra("open", false);
            for (CharSequence selected : selectedInstructional) {
                selectedList.add((String) selected);
                Log.d(TAG, "selected list: " + selected);
            }
        } catch (Exception e) {
        }
    }

    public Area getAreaById(int id) {
        for (Area area : areasList) {
            if (area != null && area.getParentId() == id) {
                return area;
            }
        }
        return null;
    }

    public Area getAreaByName(CharSequence areaname) {
        if (areaname != null)
            for (Area area : areasList) {
                if (area != null
                        && area.getAreaName().trim()
                        .equalsIgnoreCase(areaname.toString().trim())) {
                    return area;
                }
            }
        return null;
    }

    @SuppressLint("InflateParams")
    public void placeAreaFromDB() {
        LayoutParams textViewParams = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        ListView instructional = (ListView) findViewById(R.id.instructionalAreasList);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 5, 0, 0);

        LinearLayout.LayoutParams tvlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        LinearLayout.LayoutParams imglp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        for (CharSequence areaText : selectedInstructional) {
            LinearLayout v = (LinearLayout) LayoutInflater.from(this).inflate(
                    R.layout.areataskrow, null);
            v.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            v.setOrientation(LinearLayout.VERTICAL);
            v.setId(id++);
            TextView area = (TextView) v.findViewById(R.id.areatextview);

            // area.setBackground(getResources().getDrawable(R.drawable.textviewback));
            Log.i("TaskForm", areaText + "");
            area.setText(areaText);
            area.setTextAlignment(TextView.TEXT_ALIGNMENT_TEXT_START);
            area.setLayoutParams(textViewParams);
            area.setId(id++);

            Area areaObject = null;

            areaObject = getAreaByName(areaText);
            if (areaObject != null) {
                area.setId(areaObject.getParentId());
            } else {
                areaObject = new Area((String) areaText);
                // areaObject.setAreaName((String) areaText);
                areaObject.setParentId(area.getId());
            }

            if (!areaObject.tasks.isEmpty()) {
                for (Task t : areaObject.tasks) {
                    LinearLayout taskLinear = new LinearLayout(this);
                    taskLinear.setLayoutParams(lp);
                    taskLinear.setOrientation(LinearLayout.HORIZONTAL);
                    taskLinear.setGravity(Gravity.CENTER);
                    //star icon
                    final ImageView star = new ImageView(context);
                    star.setLayoutParams(imglp);
                    star.setImageResource(R.drawable.star);
                    star.setPadding(4, 4, 4, 4);

                    //task name
                    TextView task = new TextView(context);
                    task.setLayoutParams(tvlp);
                    task.setText(t.getTaskname());
                    task.setId(t.taskid);
                    task.setPadding(30, 0, 0, 0);
                    task.setTextColor(Color.BLACK);
                    if (checkStar(t)) {
                        star.setVisibility(View.VISIBLE);
                    } else {
                        star.setVisibility(View.INVISIBLE);
                    }

                    task.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            highLightLayout(v);
                            TextView taskview = (TextView) v;
                            CharSequence taskviewText = taskview.getText();
                            clickedId = v.getId();
                            setDeleteTaskListener(v);
//                            highlightThis(v);
                            LinearLayout parent = (LinearLayout) v.getParent().getParent();
                            TextView area = (TextView) parent.getChildAt(0);
                            CharSequence areaText = area.getText();
                            Area areaobj = getAreaById(area.getId());
                            Task t = areaobj.getTaskById(v.getId());
                            t.setTaskname(taskviewText.toString());
                            // TODO: CODE HERE TO ADD STRATEGIES
                            LinearLayout strategyLayout = (LinearLayout) findViewById(R.id.strategylayout);
                            strategyLayout.removeAllViews();
                            final LinearLayout row0 = (LinearLayout) getLayoutInflater()
                                    .inflate(R.layout.strategyrow, null);
                            row0.setId(0);
                            strategyLayout.addView(row0);
                            EditText strategy = (EditText) row0
                                    .findViewById(R.id.strategyedittext);
                            if (strategy == null)
                                Toast.makeText(context, "Strategy is null",
                                        Toast.LENGTH_SHORT).show();
                            // Insert strategies
                            if (t.strategies.keySet() != null
                                    && t.strategies.get("0") != null
                                    && t.strategies.get("0") != "")
                                strategy.setText(t.strategies.get("0"));
                            else
                                strategy.setText("");
                            if (t.strategies.keySet().size() > 0) {// Loop through all
                                // of them
                                strategyLayout.removeAllViews(); // instead of finding
                                // and removing text

                                // change listeners, just start over for every click
                                // improved efficiency O(n)

                                int i = -1;
                                // for each of the strategy in the task object
                                for (final String key : t.strategies.keySet()) {
                                    // Toast.makeText(context,
                                    // t.strategies.keySet().toString() + " in loop " +
                                    // t.strategies.keySet().size(),
                                    // Toast.LENGTH_SHORT).show();
                                    i++;
                                    final int id = i;
                                    // get a new row
                                    final LinearLayout row = (LinearLayout) getLayoutInflater()
                                            .inflate(R.layout.strategyrow, null);
                                    // set id
                                    row.setId(id);
                                    EditText strategyText = (EditText) row
                                            .findViewById(R.id.strategyedittext);
                                    if (t.strategies.keySet() != null
                                            && t.strategies.get(key) != null
                                            && t.strategies.get(key) != "")
                                        strategyText.setText(t.strategies.get(key));
                                    else
                                        strategyText.setText("");
                                    watcher[id] = new TextWatcher() {
                                        @Override
                                        public void onTextChanged(CharSequence s,
                                                                  int start, int before, int count) {
                                        }

                                        @Override
                                        public void beforeTextChanged(CharSequence s,
                                                                      int start, int count, int after) {
                                        }

                                        @Override
                                        public void afterTextChanged(Editable s) {
                                            View v1 = (View) findViewById(clickedId);
                                            LinearLayout parent = (LinearLayout) v1
                                                    .getParent().getParent();
                                            TextView area = (TextView) parent
                                                    .getChildAt(0);
                                            Area areaobj = getAreaById(area.getId());
                                            Task t = areaobj.getTaskById(clickedId);
                                            t.strategies.remove(id + "");
                                            t.strategies.put(id + "",
                                                    new String(s.toString()));
                                            if (!TextUtils.isEmpty(s.toString()) && checkStar(t)) {
                                                star.setVisibility(View.VISIBLE);
                                            } else {
                                                star.setVisibility(View.INVISIBLE);
                                            }
                                        }
                                    };
                                    strategyText.addTextChangedListener(watcher[id]);
                                    ((LinearLayout) findViewById(R.id.strategylayout))
                                            .addView(row);
                                    ImageButton delete = (ImageButton) row
                                            .findViewById(R.id.deletethisstrategy);
                                    delete.setOnClickListener(new OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if (!isSample) {
                                                imm.hideSoftInputFromWindow(v.getWindowToken(), 0); //强制隐藏键盘
                                                if (((LinearLayout) v.getParent()
                                                        .getParent()).getChildCount() > 1) {
                                                    ((LinearLayout) findViewById(R.id.strategylayout))
                                                            .removeView(row
                                                                    .findViewById(row
                                                                            .getId()));
                                                    View v1 = (View) findViewById(clickedId);
                                                    LinearLayout parent = (LinearLayout) v1
                                                            .getParent().getParent();
                                                    TextView area = (TextView) parent
                                                            .getChildAt(0);
                                                    Area areaobj = getAreaById(area.getId());
                                                    Task t = areaobj.getTaskById(clickedId);
                                                    t.strategies.remove(id + "");
                                                    if (checkStar(t)) {
                                                        star.setVisibility(View.VISIBLE);
                                                    } else {
                                                        star.setVisibility(View.INVISIBLE);
                                                    }
                                                    Toast.makeText(context,
                                                            "Strategy deleted",
                                                            Toast.LENGTH_SHORT).show();

                                                } else
                                                    Toast.makeText(
                                                            context,
                                                            "At least one strategy is required",
                                                            Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(
                                                        context,
                                                        "Sample cannot be modified.",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                    // add accordingly
                                }
                            } else {
                                watcher[0] = new TextWatcher() {
                                    @Override
                                    public void onTextChanged(CharSequence s, int start,
                                                              int before, int count) {
                                    }

                                    @Override
                                    public void beforeTextChanged(CharSequence s,
                                                                  int start, int count, int after) {
                                    }

                                    @Override
                                    public void afterTextChanged(Editable s) {
                                        View v1 = (View) findViewById(clickedId);
                                        LinearLayout parent = (LinearLayout) v1.getParent().getParent();
                                        TextView area = (TextView) parent.getChildAt(0);
                                        Area areaobj = getAreaById(area.getId());
                                        Task t = areaobj.getTaskById(clickedId);
                                        t.strategies.remove("0");
                                        t.strategies.put("0", new String(s.toString()));
                                        if (!TextUtils.isEmpty(s.toString()) && checkStar(t)) {
                                            star.setVisibility(View.VISIBLE);
                                        } else {
                                            star.setVisibility(View.INVISIBLE);
                                        }
                                    }
                                };
                                strategy.addTextChangedListener(watcher[0]);
                            }

                            ((TextView) findViewById(R.id.tasktitle)).setText(areaText);
                            final EditText taskname = (EditText) findViewById(R.id.taskname);
                            taskname.setText(taskviewText);
                            taskname.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                                @Override
                                public void onFocusChange(View view, boolean b) {
                                    if (b) {
                                        String hint = taskname.getHint().toString();
                                        taskname.setTag(hint);
                                        taskname.setHint("");
                                    } else {
                                        String hint = taskname.getTag().toString();
                                        taskname.setHint(hint);
                                    }
                                }
                            });
                            taskname.requestFocus();
                            imm.showSoftInput(taskname, InputMethodManager.SHOW_FORCED);
                            taskname.removeTextChangedListener(taskWatcher);
                            taskWatcher = new TextWatcher() {
                                @Override
                                public void onTextChanged(CharSequence s, int start,
                                                          int before, int count) {
                                }

                                @Override
                                public void beforeTextChanged(CharSequence s,
                                                              int start, int count, int after) {
                                    // TODO Auto-generated method stub
                                }

                                @Override
                                public void afterTextChanged(Editable s) {
                                    // TODO Auto-generated method stub
                                    try {
                                        // Toast.makeText(context, "Text Changed",
                                        // Toast.LENGTH_SHORT).show();
                                        TextView task = (TextView) findViewById(clickedId);
                                        task.setText(s);
                                        View v1 = (View) findViewById(clickedId);
                                        LinearLayout parent = (LinearLayout) v1
                                                .getParent().getParent();
                                        TextView area = (TextView) parent.getChildAt(0);
                                        Area areaobj = getAreaById(area.getId());
                                        Task t = areaobj.getTaskById(clickedId);
                                        t.setTaskname(s.toString());
                                        if (!TextUtils.isEmpty(s.toString()) && checkStar(t)) {
                                            ((LinearLayout) v1
                                                    .getParent()).getChildAt(1).setVisibility(View.VISIBLE);
                                        } else {
                                            ((LinearLayout) v1
                                                    .getParent()).getChildAt(1).setVisibility(View.INVISIBLE);
                                        }
                                        // Toast.makeText(context, "Editable text:"+s,
                                        // Toast.LENGTH_SHORT).show();
                                    } catch (Exception e) {
                                        Log.e(TAG, "ex: " + e.getMessage());
                                    }
                                }
                            };
                            taskname.addTextChangedListener(taskWatcher);
                            RadioGroup solutions = (RadioGroup) findViewById(R.id.solutionradiogroup);
                            if (t.solutions) {
                                ((RadioButton) solutions.findViewById(R.id.solutionyes))
                                        .setChecked(true);
                            } else {
                                ((RadioButton) solutions.findViewById(R.id.solutionno))
                                        .setChecked(true);

                            }
                            solutions
                                    .setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                                        @Override
                                        public void onCheckedChanged(RadioGroup group,
                                                                     int checkedId) {
                                            RadioButton checkedSolution = (RadioButton) group
                                                    .findViewById(checkedId);
                                            boolean isChecked = checkedSolution
                                                    .isChecked();
                                            View v1 = (View) findViewById(clickedId);
                                            LinearLayout parent = (LinearLayout) v1
                                                    .getParent().getParent();
                                            TextView area = (TextView) parent
                                                    .getChildAt(0);
                                            Area areaobj = getAreaById(area.getId());
                                            Task t = areaobj.getTaskById(clickedId);
                                            if (isChecked
                                                    && (checkedId == R.id.solutionyes)) {
                                                t.solutions = true;
//                                                Toast.makeText(context, "checked",
//                                                        Toast.LENGTH_SHORT).show();

                                            } else if (isChecked
                                                    && (checkedId == R.id.solutionno)) {
                                                t.solutions = false;
//                                                Toast.makeText(context, "UN Checked",
//                                                        Toast.LENGTH_SHORT).show();
                                            }

                                        }
                                    });
                        }
                    });
                    taskLinear.addView(task);
                    taskLinear.addView(star);
                    v.addView(taskLinear);
                }

            } else {
                LinearLayout taskLinear = new LinearLayout(this);
                taskLinear.setLayoutParams(lp);
                taskLinear.setOrientation(LinearLayout.HORIZONTAL);
                taskLinear.setGravity(Gravity.CENTER);
                taskLinear.setBackgroundColor(getResources().getColor(R.color.star_bg));

                //star icon
                final ImageView star = new ImageView(context);
                star.setLayoutParams(imglp);
                star.setImageResource(R.drawable.star);
                star.setVisibility(View.INVISIBLE);
                star.setPadding(4, 4, 4, 4);
                //task name
                TextView task = new TextView(context);
                task.setLayoutParams(tvlp);
                task.setId(id++);
                // Bean Storage
                Task taskObject = new Task();
                taskObject.setAreaname(areaObject.getAreaName());
                taskObject.taskid = (task.getId());
                areaObject.tasks.add(taskObject);
                areasList.add(areaObject);
                task.setPadding(30, 0, 0, 0);
                task.setTextColor(Color.BLACK);
                task.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        highLightLayout(v);
                        TextView taskview = (TextView) v;
                        CharSequence taskviewText = taskview.getText();
                        clickedId = v.getId();
                        setDeleteTaskListener(v);
//                        highlightThis(v);
                        LinearLayout parent = (LinearLayout) v.getParent().getParent();
                        TextView area = (TextView) parent.getChildAt(0);
                        CharSequence areaText = area.getText();
                        Area areaobj = getAreaById(area.getId());
                        Task t = areaobj.getTaskById(v.getId());
                        t.setTaskname(taskviewText.toString());
                        // TODO: CODE HERE TO ADD STRATEGIES
                        LinearLayout strategyLayout = (LinearLayout) findViewById(R.id.strategylayout);
                        strategyLayout.removeAllViews();
                        final LinearLayout row0 = (LinearLayout) getLayoutInflater()
                                .inflate(R.layout.strategyrow, null);
                        row0.setId(0);
                        strategyLayout.addView(row0);
                        EditText strategy = (EditText) row0
                                .findViewById(R.id.strategyedittext);
                        if (strategy == null)
                            Toast.makeText(context, "Strategy edittext is null",
                                    Toast.LENGTH_SHORT).show();
                        // Insert strategies
                        if (t.strategies.keySet() != null
                                && t.strategies.get("0") != null
                                && t.strategies.get("0") != "")
                            strategy.setText(t.strategies.get("0"));
                        else
                            strategy.setText("");
                        if (t.strategies.keySet().size() > 0) {// Loop through all
                            // of them
                            strategyLayout.removeAllViews(); // instead of finding
                            // and removing text

                            // change listeners, just start over for every click
                            // improved efficiency O(n)

                            int i = -1;
                            // for each of the strategy in the task object
                            for (final String key : t.strategies.keySet()) {
                                // Toast.makeText(context,
                                // t.strategies.keySet().toString() + " in loop " +
                                // t.strategies.keySet().size(),
                                // Toast.LENGTH_SHORT).show();
                                i++;
                                final int id = i;
                                // get a new row
                                final LinearLayout row = (LinearLayout) getLayoutInflater()
                                        .inflate(R.layout.strategyrow, null);
                                // set id
                                row.setId(id);
                                EditText strategyText = (EditText) row
                                        .findViewById(R.id.strategyedittext);
                                if (t.strategies.keySet() != null
                                        && t.strategies.get(key) != null
                                        && t.strategies.get(key) != "")
                                    strategyText.setText(t.strategies.get(key));
                                else
                                    strategyText.setText("");
                                watcher[id] = new TextWatcher() {
                                    @Override
                                    public void onTextChanged(CharSequence s,
                                                              int start, int before, int count) {
                                    }

                                    @Override
                                    public void beforeTextChanged(CharSequence s,
                                                                  int start, int count, int after) {
                                    }

                                    @Override
                                    public void afterTextChanged(Editable s) {
                                        View v1 = (View) findViewById(clickedId);
                                        LinearLayout parent = (LinearLayout) v1
                                                .getParent().getParent();
                                        TextView area = (TextView) parent
                                                .getChildAt(0);
                                        Area areaobj = getAreaById(area.getId());
                                        Task t = areaobj.getTaskById(clickedId);
                                        t.strategies.remove(id + "");
                                        t.strategies.put(id + "",
                                                new String(s.toString()));
                                        if (!TextUtils.isEmpty(s.toString()) && checkStar(t)) {
                                            star.setVisibility(View.VISIBLE);
                                        } else {
                                            star.setVisibility(View.INVISIBLE);
                                        }
                                    }
                                };
                                strategyText.addTextChangedListener(watcher[id]);
                                ((LinearLayout) findViewById(R.id.strategylayout))
                                        .addView(row);
                                ImageButton delete = (ImageButton) row
                                        .findViewById(R.id.deletethisstrategy);
                                delete.setOnClickListener(new OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (!isSample) {
                                            imm.hideSoftInputFromWindow(v.getWindowToken(), 0); //强制隐藏键盘
                                            if (((LinearLayout) v.getParent()
                                                    .getParent()).getChildCount() > 1) {
                                                ((LinearLayout) findViewById(R.id.strategylayout))
                                                        .removeView(row
                                                                .findViewById(row
                                                                        .getId()));
                                                View v1 = (View) findViewById(clickedId);
                                                LinearLayout parent = (LinearLayout) v1
                                                        .getParent().getParent();
                                                TextView area = (TextView) parent
                                                        .getChildAt(0);
                                                Area areaobj = getAreaById(area.getId());
                                                Task t = areaobj.getTaskById(clickedId);
                                                t.strategies.remove(id + "");
                                                if (checkStar(t)) {
                                                    star.setVisibility(View.VISIBLE);
                                                } else {
                                                    star.setVisibility(View.INVISIBLE);
                                                }
                                                Toast.makeText(context,
                                                        "Strategy deleted",
                                                        Toast.LENGTH_SHORT).show();

                                            } else
                                                Toast.makeText(
                                                        context,
                                                        "At least one strategy is required",
                                                        Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(
                                                    context,
                                                    "Sample cannot be modified.",
                                                    Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                });
                                // add accordingly
                            }
                        } else {
                            watcher[0] = new TextWatcher() {
                                @Override
                                public void onTextChanged(CharSequence s, int start,
                                                          int before, int count) {
                                }

                                @Override
                                public void beforeTextChanged(CharSequence s,
                                                              int start, int count, int after) {
                                }

                                @Override
                                public void afterTextChanged(Editable s) {
                                    View v1 = (View) findViewById(clickedId);
                                    LinearLayout parent = (LinearLayout) v1.getParent().getParent();
                                    TextView area = (TextView) parent.getChildAt(0);
                                    Area areaobj = getAreaById(area.getId());
                                    Task t = areaobj.getTaskById(clickedId);
                                    t.strategies.remove("0");
                                    t.strategies.put("0", new String(s.toString()));
                                    if (!TextUtils.isEmpty(s.toString()) && checkStar(t)) {
                                        star.setVisibility(View.VISIBLE);
                                    } else {
                                        star.setVisibility(View.INVISIBLE);
                                    }
                                }
                            };
                            strategy.addTextChangedListener(watcher[0]);
                        }

                        ((TextView) findViewById(R.id.tasktitle)).setText(areaText);
                        final EditText taskname = (EditText) findViewById(R.id.taskname);
                        taskname.setText(taskviewText);
                        taskname.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                            @Override
                            public void onFocusChange(View view, boolean b) {
                                if (b) {
                                    String hint = taskname.getHint().toString();
                                    taskname.setTag(hint);
                                    taskname.setHint("");
                                } else {
                                    String hint = taskname.getTag().toString();
                                    taskname.setHint(hint);
                                }
                            }
                        });
                        taskname.requestFocus();
                        imm.showSoftInput(taskname, InputMethodManager.SHOW_FORCED);
                        taskname.removeTextChangedListener(taskWatcher);
                        taskWatcher = new TextWatcher() {
                            @Override
                            public void onTextChanged(CharSequence s, int start,
                                                      int before, int count) {
                            }

                            @Override
                            public void beforeTextChanged(CharSequence s,
                                                          int start, int count, int after) {
                                // TODO Auto-generated method stub
                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                                // TODO Auto-generated method stub
                                try {
                                    // Toast.makeText(context, "Text Changed",
                                    // Toast.LENGTH_SHORT).show();
                                    TextView task = (TextView) findViewById(clickedId);
                                    task.setText(s);
                                    View v1 = (View) findViewById(clickedId);
                                    LinearLayout parent = (LinearLayout) v1
                                            .getParent().getParent();
                                    TextView area = (TextView) parent.getChildAt(0);
                                    Area areaobj = getAreaById(area.getId());
                                    Task t = areaobj.getTaskById(clickedId);
                                    t.setTaskname(s.toString());
                                    if (!TextUtils.isEmpty(s.toString()) && checkStar(t)) {
                                        ((LinearLayout) v1
                                                .getParent()).getChildAt(1).setVisibility(View.VISIBLE);
                                    } else {
                                        ((LinearLayout) v1
                                                .getParent()).getChildAt(1).setVisibility(View.INVISIBLE);
                                    }
                                    // Toast.makeText(context, "Editable text:"+s,
                                    // Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    Log.e(TAG, "ex: " + e.getMessage());
                                }
                            }
                        };
                        taskname.addTextChangedListener(taskWatcher);
                        RadioGroup solutions = (RadioGroup) findViewById(R.id.solutionradiogroup);
                        if (t.solutions) {
                            ((RadioButton) solutions.findViewById(R.id.solutionyes))
                                    .setChecked(true);
                        } else {
                            ((RadioButton) solutions.findViewById(R.id.solutionno))
                                    .setChecked(true);

                        }
                        solutions
                                .setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                                    @Override
                                    public void onCheckedChanged(RadioGroup group,
                                                                 int checkedId) {
                                        RadioButton checkedSolution = (RadioButton) group
                                                .findViewById(checkedId);
                                        boolean isChecked = checkedSolution
                                                .isChecked();
                                        View v1 = (View) findViewById(clickedId);
                                        LinearLayout parent = (LinearLayout) v1
                                                .getParent().getParent();
                                        TextView area = (TextView) parent
                                                .getChildAt(0);
                                        Area areaobj = getAreaById(area.getId());
                                        Task t = areaobj.getTaskById(clickedId);
                                        if (isChecked
                                                && (checkedId == R.id.solutionyes)) {
                                            t.solutions = true;
                                        /*Toast.makeText(context, "checked",
                                                Toast.LENGTH_SHORT).show();*/

                                        } else if (isChecked
                                                && (checkedId == R.id.solutionno)) {
                                            t.solutions = false;
                                        /*Toast.makeText(context, "UN Checked",
                                                Toast.LENGTH_SHORT).show();*/
                                        }

                                    }
                                });
                    }
                });
                taskLinear.addView(task);
                taskLinear.addView(star);
                v.addView(taskLinear);
            }
            // TODO: Enter task name is in intent
            area.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    try {
                        TextView curr = (TextView) v;
                        // currentText = curr;
                        TextView areatitle = (TextView) findViewById(R.id.tasktitle);
                        areatitle.setText(curr.getText());
//                        highlightThis((View) v.getParent());
                        // set on edit listener
                        LinearLayout area = (LinearLayout) v.getParent();
                        // final int parentid = area.getId();
                        ((LinearLayout) (area.getChildAt(1))).getChildAt(0).callOnClick();
                        Button addTask = (Button) findViewById(R.id.addnewtask);
                        addTask.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                infoCheck = true;
                                // AddNewTextView to current area
                                // blank all text views
                                // set ids++
                                // set on click listener
                                // store task to the object
                                // FIND CURRENT CLICKED
                                View currentClicked = (View) findViewById(clickedId);
                                // FIND PARENT
                                LinearLayout parent;
                                if (currentClicked instanceof LinearLayout) {
                                    parent = (LinearLayout) currentClicked;
                                } else {
                                    parent = (LinearLayout) currentClicked.getParent().getParent();
                                }


                                // get area id
                                // get area object
                                // Create empty View
                                // Current Task
                                TextView areaT = (TextView) parent.getChildAt(0);
                                CharSequence areaText = areaT.getText();
                                Area area1 = getAreaByName(areaText);
                                for (Task task : area1.getTasks()) {
                                    if (TextUtils.isEmpty(task.getTaskname()) || isStrategyEmpty(v)) {
                                        infoCheck = false;
                                        break;
                                    }
                                }

                                if (infoCheck) {
                                    if (!isSample) {
                                        onAddNewTask(parent, areaText);
                                    } else {
                                        Toast.makeText(context,
                                                "Sample cannot be modified.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(TaskForm.this, getResources().getString(R.string.field_uncomplete), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    } catch (Exception e) {
                        Log.e("ATGUIDE", e.getMessage());
                    }
                }
            });
            merge.addView(v);
            merge.setActive(v, true);
        }
        instructional.setAdapter(merge);

    }

    //check whether current task has been filled out
    private boolean checkStar(Task t) {
        if (!TextUtils.isEmpty(t.getTaskname())) {
            int i = 0;
            for (String key : t.strategies.keySet()) {
                if (t.strategies.get(key) != null && t.strategies.get(key).trim() != "") {
                    return true;
                } else if (i == t.strategies.keySet().size() - 1) {
                    return false;
                }
                i++;
            }
        }
        return false;
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
                //area name
                areaLayout.getChildAt(0).setBackgroundResource(0);
                for (int j = 1; j < areaLayout.getChildCount(); j++) {
                    //task name +star layout
                    LinearLayout taskLayout = (LinearLayout) areaLayout.getChildAt(j);
                    taskLayout.setBackgroundResource(0);
                }
            }
        }
        //taskLinear: taskName + start
        LinearLayout taskLinear = (LinearLayout) v.getParent();
        taskLinear.setBackgroundColor(getResources().getColor(R.color.highlight_blue));
        //areaName
        ((LinearLayout) taskLinear.getParent()).getChildAt(0).setBackgroundColor(getResources().getColor(R.color.highlight_blue));
    }

    /**
     * highlight this text view
     * highlight the clicked task area
     */
    /*public void highlightThis(View tv) {
        ListView lv = (ListView) findViewById(R.id.instructionalAreasList);
        MergeAdapter m = (MergeAdapter) lv.getAdapter();
        m.getCount();
        //remove bg resource by setting 0
        for (int c = 0; c <= m.getCount(); c++) {
            View tt = (View) m.getItem(c);
            if (tt != null && tt instanceof LinearLayout) {
                LinearLayout ll = (LinearLayout) tt;
                ll.setBackgroundResource(0);
                for (int i = 1; i < ll.getChildCount(); i++) {
                    LinearLayout tc = (LinearLayout) ll.getChildAt(i);
                    tc.setBackgroundResource(0);
                }
            }
        }
        tv.setBackground(getResources().getDrawable(R.drawable.highlighted));
        //task name area color: cyan
        if (tv.getParent().getParent() instanceof LinearLayout)
            ((LinearLayout) tv.getParent().getParent()).getChildAt(0).setBackgroundColor(
                    Color.CYAN);
    }*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.task_form, menu);
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

    public void onAddNewTask(LinearLayout parent, CharSequence areaText) {
//        Toast.makeText(activity, "new task", Toast.LENGTH_SHORT).show();
        ((TextView) findViewById(R.id.tasktitle)).setText(areaText);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 5, 0, 0);
        LinearLayout taskLinear = new LinearLayout(this);
        taskLinear.setLayoutParams(lp);
        taskLinear.setOrientation(LinearLayout.HORIZONTAL);
        taskLinear.setGravity(Gravity.CENTER);
        taskLinear.setBackgroundColor(getResources().getColor(R.color.star_bg));

        LinearLayout.LayoutParams tvlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        LinearLayout.LayoutParams imglp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView tv = new TextView(context);
        tv.setLayoutParams(tvlp);
        tv.setId(id++);
        tv.setTextColor(Color.BLACK);
        tv.setPadding(30, 0, 0, 0);

        final ImageView imageView = new ImageView(this);
        imageView.setLayoutParams(imglp);
        imageView.setImageResource(R.drawable.star);
        imageView.setVisibility(View.INVISIBLE);
        imageView.setPadding(4, 4, 4, 4);

        Area areaObject = getAreaByName(areaText);
        final Task t = new Task();
        t.setTaskname(tv.getText().toString());
        t.taskid = tv.getId();
        RadioGroup solutions = (RadioGroup) findViewById(R.id.solutionradiogroup);

        //solutions default on solution-no
        /*t.solutions = false;
        ((RadioButton) solutions.findViewById(R.id.solutionno))
                .setChecked(true);*/
        solutions.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton checkedSolution = (RadioButton) group
                        .findViewById(checkedId);
                boolean isChecked = checkedSolution.isChecked();
                if (isChecked && (checkedId == R.id.solutionyes)) {
                    t.solutions = true;
//                            Toast.makeText(context, "checked",
//                                    Toast.LENGTH_SHORT).show();
                } else {
                    t.solutions = false;
//                            Toast.makeText(context, "UN checked",
//                                    Toast.LENGTH_SHORT).show();
                }

            }
        });
        areaObject.addTask(t);
        // On click listener of the task
        tv.setOnClickListener(new OnClickListener() {
            @SuppressLint("InflateParams")
            @Override
            public void onClick(View v) {
                highLightLayout(v);
                TextView taskview = (TextView) v;
                CharSequence taskviewText = taskview.getText();
                clickedId = v.getId();
                setDeleteTaskListener(v);
//                highlightThis(v);
                LinearLayout parent = (LinearLayout) v.getParent().getParent();
                TextView area = (TextView) parent.getChildAt(0);
                CharSequence areaText = area.getText();
                // Area Object is already created and is in the area list
                Area areaobj = getAreaById(area.getId());
                // Create task for this
                Task t = areaobj.getTaskById(v.getId());// ? what if task not
                // present
                Task t1 = new Task();
                t1.copyTask(t);
                t.setTaskname(taskviewText.toString());
                // TODO: CODE HERE TO ADD STRATEGIES
                LinearLayout strategyLayout = (LinearLayout) findViewById(R.id.strategylayout);
                // Remove All views
                strategyLayout.removeAllViews();
                final LinearLayout row0 = (LinearLayout) getLayoutInflater()
                        .inflate(R.layout.strategyrow, null);
                row0.setId(0);
                strategyLayout.addView(row0);
                EditText strategy = (EditText) row0
                        .findViewById(R.id.strategyedittext);

                if (strategy == null)
                    Toast.makeText(context, "Strategy is null",
                            Toast.LENGTH_SHORT).show();

                // Insert strategies
                if (t.strategies.keySet() != null
                        && t.strategies.get("0") != null
                        && t.strategies.get("0") != "")
                    strategy.setText(t.strategies.get("0"));
                else
                    strategy.setText("");
                if (t.strategies.keySet().size() > 0) {// Loop through all of
                    // them
                    strategyLayout.removeAllViews(); // instead of finding and
                    // removing text
                    // change listeners, just start over for every click
                    // improved efficiency O(n)
                    int i = -1;
                    // for each of the strategy in the task object
                    for (final String key : t.strategies.keySet()) {
                        // Toast.makeText(context,
                        // t.strategies.keySet().toString() + " in loop " +
                        // t.strategies.keySet().size(),
                        // Toast.LENGTH_SHORT).show();
                        i++;
                        final int id = i;
                        // get a new row
                        final LinearLayout row = (LinearLayout) getLayoutInflater()
                                .inflate(R.layout.strategyrow, null);
                        // set id
                        row.setId(id);
                        EditText strategyText = (EditText) row
                                .findViewById(R.id.strategyedittext);
                        if (t.strategies.keySet() != null
                                && t.strategies.get(key) != null
                                && t.strategies.get(key) != "")
                            strategyText.setText(t.strategies.get(key));
                        else
                            strategyText.setText("");
                        watcher[id] = new TextWatcher() {
                            @Override
                            public void onTextChanged(CharSequence s,
                                                      int start, int before, int count) {
                            }

                            @Override
                            public void beforeTextChanged(CharSequence s,
                                                          int start, int count, int after) {
                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                                View v = (View) findViewById(clickedId);
                                LinearLayout parent = (LinearLayout) v
                                        .getParent().getParent();
                                TextView area = (TextView) parent.getChildAt(0);
                                Area areaObj = getAreaById(area.getId());
                                Task t = areaObj.getTaskById(clickedId);

                                t.strategies.remove(id + "");
                                t.strategies.put(id + "",
                                        new String(s.toString()));
                                if (!TextUtils.isEmpty(s.toString()) && checkStar(t)) {
                                    imageView.setVisibility(View.VISIBLE);
                                } else {
                                    imageView.setVisibility(View.INVISIBLE);
                                }
                            }
                        };
                        // Strategy Text
                        strategyText.addTextChangedListener(watcher[id]);
                        ((LinearLayout) findViewById(R.id.strategylayout))
                                .addView(row);
                        ImageButton delete = (ImageButton) row
                                .findViewById(R.id.deletethisstrategy);
                        delete.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (!isSample) {
                                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0); //强制隐藏键盘
                                    if (((LinearLayout) v.getParent().getParent())
                                            .getChildCount() > 1) {
                                        ((LinearLayout) findViewById(R.id.strategylayout))
                                                .removeView(row.findViewById(row
                                                        .getId()));
                                        View v1 = (View) findViewById(clickedId);
                                        LinearLayout parent = (LinearLayout) v1
                                                .getParent().getParent();
                                        TextView area = (TextView) parent
                                                .getChildAt(0);
                                        Area areaObj = getAreaById(area.getId());
                                        Task t = areaObj.getTaskById(clickedId);

                                        t.strategies.remove(id + "");
                                        if (checkStar(t)) {
                                            imageView.setVisibility(View.VISIBLE);
                                        } else {
                                            imageView.setVisibility(View.INVISIBLE);
                                        }
                                        Toast.makeText(context, "Strategy deleted",
                                                Toast.LENGTH_SHORT).show();
                                    } else
                                        Toast.makeText(
                                                context,
                                                "At least one strategy is required",
                                                Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(
                                            context,
                                            "Sample cannot be modified.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        // add accordingly
                    }
                } else {
                    // First strategy
                    watcher[0] = new TextWatcher() {
                        @Override
                        public void onTextChanged(CharSequence s, int start,
                                                  int before, int count) {
                        }

                        @Override
                        public void beforeTextChanged(CharSequence s, int start,
                                                      int count, int after) {
                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            View v1 = (View) findViewById(clickedId);
                            LinearLayout parent = (LinearLayout) v1.getParent().getParent();
                            TextView area = (TextView) parent.getChildAt(0);
                            Area areaObj = getAreaById(area.getId());
                            Task t = areaObj.getTaskById(clickedId);
                            t.strategies.remove("0");
                            t.strategies.put("0", new String(s.toString()));
                            if (!TextUtils.isEmpty(s.toString()) && checkStar(t)) {
                                imageView.setVisibility(View.VISIBLE);
                            } else {
                                imageView.setVisibility(View.INVISIBLE);
                            }
                        }
                    };
                    // Add listener to first strategy
                    strategy.addTextChangedListener(watcher[0]);
                }
                ((TextView) findViewById(R.id.tasktitle)).setText(areaText);
                final EditText taskname = (EditText) findViewById(R.id.taskname);
                taskname.setText(taskviewText);
                taskname.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View view, boolean b) {
                        if (b) {
                            String hint = taskname.getHint().toString();
                            taskname.setTag(hint);
                            taskname.setHint("");
                        } else {
                            String hint = taskname.getTag().toString();
                            taskname.setHint(hint);
                        }
                    }
                });
                taskname.requestFocus();
                imm.showSoftInput(taskname, InputMethodManager.SHOW_FORCED);
                taskname.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count, int after) {
                        // TODO Auto-generated method stub
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        // TODO Auto-generated method stub
                        try {
                            // Toast.makeText(context, "Text Changed",
                            // Toast.LENGTH_SHORT).show();
                            TextView task = (TextView) findViewById(clickedId);
                            task.setText(s);
                            View v1 = (View) findViewById(clickedId);
                            LinearLayout parent = (LinearLayout) v1.getParent().getParent();
                            TextView area = (TextView) parent.getChildAt(0);
                            Area areaObj = getAreaById(area.getId());
                            Task t = areaObj.getTaskById(clickedId);
                            t.setTaskname(task.getText().toString());
                            if (!TextUtils.isEmpty(s.toString()) && checkStar(t)) {
                                ((LinearLayout) v1.getParent()).getChildAt(1).setVisibility(View.VISIBLE);
                            } else {
                                ((LinearLayout) v1.getParent()).getChildAt(1).setVisibility(View.INVISIBLE);
                            }
                            // Toast.makeText(context, "Editable text:"+s,
                            // Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Log.e(TAG, "ex: " + e.getMessage());
                        }
                    }
                });

            }

        });

        taskLinear.addView(tv);
        taskLinear.addView(imageView);
        // ADD EMPTY VIEW TO THIS
        parent.addView(taskLinear);
        tv.callOnClick();
    }

}
