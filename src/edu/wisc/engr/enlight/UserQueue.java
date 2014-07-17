package edu.wisc.engr.enlight;

import java.util.ArrayList;
import java.util.Queue;

public class UserQueue {
	Queue<UserEntry> uQueue;
	ArrayList<UserEntry> list;
	int deviceID = -1; //device id (should be acquired from server)
	int devicePos = -1; //Position within the queue with priority prio (not entire queue)
	int devicePrio = -1; //Priority of user (should be acquired from server)
	int timePerUser = 15; //seconds that a single user has control
	
	public UserQueue(){
		list = new ArrayList<UserEntry>();
	}
	
	public UserQueue(int id, int pos, int prio){
		list = new ArrayList<UserEntry>();
		this.deviceID = id;
		this.devicePos = pos;
		this.devicePrio = prio;
	}
	public void addUser(UserEntry toAdd){
		list.add(toAdd);
	}
	
	public void clearList(){
		list.clear();
	}
	
	public void removeUser(int userID){
		for (int i = 0; i < list.size(); i++){
			if (list.get(i).id == userID){
				list.remove(i);
			}
		}
	}
	
	public UserEntry getUserAt(int pos){
		return list.get(pos);
	}
	
	public int getUserPosition(){
		int pos = 0;
		if (devicePos == -1){
			//The user isn't in the queue
			return -1;
		}
		for (int i = 0; i < list.size(); i++){
			UserEntry curr = list.get(i);
			//Go through the list. Anything with a higher priority or a lower 
			//position is before our user
			if(curr.priority > devicePrio || (curr.priority == devicePrio && curr.position < devicePos)){
				pos++;
			}
		}
		return pos;
	}
	
	/**
	 * See if our user is currently in control by finding the device currently
	 * at the front of the queue
	 * @return
	 */
	public boolean hasControl(){
		for (int i = 0; i < list.size(); i++){
			if (list.get(i).position == 0){
				return list.get(i).id == deviceID;
			}
		}
		return false;
	}
	
	/**
	 * Calculates the time (in seconds) until the user has control
	 * @return Returns the time in seconds until the user has control.
	 */
	public int timeUntilControl(){
		return getUserPosition() * timePerUser;
	}
}
