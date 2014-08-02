package edu.wisc.engr.enlight;

/**
 * Simple class to contain a user entry
 * @author Sam
 *
 */
public class UserEntry {
	public int id;
	public int acquired;
	public int expires;
	public int priority;
	public int position;
	
	public UserEntry(int id, int acquired, int expires, int priority, int qPosition){
		this.id = id;
		this.acquired = acquired;
		this.expires = expires;
		this.priority = priority;
		this.position = qPosition;
	}
}
