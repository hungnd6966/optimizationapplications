package com.hust.opt.optapp.teacherclassassignment.model;

public class Class {
    private String code;
    private int credit;
    private Session[] timeTable;
    private Course course;

    private String departmentCode; // KHMT, KTMT, HTTT, CNPM, TTMMT, TTMT
    private String teacherCode;// GV da duoc phan cong bang tay vi du dung.phamquang@hust.edu.vn --> truong nay de phan tich solution xep tay
    
    
    public Class clone(){
    	Session[] c_timeTable = new Session[timeTable.length];
    	for(int i = 0; i < timeTable.length; i++)
    		c_timeTable[i] = timeTable[i].clone();
    	Course c = course.clone();
    	return new Class(code, credit, c_timeTable, c);
    }
    public String getTimeTableText(){
    	String s= "";
    	for(int i = 0; i < timeTable.length; i++){
    		String w = "";
    		for(int j = 0; j < timeTable[i].getWeeks().size(); j++) w += timeTable[i].getWeeks().get(j) + ",";
    		s = s + "[" + timeTable[i].getStartTime() + "-" + timeTable[i].getEndTime() + w + "] ";
    	}
    	return s;
    }
    public int getCredit() {
        return credit;
    }

    public void setCredit(int credit) {
        this.credit = credit;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Session[] getTimeTable() {
        return timeTable;
    }

    public void setTimeTable(Session[] timeTable) {
        this.timeTable = timeTable;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    @Override
    public String toString() {
        return "" + getCourse().getCode() + " - "
                + getCode() + " - " + getCourse().getName();
    }

	public Class(String code, int credit, Session[] timeTable, Course course) {
		super();
		this.code = code;
		this.credit = credit;
		this.timeTable = timeTable;
		this.course = course;
	}

	public Class() {
		super();
		// TODO Auto-generated constructor stub
	}
    
}
