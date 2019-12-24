package com.hust.opt.optapp.teacherclassassignment.model;

import java.util.ArrayList;

public class Session {
    private int index;
    private int startTime;
    private int endTime;
    private ArrayList<Integer> weeks;
    private String location;

    
    public Session clone(){
    	ArrayList<Integer> W = new ArrayList<Integer>();
    	for(int i : weeks) W.add(i);
    	return new Session(index, startTime, endTime, W, location);
    }
	public Session() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
     * Constructor
     *
     * @param index
     * @param startTime
     * @param endTime
     * @param weeks
     * @param location
     */
    public Session(int index, int startTime, int endTime, ArrayList<Integer> weeks, String location) {
        super();
        this.index = index;
        this.startTime = startTime;
        this.endTime = endTime;
        this.weeks = weeks;
        this.setLocation(location);
    }

    /**
     * Check conflict between 2 session
     *
     * @param session
     * @return
     */
    public boolean isConflict(Session session) {
        boolean flag = false;

        for (int i : this.weeks) {
            for (int j : session.weeks) {
                if (i == j) {
                    flag = true;
                    break;
                }
            }
            if (flag) break;
        }

        if (flag) {
            if (this.getStartTime() == session.getStartTime()
                    && this.getEndTime() == session.getEndTime()) {
                return true;
            }

            if (isInRange(this.getStartTime(),
                    session.getStartTime(), session.getEndTime())) {
                return true;
            }
            if (isInRange(this.getEndTime(),
                    session.getStartTime(), session.getEndTime())) {
                return true;
            }
            if (isInRange(session.getStartTime(),
                    this.getStartTime(), this.getEndTime())) {
                return true;
            }
            if (isInRange(session.getEndTime(),
                    this.getStartTime(), this.getEndTime())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check point p in range (s, e)
     *
     * @param p
     * @param s
     * @param e
     * @return
     */
    public boolean isInRange(int p, int s, int e) {
        if (p > s && p < e) {
            return true;
        } else {
            return false;
        }
    }


    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public ArrayList<Integer> getWeeks() {
        return weeks;
    }

    public void setWeeks(ArrayList<Integer> arrayList) {
        this.weeks = arrayList;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Overwrite toString() method
     */
    public String toString() {
        String weeks_str = "";

        if (this.getWeeks() != null) {
            for (int week : this.getWeeks()) {
                weeks_str += week + ", ";
            }
        }

        return "" + this.getIndex() + ", " + this.getStartTime() + ", "
                + this.getEndTime() + ", " + weeks_str + this.getLocation();
    }
}
