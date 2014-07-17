package edu.wisc.engr.enlight;

/**
 * Simple class to contain a fountain valve pattern.
 * @author Sam
 *
 */
public class Pattern {
	public int id;
	public String name;
	public boolean active;
	
	public Pattern(int id, String name, boolean active){
		this.id = id;
		this.name = name;
		this.active = active;
	}
}
