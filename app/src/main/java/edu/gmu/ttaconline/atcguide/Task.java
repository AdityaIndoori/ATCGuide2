package edu.gmu.ttaconline.atcguide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * Bean to store single task from a particular area 
 * */
public class Task {
	//Overview: 
	//Bean to store single task from a particular area
	//Instances
	String taskname;
	int taskid;
	boolean solutions=false;
	//whether first solution will work
	boolean trial1solutions=true;
	//whether second solution will work
	boolean trial2solutions=true;

	String areaname;
	Map<String,String> strategies= new HashMap<String,String>();
	public List<String> strategyList=new ArrayList<String>();
	static int strategyId;
	public List<AT> ats=new ArrayList<>();
	//Methods
	/**
	 * @return TaskName of this task
	 */
	public String getTaskname() {
		return taskname;
	}
	/**
	 * set the given task name to this task
	 * @param taskname name of the task to be set to this
	 */
	public void setTaskname(String taskname) {
		this.taskname = taskname;
	}
	
	
	public void logATs(){
		
		for(AT at:ats){
			at.log();
		}
	}
	
	public List<AT> getAts() {
		return ats;
	}
	
	public void setTrial1solutions(boolean trial1solutions) {
		this.trial1solutions = trial1solutions;
	}
	
	public void setTrial2solutions(boolean trial2solutions) {
		this.trial2solutions = trial2solutions;
	}
	
	//copy task except ats
	public void copyTask(Task task){
		this.taskid=task.taskid;
		this.solutions=task.solutions;
		this.areaname=task.areaname;
		this.strategies=task.strategies;
		this.strategyList=task.strategyList;
		this.taskname = task.taskname;
		this.trial1solutions = task.trial1solutions;
		this.trial2solutions = task.trial2solutions;
	}
	
	/**
	 * retrieve the area name of this task
	 * @return Area name to which this task belongs
	 */
	public String getAreaname() {
		return areaname;
	}

	/**
	 * Set the area to the task
	 * @param areaname the name of the area to which this task should set to belong
	 */
	public void setAreaname(String areaname) {
		this.areaname = areaname;
	}

	/**
	 * @param id of the at in this task
	 * @return AT with the given id in this task
	 */
	public AT getATById(int id){
		
		for(AT at:ats){
			if(at.id==id)
				return at;
		}
		
		AT at = new AT();
		at.task = taskname;
		at.ATName = "Choose AT";
		at.instructionalArea = areaname;
		at.id = id;
		//setAtToView(at, v);
		ats.add(at);
		return at;
	}
	
	
//	public void addStrategy(String strategy) {
//		strategies.add(strategy);
//	}

}
