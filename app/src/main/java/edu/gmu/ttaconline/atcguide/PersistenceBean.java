package edu.gmu.ttaconline.atcguide;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import edu.gmu.ttaconline.atcguide.SQLiteContract.ATStore;
import edu.gmu.ttaconline.atcguide.SQLiteContract.AreaStore;
import edu.gmu.ttaconline.atcguide.SQLiteContract.FeedEntry;
import edu.gmu.ttaconline.atcguide.SQLiteContract.IntentStore;
import edu.gmu.ttaconline.atcguide.SQLiteContract.SelectedArea;
import edu.gmu.ttaconline.atcguide.SQLiteContract.StrategyStore;
import edu.gmu.ttaconline.atcguide.SQLiteContract.TaskStore;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;


/**
 * ORM for ATGuide application Java bean to persist data to the SQLite DB.
 * Storing details of current intent and retrieving all previous values
 * utilizing the helper and contract classes This class is the part of the
 * ATGUIDE application built for KIHD, CEHD, GMU. Spring 2017
 *
 * @author Animesh Jain | Java programmer -CEHD GMU |MS CS class of 2018
 */
public class PersistenceBean {
    // Static Methods

    /**
     * Method to input data from the first form of the screen in creating new
     * student record.
     */
    public static Intent persistInputFormData(ViewGroup v, Context context) {
        // Requires: view group containing the required fields and current
        // application context
        // Modifies: SQL Database, Intent
        // Effect: stores the details in this context and view group in the
        // intent and the Database

        boolean result = false;
        Log.d("ATGUIDE", "Data Persistence Started");
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
        System.out.println(" " + " " + studentid + "  " + studentgrade
                + studentschool + studentparticipant + day + month + year);
        // SQLiteDatabase
        // db=context.openOrCreateDatabase("student",context.MODE_PRIVATE,null);

        SQLiteDbHelper mDbHelper = new SQLiteDbHelper(context);
        @SuppressWarnings("deprecation")
        java.sql.Date date = new java.sql.Date(year, month, day);
        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        mDbHelper.onUpgrade(db, 2, 3);
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(FeedEntry.STUDENT_ID, studentid);
        values.put(FeedEntry.STUDENT_GRADE, studentgrade);
        values.put(FeedEntry.STUDENT_SCHOOL, studentschool);
        values.put(FeedEntry.STUDENT_PARTICIPANTS, studentparticipant);
        values.put(FeedEntry.STUDENT_DATE, date.toString());

        // values.put(FeedEntry.STUDENT_IEPGOALS, studentgrade);

        // Insert the new row, returning the primary key value of the new row
        try {
            db.insert(FeedEntry.STUDENT, null, values);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
            Log.e("ATGUIDE",
                    "Exception while inserting Student: " + e.getMessage());
        } finally {
            db.close();
        }
        Log.d("ATGUIDE", "Data Persistence ended with : " + result);
        Intent intent = new Intent();
        intent.putExtra("studentid", studentid);
        intent.putExtra("studentgrade", studentgrade);
        intent.putExtra("studentparticipant", studentparticipant);
        intent.putExtra("studentschool", studentschool);
        intent.putExtra("date", date.toString());
        Log.d("ATGUIDE", "date " + date.toString());
        return intent;
    }

    /**
     * Method to persist data from IEP Goals Activity
     */
    public static void persistIEGoals(String studentid, String iepGoal,
                                      Context context) {
        SQLiteDbHelper mDbHelper = new SQLiteDbHelper(context);
        ContentValues values = new ContentValues();
        int rows = 0;
        values.put(FeedEntry.STUDENT_IEPGOAL, iepGoal);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        try {
            rows = db.update(FeedEntry.STUDENT, values, ""
                    + FeedEntry.STUDENT_ID + "='" + studentid + "'", null);

        } catch (SQLException e) {
            Log.e("ATGUIDE",
                    "SQLException while inserting student: " + e.getMessage());
        } catch (Exception e) {
            Log.e("ATGUIDE",
                    "Other Exception while inserting student: "
                            + e.getMessage());
        }
        db.close();
        if (rows > 0) {
//			Toast.makeText(context, "" + rows + " record(s) affected",
//					Toast.LENGTH_SHORT).show();
//			Log.d("ATGUIDE","");

        }
        Log.d("ATGUIDE", "IEP Goals persisted, number of row(s): " + rows);
    }

    /**
     * Method to persist data from instructional areas activity
     */
    public static void persistInstructionalAreas(String studentId,
                                                 ArrayList<String> selectedInstructionalAreas, Context context) {
        SQLiteDbHelper mDbHelper = new SQLiteDbHelper(context);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        // delete before inserting for uniqueness of values
        db.execSQL("DELETE FROM " + SelectedArea.TABLE_NAME + " WHERE "
                + SelectedArea.COL_ID + " = '" + studentId + "'");
        for (String area : selectedInstructionalAreas) {
            ContentValues values = new ContentValues();
            values.put(SelectedArea.COL_ID, studentId);
            values.put(SelectedArea.COL_AREA, area);
            db.insert(SelectedArea.TABLE_NAME, null, values);
        }
        db.close();
    }

    /**
     * Method to persist current intent data
     */
    public static boolean persistIntent(String studentId, Intent intent,
                                        Context context) {

        boolean result = true;
        try {
            String intentDescription = null;
            SQLiteDbHelper mDbHelper = new SQLiteDbHelper(context);
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            db.execSQL("DELETE FROM " + IntentStore.TABLE_NAME + " WHERE "
                    + IntentStore.COLUMN_NAME_ID + " = '" + studentId + "'");
            intentDescription = intent.toUri(0);
            ContentValues values = new ContentValues();
            values.put(IntentStore.COLUMN_NAME_ID, studentId);
            values.put(IntentStore.COLUMN_NAME_INTENT, intentDescription);
            long rows = 0;
            rows = db.insert(IntentStore.TABLE_NAME, null, values);
            db.close();
            if (rows > 0) {
//				Toast.makeText(context, "" + rows + " inserted",
//						Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e("ATGUIDE", "Error in persisting intent " + e.getStackTrace());
            result = false;
        }

        return result;
    }

    /**
     * Method to retrieve persisted area list for this student
     */
    public static ArrayList<CharSequence> getPersistedAreaList(String studentid, Context context) {
        ArrayList<CharSequence> persistedAreas = new ArrayList<CharSequence>();
        SQLiteDbHelper mDbHelper = new SQLiteDbHelper(context);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Cursor cursor;
        try {
            Log.d("ATGUIDE", "Retrieving cursor from DB");

            cursor = db.query(true, SelectedArea.TABLE_NAME,
                    new String[]{SelectedArea.COL_AREA}, SelectedArea.COL_ID
                            + " = " + "'" + studentid + "'", null, null, null,
                    null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    Log.d("ATGUIDE", "get area list to instructional areas");
                    persistedAreas.add(cursor.getString(0));
                    cursor.moveToNext();
                }

            }
            db.close();
        } catch (Exception e) {
            Log.e("ATUGUIDE",
                    "Error in retrieving persisted area list " + e.getMessage());
            return null;
        }
        return persistedAreas;
    }

    /**
     * Method to retrieve persisted intent from the database
     */
    public static Intent getExistingIntent(String studentId, Context context) {
        Intent requiredIntent = null;
        Log.i("ATGUIDE", "Searching for persisted intent with student id :"
                + studentId);
        int records = 0;
        Log.d("ATGUIDE", "before Db Helper");

        SQLiteDbHelper mDbHelper = new SQLiteDbHelper(context);
        Log.d("ATGUIDE", "before getting database");
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        // ContentValues values = new ContentValues();
        String intentDescription = null;
        Cursor cursor;

        try {
            Log.d("ATGUIDE", "Trying to get cursor");
            cursor = db.query(IntentStore.TABLE_NAME,
                    new String[]{IntentStore.COLUMN_NAME_INTENT},
                    IntentStore.COLUMN_NAME_ID + " = " + "'" + studentId + "'",
                    null, null, null, null);

        } catch (Exception e) {
            Log.e("ATUGUIDE", "Error GETTING CURSOR " + e.getMessage());
            return null;
        }
        if (cursor == null) {
            Log.e("ATGUIDE", "Cursor is null ");
        }
        int i = 0;
        Log.d("ATGUIDE", "before cursor while ");
        cursor.moveToFirst();

        Log.d("ATGUIDE", "While started: " + (i++));
        intentDescription = cursor.getString(0);
        Log.d("ATGUIDE", "INTENT DESCRIPTION " + intentDescription);
        records++;
        try {
            requiredIntent = Intent.parseUri(intentDescription, 0);
        } catch (URISyntaxException e) {
            Log.e("ATGUIDE", "Error parsing intent: " + e.getStackTrace());
        }

//		Toast.makeText(context, "Retrieved: " + records + " records",
//				Toast.LENGTH_SHORT).show();
        if (requiredIntent == null) {
            Log.e("ATGUIDE", "Required intent is null ");
        }
        db.close();
        return requiredIntent;
    }

    /**
     * Method to check if given student id is already present in the database
     */
    public static boolean isExistingRecord(String studentId, Context context) {
        SQLiteDbHelper mDbHelper = new SQLiteDbHelper(
                context.getApplicationContext());
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Cursor cursor = db.query(IntentStore.TABLE_NAME,
                new String[]{IntentStore.COLUMN_NAME_INTENT},
                IntentStore.COLUMN_NAME_ID + " like " + "'" + studentId + "'",
                null, null, null, null);
        if (cursor.getCount() > 0)
            return true;
        else
            return false;
    }

    /**
     * Persists current student id
     */
    public static void persistCurrentId(String studentId, Context context) {
        try {
            SQLiteDbHelper mDbHelper = new SQLiteDbHelper(context);
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(SelectedArea.COL_ID, studentId);
            db.execSQL("DELETE FROM CURRENT_DATA");
            db.insert("CURRENT_DATA", null, values);
            db.close();
        } catch (Exception e) {
            Log.e("ATGUIDE", "Exception while persisting current student id");
        }
    }

    //persist student trial record
    //rank: 1 first trial, 2 second trial
    public static void persistTrial(String studentId, Context context, int rank) {
        try {
            SQLiteDbHelper mDbHelper = new SQLiteDbHelper(context);
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM "
                            + SQLiteContract.TrialStore.TABLE_NAME + " WHERE "
                            + SQLiteContract.TrialStore.COL_STUDENT_ID + "= '" + studentId + "'",
                    null);
            //have this student record before, update
            if (cursor.moveToNext()) {
                Log.i("Trial", "update trial record");
                if (rank == 1) {
                    String update = "UPDATE " + SQLiteContract.TrialStore.TABLE_NAME
                            + " set " + SQLiteContract.TrialStore.TRIAL_1 + "=" + "'" + 1 + "'" + " where " +
                            SQLiteContract.TrialStore.COL_STUDENT_ID + "=" + "'" + studentId + "'";
                    db.execSQL(update);
                } else {
                    String update = "UPDATE " + SQLiteContract.TrialStore.TABLE_NAME
                            + " set " + SQLiteContract.TrialStore.TRIAL_2 + "=" + "'" + 1 + "'" + " where " +
                            SQLiteContract.TrialStore.COL_STUDENT_ID + "=" + "'" + studentId + "'";
                    db.execSQL(update);
                }
            } else {
                Log.i("Trial", "create trial record");
                ContentValues values = new ContentValues();
                values.put(SQLiteContract.TrialStore.COL_STUDENT_ID, studentId);
                if (rank == 1) {
                    values.put(SQLiteContract.TrialStore.TRIAL_1, 1);
                    values.put(SQLiteContract.TrialStore.TRIAL_2, 0);
                } else {
                    values.put(SQLiteContract.TrialStore.TRIAL_1, 1);
                    values.put(SQLiteContract.TrialStore.TRIAL_2, 1);
                }
                db.insert(SQLiteContract.TrialStore.TABLE_NAME, null, values);
            }
            db.close();
        } catch (Exception e) {
            Log.e("ATGUIDE", "Exception while persisting current student id");
        }
    }

    //if cur student has trial record
    public static int getTrialRecord(String studentId, Context context) {
        int trial = 0;
        SQLiteDbHelper mDbHelper = new SQLiteDbHelper(context);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Cursor cursor;
        try {
            cursor = db.rawQuery("SELECT * FROM "
                            + SQLiteContract.TrialStore.TABLE_NAME + " WHERE "
                            + SQLiteContract.TrialStore.COL_STUDENT_ID + "= '" + studentId + "'",
                    null);

            if (cursor.moveToNext()) {
                trial = cursor.getInt(cursor
                        .getColumnIndex(SQLiteContract.TrialStore.TRIAL_1));
                if (trial == 1) {
                    int trial2 = cursor.getInt(cursor
                            .getColumnIndex(SQLiteContract.TrialStore.TRIAL_2));
                    db.close();
                    if (trial2 == 1) {
                        return 2;
                    } else {
                        return 1;
                    }
                } else {
                    return 0;
                }
            } else {
                return 0;
            }
        } catch (Exception e) {
            Log.e("ATGUIDE", "Error retrieving the trial record");
        }
        return 0;
    }

    /**
     * Retrieves current running student id from the database
     */
    public static String getCurrentId(Context context) {
        // Requires: current application context
        // Effects: Returns the current running id of this student

        SQLiteDbHelper mDbHelper = new SQLiteDbHelper(context);
        String currentId = null;
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Cursor cursor;
        try {
            Log.d("ATGUIDE", "Trying to get current id cursor");
            cursor = db.rawQuery("select * from CURRENT_DATA", null);
            cursor.moveToFirst();
            currentId = cursor.getString(0);
        } catch (Exception e) {
            Log.e("ATGUIDE", "Error retrieving the current id");
        }
        db.close();
        return currentId;
    }

    /**
     * Method to persist task object
     */
    public static void persistTaskObject(Task task, String studentid,
                                         String areaId, Context context) {
        int taskid = task.taskid;
        String taskName = task.getTaskname();
        String areaName = task.getAreaname();
        String solution = null;
        String trial1solution = null;
        String trial2solution = null;

        if (task.solutions) {
            solution = "yes";
        } else
            solution = "no";

        if (task.trial1solutions) {
            trial1solution = "yes";
        } else
            trial1solution = "no";

        if (task.trial2solutions) {
            trial2solution = "yes";
        } else
            trial2solution = "no";


        Set<String> StrategyKeys = task.strategies.keySet();
        // studentid, taskid
        List<AT> ats = task.ats;
        try {
            Log.d("ATGUIDE", "Inserting into task store");
            SQLiteDbHelper mDbHelper = new SQLiteDbHelper(context);
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            // REMOVE PREVIOUS TASKS
            values.put(TaskStore.COL_TASK_ID, taskid);
            values.put(TaskStore.COL_STUDENT_ID, studentid);
            values.put(TaskStore.COL_TASK_NAME, taskName);
            values.put(TaskStore.COL_AREA_NAME, areaName);
            values.put(TaskStore.COL_AREA_ID, areaId);
            values.put(TaskStore.COL_SOLUTION, solution);

            values.put(TaskStore.COL_SOLUTION_T1, trial1solution);
            values.put(TaskStore.COL_SOLUTION_T2, trial2solution);

            db.insert(TaskStore.TABLE_NAME, null, values);
            Log.d("ATGUIDE",
                    "Inserting into strategy store on an iterative loop");
            for (String key : StrategyKeys) {
                // Loop Through all the strategies
                values = new ContentValues();
                values.put(StrategyStore.COL_STUDENT_ID, studentid);
                values.put(StrategyStore.COL_TASKID, taskid);
                values.put(StrategyStore.COL_STRATEGY_ID, key);
                values.put(StrategyStore.COL_STRATEGY_TEXT,
                        task.strategies.get(key));
                db.insert(StrategyStore.TABLE_NAME, null, values);
            }
            db.close();
            for (AT at : ats) {
                persistATObject(at, task, studentid, areaId, context);
            }
        } catch (Exception e) {
            Log.e("ATGUIDE", "Exception while persisting current task  " + e);
        }
    }

    /**
     * Method to persist AT object
     */
    public static void persistATObject(AT at, Task task, String studentid,
                                       String areaId, Context context) {
        int taskid = task.taskid;
        String taskName = task.getTaskname();
        String ATName = at.ATName;
        int ATID = at.id;
        String firstTrialDate = at.firstTrialDate;
        String participants = at.participants;
        String secondTrialDate = at.secondTrialDate;
        Log.d("ATGUIDE", "PersistenceBean.persistATObject() 400 participants:"
                + participants);
        String solutionWorking = at.solutionWorking + "";
        String areaName = task.getAreaname();
//		String solution = null;

        // studentid, taskid
        try {
            Log.d("ATGUIDE", "Inserting into AT store");
            SQLiteDbHelper mDbHelper = new SQLiteDbHelper(context);
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            db.execSQL("DELETE FROM " + ATStore.TABLE_NAME + " WHERE "
                    + ATStore.COL_STUDENT_ID + " = '" + studentid + "' AND "
                    + ATStore.COL_AREA_ID + "= '" + areaId + "' AND "
                    + ATStore.COL_TASK_ID + " = '" + taskid + "'" + " AND "
                    + "" + ATStore.COL_AT_ID + " = '" + ATID + "' ");
            ContentValues values = new ContentValues();
            // REMOVE PREVIOUS TASKS
            values.put(ATStore.COL_STUDENT_ID, studentid);
            values.put(ATStore.COL_AREA_ID, areaId);
            values.put(ATStore.COL_TASK_ID, taskid);
            values.put(ATStore.COL_AT_ID, ATID);
            values.put(ATStore.COL_ATNAME, ATName);
            values.put(ATStore.COL_PARTICIPANTS, participants);
            values.put(ATStore.COL_1stTrialDate, firstTrialDate);
            values.put(ATStore.COL_1stTrialWorking, at.firstWorking);
            values.put(ATStore.COL_AREA_NAME, at.getInstructionalArea());
            values.put(ATStore.COL_TASK_NAME, taskName);
            values.put(ATStore.COL_2ndTrialDate, secondTrialDate);

            values.put(ATStore.COL_SOLUTION, solutionWorking);
            values.put(ATStore.COL_TRIAL1_COMPLETION, at.trial1CompletionDate);
            values.put(ATStore.COL_TRIAL1_Action, at.trial1Action);
            values.put(ATStore.COL_TRIAL1_Persons, at.trial1Persons);
            values.put(ATStore.COL_SOLUTION_T1, at.trial2solutionWorking);
            values.put(ATStore.COL_SOLUTION_T1, at.trial2solutionWorking);
            values.put(ATStore.COL_TRIAL2_COMPLETION, at.trial2CompletionDate);
            values.put(ATStore.COL_TRIAL2_Action, at.trial2Action);
            values.put(ATStore.COL_TRIAL2_Persons, at.trial2Persons);
            values.put(ATStore.COL_PARTICIPANTS_T2, at.trial2Participants);

            db.insert(ATStore.TABLE_NAME, null, values);
            db.close();
        } catch (Exception e) {
            Log.e("ATGUIDE",
                    "Exception while persisting current AT PersistenceBean.persistATObject() "
                            + e);
        }
    }

    /**
     * delete at from the task from area
     *
     * @param context
     * @param studentid
     * @param areaId
     * @param taskid
     * @param ATID
     */
    public static void deleteATObject(Context context, String studentid, String areaId, int taskid, int ATID) {
        SQLiteDbHelper mDbHelper = new SQLiteDbHelper(context);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.execSQL("DELETE FROM " + ATStore.TABLE_NAME + " WHERE "
                + ATStore.COL_STUDENT_ID + " = '" + studentid + "' AND "
                + ATStore.COL_AREA_ID + "= '" + areaId + "' AND "
                + ATStore.COL_TASK_ID + " = '" + taskid + "'" + " AND "
                + "" + ATStore.COL_AT_ID + " = '" + ATID + "' ");
        db.close();
    }

    /**
     * Method to persist Area object
     */
    public static void persistAreaObject(ArrayList<Area> areasList,
                                         String studentid, Context context) {
        // Abstract function:=
        // Get this area object
        // Persist all its details into one table
        // get all the associated tasks with this area&&StudentId
        // For each task: persist(Task)

        SQLiteDbHelper mDbHelper = new SQLiteDbHelper(context);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        for (Area area : areasList) {
            String areaname = area.getAreaName();
            int parentid = area.getParentId();
            ArrayList<Task> tasks = area.getTasks();
            try {
                Log.d("ATGUIDE", "Inserting into Area store");

                //only update the information, but can not delete related info with this student
                //so that the old useless info will be left in table, and get method will return useful data as well
                db.execSQL("DELETE FROM " + AreaStore.TABLE_NAME + " WHERE "
                        + AreaStore.COL_STUDENT_ID + " = '" + studentid
                        + "' AND " + AreaStore.COL_AREA_ID + " = '"
                        + parentid + "'");
                ContentValues values = new ContentValues();
                values.put(AreaStore.COL_AREA_NAME, areaname);
                values.put(AreaStore.COL_STUDENT_ID, studentid);
                values.put(AreaStore.COL_AREA_ID, parentid);
                db.insert(AreaStore.TABLE_NAME, null, values);
                //delete all the old records about the area
                db.execSQL("DELETE FROM " + TaskStore.TABLE_NAME + " WHERE "
                        + TaskStore.COL_STUDENT_ID + " = '" + studentid + "' AND "
                        + TaskStore.COL_AREA_ID + "= '" + parentid + "'");
                Log.d("ATGUIDE",
                        "Inserting into task store on an iterative loop");
                for (Task task : tasks) {
                    db.execSQL("DELETE FROM " + StrategyStore.TABLE_NAME + " WHERE "
                            + StrategyStore.COL_STUDENT_ID + " = '" + studentid + "' AND "
                            + StrategyStore.COL_TASKID + " = '" + task.taskid + "'");
                    persistTaskObject(task, studentid, parentid + "", context);
                }
                // for(String key:StrategyKeys){
                // //Loop Through all the strategies
                //
                // }
            } catch (Exception e) {
                Log.e("ATGUIDE",
                        "Exception while persisting current area object " + e);
                db.close();
            }
        }
        db.close();
    }

    /**
     * when unselecting the instructional areas, the related area and task and at should be deleted at the same time
     *
     * @param studentId
     * @param areaName
     * @param context
     */
    public static void deleteAreaRelatedRecords(String studentId, String areaName, Context context) {
        SQLiteDbHelper mDbHelper = new SQLiteDbHelper(context);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.execSQL("DELETE FROM " + SelectedArea.TABLE_NAME + " WHERE " + SelectedArea.COL_ID + " = '" + studentId + "' AND " + SelectedArea.COL_AREA + "= '" + areaName + "'");
        db.execSQL("DELETE FROM " + SelectedArea.TABLE_NAME + " WHERE " + SelectedArea.COL_ID + " = 'trial1" + studentId + "' AND " + SelectedArea.COL_AREA + "= '" + areaName + "'");
        db.execSQL("DELETE FROM " + SelectedArea.TABLE_NAME + " WHERE " + SelectedArea.COL_ID + " = 'trial2" + studentId + "' AND " + SelectedArea.COL_AREA + "= '" + areaName + "'");
        //delete area, task, at
        db.execSQL("DELETE FROM " + AreaStore.TABLE_NAME + " WHERE " + AreaStore.COL_STUDENT_ID + " = '" + studentId + "' AND " + AreaStore.COL_AREA_NAME + "= '" + areaName + "'");
        db.execSQL("DELETE FROM " + TaskStore.TABLE_NAME + " WHERE " + TaskStore.COL_STUDENT_ID + " = '" + studentId + "' AND " + TaskStore.COL_AREA_NAME + "= '" + areaName + "'");
        db.execSQL("DELETE FROM " + ATStore.TABLE_NAME + " WHERE " + ATStore.COL_STUDENT_ID + " = '" + studentId + "' AND " + ATStore.COL_AREA_NAME + "= '" + areaName + "'");
        //delete related first trial and second trial data
        db.execSQL("DELETE FROM " + AreaStore.TABLE_NAME + " WHERE " + AreaStore.COL_STUDENT_ID + " = 'trial1" + studentId + "' AND " + AreaStore.COL_AREA_NAME + "= '" + areaName + "'");
        db.execSQL("DELETE FROM " + TaskStore.TABLE_NAME + " WHERE " + TaskStore.COL_STUDENT_ID + " = 'trial1" + studentId + "' AND " + TaskStore.COL_AREA_NAME + "= '" + areaName + "'");
        db.execSQL("DELETE FROM " + ATStore.TABLE_NAME + " WHERE " + ATStore.COL_STUDENT_ID + " = 'trial1" + studentId + "' AND " + ATStore.COL_AREA_NAME + "= '" + areaName + "'");
        db.execSQL("DELETE FROM " + AreaStore.TABLE_NAME + " WHERE " + AreaStore.COL_STUDENT_ID + " = 'trial2" + studentId + "' AND " + AreaStore.COL_AREA_NAME + "= '" + areaName + "'");
        db.execSQL("DELETE FROM " + TaskStore.TABLE_NAME + " WHERE " + TaskStore.COL_STUDENT_ID + " = 'trial2" + studentId + "' AND " + TaskStore.COL_AREA_NAME + "= '" + areaName + "'");
        db.execSQL("DELETE FROM " + ATStore.TABLE_NAME + " WHERE " + ATStore.COL_STUDENT_ID + " = 'trial2" + studentId + "' AND " + ATStore.COL_AREA_NAME + "= '" + areaName + "'");
        db.close();
    }


    public static String deleteOtherAreaRecord(String studentId, Context context, String[] list1, String[] list2) {
        SQLiteDbHelper mDbHelper = new SQLiteDbHelper(context);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM "
                        + SelectedArea.TABLE_NAME + " WHERE "
                        + SelectedArea.COL_ID + " = '" + studentId + "'",
                null);
        cursor.moveToFirst();
        String areaName = "";
        boolean hasOther = false;
        while (!cursor.isAfterLast()) {
            areaName = cursor.getString(cursor
                    .getColumnIndex(SelectedArea.COL_AREA));
            if (!Arrays.asList(list1).contains(areaName) && !Arrays.asList(list2).contains(areaName)) {
                hasOther = true;
                db.execSQL("DELETE FROM " + SelectedArea.TABLE_NAME + " WHERE " + SelectedArea.COL_ID + " = '" + studentId + "' AND " + SelectedArea.COL_AREA + "= '" + areaName + "'");
                db.execSQL("DELETE FROM " + SelectedArea.TABLE_NAME + " WHERE " + SelectedArea.COL_ID + " = 'trial1" + studentId + "' AND " + SelectedArea.COL_AREA + "= '" + areaName + "'");
                db.execSQL("DELETE FROM " + SelectedArea.TABLE_NAME + " WHERE " + SelectedArea.COL_ID + " = 'trial2" + studentId + "' AND " + SelectedArea.COL_AREA + "= '" + areaName + "'");
                //delete area, task, at
                db.execSQL("DELETE FROM " + AreaStore.TABLE_NAME + " WHERE " + AreaStore.COL_STUDENT_ID + " = '" + studentId + "' AND " + AreaStore.COL_AREA_NAME + "= '" + areaName + "'");
                db.execSQL("DELETE FROM " + TaskStore.TABLE_NAME + " WHERE " + TaskStore.COL_STUDENT_ID + " = '" + studentId + "' AND " + TaskStore.COL_AREA_NAME + "= '" + areaName + "'");
                db.execSQL("DELETE FROM " + ATStore.TABLE_NAME + " WHERE " + ATStore.COL_STUDENT_ID + " = '" + studentId + "' AND " + ATStore.COL_AREA_NAME + "= '" + areaName + "'");
                //delete related first trial and second trial data
                db.execSQL("DELETE FROM " + AreaStore.TABLE_NAME + " WHERE " + AreaStore.COL_STUDENT_ID + " = 'trial1" + studentId + "' AND " + AreaStore.COL_AREA_NAME + "= '" + areaName + "'");
                db.execSQL("DELETE FROM " + TaskStore.TABLE_NAME + " WHERE " + TaskStore.COL_STUDENT_ID + " = 'trial1" + studentId + "' AND " + TaskStore.COL_AREA_NAME + "= '" + areaName + "'");
                db.execSQL("DELETE FROM " + ATStore.TABLE_NAME + " WHERE " + ATStore.COL_STUDENT_ID + " = 'trial1" + studentId + "' AND " + ATStore.COL_AREA_NAME + "= '" + areaName + "'");
                db.execSQL("DELETE FROM " + AreaStore.TABLE_NAME + " WHERE " + AreaStore.COL_STUDENT_ID + " = 'trial2" + studentId + "' AND " + AreaStore.COL_AREA_NAME + "= '" + areaName + "'");
                db.execSQL("DELETE FROM " + TaskStore.TABLE_NAME + " WHERE " + TaskStore.COL_STUDENT_ID + " = 'trial2" + studentId + "' AND " + TaskStore.COL_AREA_NAME + "= '" + areaName + "'");
                db.execSQL("DELETE FROM " + ATStore.TABLE_NAME + " WHERE " + ATStore.COL_STUDENT_ID + " = 'trial2" + studentId + "' AND " + ATStore.COL_AREA_NAME + "= '" + areaName + "'");
                break;
            }
            cursor.moveToNext();
        }
        db.close();
        if (hasOther) {
            return areaName;
        } else {
            return "";
        }
    }

    //delete this record when task solution is yes
    public static void deleteSolutionRecords(String studentId1, String studentId2, ArrayList<Area> arealist, Context context) {
        SQLiteDbHelper mDbHelper = new SQLiteDbHelper(context);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        for (Area area : arealist) {
            for (Task task : area.getTasks()) {
                if (task.solutions) {
                    db.execSQL("DELETE FROM " + ATStore.TABLE_NAME + " WHERE " + ATStore.COL_STUDENT_ID
                            + " = '" + studentId1 + "' AND " + ATStore.COL_AREA_ID + "= '" + area.getParentId() + "' AND "
                            + ATStore.COL_TASK_ID + "= '" + task.taskid + "'");
                    db.execSQL("DELETE FROM " + ATStore.TABLE_NAME + " WHERE " + ATStore.COL_STUDENT_ID +
                            " = '" + studentId2 + "' AND " + ATStore.COL_AREA_ID + "= '" + area.getParentId() + "' AND "
                            + ATStore.COL_TASK_ID + "= '" + task.taskid + "'");
                }
            }
        }
        db.close();
    }

    /**
     * get the Area objects from the database, use the student id
     *
     * @param studentid
     * @param context
     * @return ArrayList Area list of Area objects
     */
    public static ArrayList<Area> getPersistedAreaObjects(String studentid,
                                                          Context context) {
        ArrayList<Area> areaObjList = new ArrayList<Area>();
        // get all area objects

        // For each task: persist(Task);
        {

            try {
                Log.d("ATGUIDE", "Getting Persisted area objects student id: "
                        + studentid);
                SQLiteDbHelper mDbHelper = new SQLiteDbHelper(context);
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                // db.execSQL("DELETE FROM "+TaskStore.TABLE_NAME+" WHERE "+TaskStore.COL_STUDENT_ID+" = '"+studentid+"' AND "+TaskStore.COL_AREA_ID+"= '"+parentid+"'");
                Cursor cursor = db.rawQuery("SELECT * FROM "
                                + AreaStore.TABLE_NAME + " WHERE "
                                + AreaStore.COL_STUDENT_ID + "= '" + studentid + "'",
                        null);
                cursor.moveToFirst();
                Log.d("ATGUIDE",
                        "count of records for area: " + cursor.getCount());
                while (!cursor.isAfterLast()) {
                    // For each area
                    Area area = new Area();
                    area.setAreaName(cursor.getString(cursor
                            .getColumnIndex(AreaStore.COL_AREA_NAME)));
                    area.setParentId(Integer.parseInt(cursor.getString(cursor
                            .getColumnIndex(AreaStore.COL_AREA_ID))));
                    area.tasks = getPersistedTasks(area, studentid, context);
                    areaObjList.add(area);
                    cursor.moveToNext();
                }
                db.close();
            } catch (Exception e) {
                // Log.e()
            }
        }
        return areaObjList;
    }

    /**
     * To return all the persisted tasks that belong to particular area
     *
     * @param area
     * @param studentid
     * @param context
     * @return list of persisted tasks
     */
    public static ArrayList<Task> getPersistedTasks(Area area,
                                                    String studentid, Context context) {
        ArrayList<Task> tasks = new ArrayList<Task>();
        try {
            Log.d("ATGUIDE", "task store");
            SQLiteDbHelper mDbHelper = new SQLiteDbHelper(context);
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            // db.execSQL("DELETE FROM "+TaskStore.TABLE_NAME+" WHERE "+TaskStore.COL_STUDENT_ID+" = '"+studentid+"' AND "+TaskStore.COL_AREA_ID+"= '"+parentid+"'");
            String sql = "SELECT * FROM " + TaskStore.TABLE_NAME + " WHERE "
                    + TaskStore.COL_STUDENT_ID + " = '" + studentid + "' AND "
                    + TaskStore.COL_AREA_ID + " = '" + area.getParentId() + "'";
            Cursor cursor = db.rawQuery(sql, null);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                // For each area
                Task currentTask = new Task();
                currentTask.areaname = area.getAreaName();
                currentTask.taskname = cursor.getString(cursor
                        .getColumnIndex(TaskStore.COL_TASK_NAME));
                currentTask.taskid = Integer.parseInt(cursor.getString(cursor
                        .getColumnIndex(TaskStore.COL_TASK_ID)));
                if ((cursor.getString(cursor
                        .getColumnIndex(TaskStore.COL_SOLUTION)))
                        .contains("yes"))
                    currentTask.solutions = true;
                else
                    currentTask.solutions = false;
                currentTask.strategies = getPersistedStrategiesMap(
                        currentTask.taskid + "", studentid, context);
                currentTask.ats = getPersistedATs(area, currentTask, studentid,
                        context);

                currentTask.trial1solutions =
                        Boolean.parseBoolean(cursor
                                .getString(cursor
                                        .getColumnIndex(TaskStore.COL_SOLUTION_T1)));
                currentTask.trial2solutions =
                        Boolean.parseBoolean(cursor
                                .getString(cursor
                                        .getColumnIndex(TaskStore.COL_SOLUTION_T2)));

                tasks.add(currentTask);
                cursor.moveToNext();
            }
            db.close();
            Log.d("ATGUIDE", "task store retrieve successful");
        } catch (Exception e) {
            Log.e("ATGUIDE", "Task Store Error on retrieval " + e);
        }
        Log.d("ATGUIDE", "Tasks size: " + tasks.size());
        return tasks;

    }

    /**
     * To return all the persisted tasks that belong to particular area
     *
     * @param area
     * @param studentid
     * @param context
     * @return list of persisted tasks
     */
    public static ArrayList<AT> getPersistedATs(Area area, Task task,
                                                String studentid, Context context) {
        ArrayList<AT> atList = new ArrayList<AT>();
        try {
            Log.d("ATGUIDE", "task store");
            SQLiteDbHelper mDbHelper = new SQLiteDbHelper(context);
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            // db.execSQL("DELETE FROM "+TaskStore.TABLE_NAME+" WHERE "+TaskStore.COL_STUDENT_ID+" = '"+studentid+"' AND "+TaskStore.COL_AREA_ID+"= '"+parentid+"'");
            String sql = "SELECT * FROM " + ATStore.TABLE_NAME + " WHERE "
                    + ATStore.COL_STUDENT_ID + " = '" + studentid + "' AND "
                    + ATStore.COL_AREA_ID + " = '" + area.getParentId()
                    + "' AND " + ATStore.COL_TASK_ID + " = '" + task.taskid
                    + "' ";
            Cursor cursor = db.rawQuery(sql, null);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                // For each AT
                AT currentAT = new AT();
                currentAT.ATName = area.getAreaName();
                currentAT.instructionalArea = cursor.getString(cursor
                        .getColumnIndex(ATStore.COL_AREA_NAME));
                currentAT.id = cursor.getInt(cursor
                        .getColumnIndex(ATStore.COL_AT_ID));

                currentAT.setATName(cursor.getString(cursor
                        .getColumnIndex(ATStore.COL_ATNAME)));

                currentAT.task = cursor.getString(cursor
                        .getColumnIndex(ATStore.COL_TASK_NAME));

                currentAT.participants = cursor.getString(cursor
                        .getColumnIndex(ATStore.COL_PARTICIPANTS));

                currentAT.firstTrialDate = cursor.getString(cursor
                        .getColumnIndex(ATStore.COL_1stTrialDate));

                currentAT.firstWorking = Boolean.parseBoolean(cursor
                        .getString(cursor
                                .getColumnIndex(ATStore.COL_1stTrialWorking)));

                currentAT.solutionWorking = cursor
                        .getString(cursor
                                .getColumnIndex(ATStore.COL_SOLUTION));

                currentAT.trial1CompletionDate = cursor.getString(cursor
                        .getColumnIndex(ATStore.COL_TRIAL1_COMPLETION));
                currentAT.trial1Action = cursor.getString(cursor
                        .getColumnIndex(ATStore.COL_TRIAL1_Action));
                currentAT.trial1Persons = cursor.getString(cursor
                        .getColumnIndex(ATStore.COL_TRIAL1_Persons));

                currentAT.secondTrialDate = cursor.getString(cursor
                        .getColumnIndex(ATStore.COL_2ndTrialDate));

                currentAT.trial2CompletionDate = cursor.getString(cursor
                        .getColumnIndex(ATStore.COL_TRIAL2_COMPLETION));
                currentAT.trial2Action = cursor.getString(cursor
                        .getColumnIndex(ATStore.COL_TRIAL2_Action));
                currentAT.trial2Persons = cursor.getString(cursor
                        .getColumnIndex(ATStore.COL_TRIAL2_Persons));
                currentAT.trial2Participants = cursor.getString(cursor
                        .getColumnIndex(ATStore.COL_PARTICIPANTS_T2));

                currentAT.trial2solutionWorking = Boolean.parseBoolean(cursor
                        .getString(cursor
                                .getColumnIndex(ATStore.COL_SOLUTION_T1)));


                atList.add(currentAT);
                cursor.moveToNext();
            }
            db.close();
            Log.d("ATGUIDE", "AT retrieve successful");
        } catch (Exception e) {
            Log.e("ATGUIDE", "AT Error on retrieval " + e);
        }
        return atList;
    }

    public static HashMap<String, String> getPersistedStrategiesMap(
            String taskid, String studentid, Context context) {
        HashMap<String, String> strategyMap = new HashMap<String, String>();
        try {
            SQLiteDbHelper mDbHelper = new SQLiteDbHelper(context);
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            String sql = "SELECT * FROM " + StrategyStore.TABLE_NAME
                    + " WHERE " + "" + StrategyStore.COL_STUDENT_ID + " = '"
                    + studentid + "' AND " + StrategyStore.COL_TASKID + " = '"
                    + taskid + "'";
            Cursor cursor = db.rawQuery(sql, null);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                String key = cursor.getString(cursor
                        .getColumnIndex(StrategyStore.COL_STRATEGY_ID));
                String strategyText = cursor.getString(cursor
                        .getColumnIndex(StrategyStore.COL_STRATEGY_TEXT));
                strategyMap.put(key, strategyText);
                cursor.moveToNext();
            }
            db.close();
        } catch (Exception e) {
            Log.e("ATGUIDE", "Exception getting strategies " + e);
        }
        return strategyMap;
    }

    /**
     * Get List of students in the DB
     *
     * @param context
     * @return List of students
     */
    public static ArrayList<StudentBean> getStudentList(Context context) {
        ArrayList<StudentBean> studentList = new ArrayList<StudentBean>();
        SQLiteDbHelper mDbHelper = new SQLiteDbHelper(context);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        try {
            String sql = "SELECT *" /*+ IntentStore.COLUMN_NAME_ID*/
                    + " FROM " + IntentStore.TABLE_NAME;
            Cursor cursor = db.rawQuery(sql, null);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                StudentBean studentBean = new StudentBean();
                studentBean.setStudentid(cursor.getString(cursor
                        .getColumnIndex(IntentStore.COLUMN_NAME_ID)));
                Intent intent = Intent.parseUri(cursor.getString(cursor
                        .getColumnIndex(IntentStore.COLUMN_NAME_INTENT)), 0);
                studentBean.setRecordId(intent.getStringExtra("studentidrecord"));
                studentList.add(studentBean);
                cursor.moveToNext();
            }
        } catch (SQLException sqlE) {
            Log.e("ATGUIDE", "Exception in db query " + sqlE);
        } catch (Exception unknown) {
            Log.e("ATGUIDE", "Unknown exception in retriveing all student id "
                    + unknown);
        }
        db.close();

        // studentList.add(object)
        return studentList;
    }

    public static void deleteNavigatorRecord(Context context, String stuId, String areaName) {
        SQLiteDbHelper mDbHelper = new SQLiteDbHelper(context);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.execSQL("DELETE FROM " + SelectedArea.TABLE_NAME + " WHERE " + SelectedArea.COL_ID +
                " = '" + stuId + "' AND " + SelectedArea.COL_AREA + " = '" + areaName + "'");
        db.execSQL("DELETE FROM " + AreaStore.TABLE_NAME + " WHERE " + AreaStore.COL_STUDENT_ID +
                " = '" + stuId + "' AND " + AreaStore.COL_AREA_NAME + " = '" + areaName + "'");
        db.execSQL("DELETE FROM " + AreaStore.TABLE_NAME + " WHERE " + AreaStore.COL_STUDENT_ID +
                " = 'trial1" + stuId + "' AND " + AreaStore.COL_AREA_NAME + " = '" + areaName + "'");
        db.execSQL("DELETE FROM " + AreaStore.TABLE_NAME + " WHERE " + AreaStore.COL_STUDENT_ID +
                " = 'trial2" + stuId + "' AND " + AreaStore.COL_AREA_NAME + " = '" + areaName + "'");

        db.execSQL("DELETE FROM " + TaskStore.TABLE_NAME + " WHERE " + TaskStore.COL_STUDENT_ID +
                " = '" + stuId + "' AND " + TaskStore.COL_AREA_NAME + " = '" + areaName + "'");
        db.execSQL("DELETE FROM " + TaskStore.TABLE_NAME + " WHERE " + TaskStore.COL_STUDENT_ID +
                " = 'trial1" + stuId + "' AND " + TaskStore.COL_AREA_NAME + " = '" + areaName + "'");
        db.execSQL("DELETE FROM " + TaskStore.TABLE_NAME + " WHERE " + TaskStore.COL_STUDENT_ID +
                " = 'trial2" + stuId + "' AND " + TaskStore.COL_AREA_NAME + " = '" + areaName + "'");
        db.close();
    }

    public static void deleteStudent(String id, Context context) {
        SQLiteDbHelper mDbHelper = new SQLiteDbHelper(context);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        try {
            db.execSQL("DELETE FROM " + IntentStore.TABLE_NAME + " WHERE "
                    + IntentStore.COLUMN_NAME_ID + " = '" + id + "'");
            db.execSQL("DELETE FROM " + AreaStore.TABLE_NAME + " WHERE " + AreaStore.COL_STUDENT_ID + " = '" + id + "'");
            db.execSQL("DELETE FROM " + TaskStore.TABLE_NAME + " WHERE " + TaskStore.COL_STUDENT_ID + " = '" + id + "'");
            db.execSQL("DELETE FROM " + ATStore.TABLE_NAME + " WHERE " + ATStore.COL_STUDENT_ID + " = '" + id + "'");
            //delete related first trial and second trial data
            db.execSQL("DELETE FROM " + AreaStore.TABLE_NAME + " WHERE " + AreaStore.COL_STUDENT_ID + " = 'trial1" + id + "'");
            db.execSQL("DELETE FROM " + TaskStore.TABLE_NAME + " WHERE " + TaskStore.COL_STUDENT_ID + " = 'trial1" + id + "'");
            db.execSQL("DELETE FROM " + ATStore.TABLE_NAME + " WHERE " + ATStore.COL_STUDENT_ID + " = 'trial1" + id + "'");
            db.execSQL("DELETE FROM " + AreaStore.TABLE_NAME + " WHERE " + AreaStore.COL_STUDENT_ID + " = 'trial2" + id + "'");
            db.execSQL("DELETE FROM " + TaskStore.TABLE_NAME + " WHERE " + TaskStore.COL_STUDENT_ID + " = 'trial2" + id + "'");
            db.execSQL("DELETE FROM " + ATStore.TABLE_NAME + " WHERE " + ATStore.COL_STUDENT_ID + " = 'trial2" + id + "'");
            db.execSQL("DELETE FROM " + StrategyStore.TABLE_NAME + " WHERE " + StrategyStore.COL_STUDENT_ID + " = '" + id + "'");
            db.execSQL("DELETE FROM " + StrategyStore.TABLE_NAME + " WHERE " + StrategyStore.COL_STUDENT_ID + " = 'trial1" + id + "'");
            db.execSQL("DELETE FROM " + StrategyStore.TABLE_NAME + " WHERE " + StrategyStore.COL_STUDENT_ID + " = 'trial2" + id + "'");
            db.execSQL("DELETE FROM " + SelectedArea.TABLE_NAME + " WHERE " + SelectedArea.COL_ID + " = '" + id + "'");
            db.execSQL("DELETE FROM " + SelectedArea.TABLE_NAME + " WHERE " + SelectedArea.COL_ID + " = 'trial1" + id + "'");
            db.execSQL("DELETE FROM " + SelectedArea.TABLE_NAME + " WHERE " + SelectedArea.COL_ID + " = 'trial2" + id + "'");
            db.execSQL("DELETE FROM " + SQLiteContract.TrialStore.TABLE_NAME + " WHERE " + SQLiteContract.TrialStore.COL_STUDENT_ID + " = '" + id + "'");
            db.execSQL("DELETE FROM CURRENT_DATA");
        } catch (SQLException sqlE) {
            Log.e("ATGUIDE", "Exception in db query " + sqlE);
        } catch (Exception unknown) {
            Log.e("ATGUIDE", "Unknown exception in retriveing all student id "
                    + unknown);
        } finally {
            db.close();
        }
    }

    public static boolean checkSample(Context context) {
        SQLiteDbHelper mDbHelper = new SQLiteDbHelper(context);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        String sql = "SELECT * FROM " + IntentStore.TABLE_NAME + " WHERE " + IntentStore.COLUMN_NAME_ID + " = 'AA12320170131065145'";
        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {
            db.close();
            return true;
        } else {
            db.close();
            return false;
        }
    }

    /**
     * insert sample record when creating the database
     */
    public static void insertSample(Context context) {
        ArrayList<String> selectedInstructionalAreas = new ArrayList<String>();
        ArrayList<Area> trial1List = new ArrayList<>();
        ArrayList<String> trial1TextList = new ArrayList<>();
        ArrayList<Area> areaList = new ArrayList<>();
        selectedInstructionalAreas.add("Reading");
        selectedInstructionalAreas.add("Spelling");
        selectedInstructionalAreas.add("Writing");

        Area areaObj1 = new Area("Reading");
        areaObj1.setParentId(1001);
        Area areaObj2 = new Area("Spelling");
        areaObj2.setParentId(1002);
        Area areaObj3 = new Area("Writing");
        areaObj3.setParentId(1003);
        Area navigator = new Area("AEM Navigator");
        navigator.setParentId(1004);
        //reading
        Task task1 = new Task();
        task1.setAreaname(areaObj1.getAreaName());
        task1.setTaskname("Applying word analysis skills when reading");
        task1.taskid = 3010;
        task1.solutions = false;
        task1.strategies.put("0", "Verbal contextual analysis: reading aloud by others, gives clues");
        task1.strategies.put("1", "Basic word identification strategies: say the stem of the word, and dictionary use.");
        AT at2 = new AT();
        at2.id = 1001;
        at2.setATName("Access to digitally formatted textbooks");
        at2.participants = "Mrs.Carl, SPED Teacher";
        at2.setFirstTrialDate("28/1/2017");
        at2.setInstructionalArea(areaObj1.getAreaName());
        at2.solutionWorking = "YES";
        at2.trial1Action = "Student able to access digital books; training for family and teachers needs to be completed";
        at2.trial1Persons = "Mrs. Carl, SPED Teacher";
        at2.trial1CompletionDate = "28/2/2017";
        task1.setTrial1solutions(true);
        task1.ats.add(at2);
        areaObj1.addTask(task1);
        //spelling
        Task task2 = new Task();
        task2.setAreaname(areaObj2.getAreaName());
        task2.setTaskname("Spelling of high frequency sight words");
        task2.taskid = 3012;
        task2.solutions = true;
        task2.strategies.put("0", "Personal dictionary, reduced number of spelling words.");
        areaObj2.addTask(task2);
        //writing
        Task task3 = new Task();
        task3.setAreaname(areaObj3.getAreaName());
        task3.setTaskname("Writing a short paragraph of 2-3 sentences on a topic area");
        task3.taskid = 3014;
        task3.solutions = false;
        task3.strategies.put("0", "Sentence starters, word bank, extra time");
        AT at3 = new AT();
        at3.id = 1002;
        at3.setATName("Graphic organizer software");
        at3.participants = "Mrs.Sheets, General Ed.Teacher";
        at3.setFirstTrialDate("12/2/2017");
        at3.setInstructionalArea(areaObj3.getAreaName());
        at3.solutionWorking = "NO";
        at3.trial1Action = "Student would not use graphic organizers - Since Neither AT worked, new trial: using speech to text software";
        at3.trial1Persons = "All Teachers";
        at3.trial1CompletionDate = "18/3/2017";
        AT at4 = new AT();
        at4.id = 1003;
        at4.setATName("Word prediction with auditory scanning");
        at4.participants = "Mrs.Sheets, General Ed.Teacher";
        at4.setFirstTrialDate("12/2/2017");
        at4.setInstructionalArea(areaObj3.getAreaName());
        at4.solutionWorking = "NO";
        at4.trial1Action = "Word prediction software not successful";
        at4.trial1Persons = "Mrs. Sheets, General Ed Teacher";
        at4.trial1CompletionDate = "28/2/2017";
        task3.setTrial1solutions(false);
        task3.ats.add(at3);
        task3.ats.add(at4);
        areaObj3.addTask(task3);
        //navigator area
        Task exploring = new Task();
        exploring.setTaskname("VA Explorer");
        exploring.setAreaname(navigator.getAreaName());
        exploring.taskid = 3016;
        exploring.solutions = false;
        //navigator at service
        AT at1 = new AT();
        at1.id = 1004;
        at1.setATName("AT service data from AEM Navigator app.");
        at1.participants = "Vicky";
        at1.setInstructionalArea(navigator.getAreaName());
        at1.setFirstTrialDate("07/1/2017");
        at1.solutionWorking = "YES";
        exploring.setTrial1solutions(true);
        exploring.ats.add(at1);
        navigator.addTask(exploring);

        //add area objects to list
        areaList.add(areaObj1);
        areaList.add(areaObj2);
        areaList.add(areaObj3);
        areaList.add(navigator);
        //only reading and writing need further solutions
		/*trial1List.add(areaObj1);
		trial1List.add(areaObj3);
		trial1TextList.add(areaObj1.getAreaName());
		trial1TextList.add(areaObj3.getAreaName());*/
        String studentid = "AA123";
        String studentschool = "Port Bell Elementary School";
        String studentparticipant = "Mr.Johns, Parent; Miss Sheets, GeneralEducationTeacher; Mrs. Monterey, Title I Teacher, Mrs. Carl,"
                + "SPED Teacher; Mr. Dean, Administrator";
        String studentgrade = "5th Grade";
        int day = 31;
        int month = 0;
        int year = 2017;
        String date = month + "-" + day + "-" + year;
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");// HH:mm:ss
        Date realDate = new Date(System.currentTimeMillis());
        Intent saveIntent = new Intent();
        saveIntent.putExtra("studentid", studentid + "20170131065145");
        saveIntent.putExtra("studentidrecord", studentid);
        saveIntent.putExtra("studentgrade", studentgrade);
        saveIntent.putExtra("studentparticipant", studentparticipant);
        saveIntent.putExtra("studentschool", studentschool);
        saveIntent.putExtra("date", date);
        saveIntent.putExtra("day", day);
        saveIntent.putExtra("month", month);
        saveIntent.putExtra("year", year);
        saveIntent.putExtra("iepgoal", "YES");
        saveIntent.putExtra("sample", true);
        saveIntent.putStringArrayListExtra("selectedareas",
                selectedInstructionalAreas);
        saveIntent.putExtra("iepreading", "YES");
        saveIntent.putExtra("iepaltradio", "YES");
        saveIntent.putExtra("iepalt", "yes");
        saveIntent.putExtra("eligibility", "yes");
        saveIntent.putExtra("trial1", true);
        PersistenceBean.persistIntent(saveIntent.getStringExtra("studentid"), saveIntent, context);
        PersistenceBean.persistCurrentId(saveIntent.getStringExtra("studentid"), context);
        PersistenceBean.persistInstructionalAreas(saveIntent.getStringExtra("studentid"), selectedInstructionalAreas, context);
        PersistenceBean.persistInstructionalAreas("trial1" + saveIntent.getStringExtra("studentid"), selectedInstructionalAreas, context);
        PersistenceBean.persistAreaObject(areaList, saveIntent.getStringExtra("studentid"), context);
        PersistenceBean.persistAreaObject(areaList, "trial1" + saveIntent.getStringExtra("studentid"), context);
//		PersistenceBean.persistAreaObject(trial1List, "trial1" + saveIntent.getStringExtra("studentid"), context);
//		PersistenceBean.persistInstructionalAreas("trial1" + saveIntent.getStringExtra("studentid"),
//				trial1TextList, context);
        PersistenceBean.persistTrial(saveIntent.getStringExtra("studentid"), context, 1);
    }
}
