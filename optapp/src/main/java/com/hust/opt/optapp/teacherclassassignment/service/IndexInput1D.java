package com.hust.opt.optapp.teacherclassassignment.service;

import com.google.gson.Gson;


import com.hust.opt.optapp.teacherclassassignment.model.Class;
import com.hust.opt.optapp.teacherclassassignment.model.*;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;

public class IndexInput1D {
	public static final String DATA_FOLDER = "./data/teacherclassassignment/";
	TeacherClassAssignmentInput input;
    public int numClass;
    public int numTeacher;

    public ArrayList<Integer>[] possibleTeacherForClass;
    public ArrayList<Integer>[] priority;
    public int[][] conflictPairs;
    public int[] creditOfClass;
    public int[] maxCredit;

    public IndexInput1D(TeacherClassAssignmentInput input) {
    	this.input = input;
        this.numClass = input.getClasses().length;
        this.numTeacher = input.getTeachers().length;
        this.convertConflictPairs(input);
        this.convertCreditOfClass(input);
        this.convertMaxCredit(input);
        this.convertPossibleTeacherForClass(input);
    }

    public static void main(String[] args) {
        Gson gson = new Gson();
        TeacherClassAssignmentInput input = null;
        try {
            Reader reader = new FileReader(
                    DATA_FOLDER + "json/input_20182_1905.json");
            input = gson.fromJson(reader, TeacherClassAssignmentInput.class);
            int org = input.getClasses().length;
            input.addMoreClass(0);
            
            int ext = input.getClasses().length;
            
            IndexInput1D indexInput1D = new IndexInput1D(input);
            int p = indexInput1D.getMaxPossiblePriority();
            System.out.println(p);
            
            
            indexInput1D.writeData2PlainText(DATA_FOLDER + "input_20182_1905_ext_0_format1.txt",DATA_FOLDER + "input_20182_1905_master.txt");
            indexInput1D.writeData2PlainTextVerion2(DATA_FOLDER + "input_20182_1905_ext_0_format2.txt",DATA_FOLDER + "input_20182_1905_master.txt");
            System.out.println(org + " - " + ext);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    public void writeData2PlainText(String filename, String masterDataFilename){
    	try{
    		PrintWriter out = new PrintWriter(filename);
    		out.println(this.numClass + " " + this.numTeacher);
    		for(int i = 0; i < this.numClass; i++){
    			out.print(i + " " + creditOfClass[i] + " " + possibleTeacherForClass[i].size() + " ");
    			for(int j = 0; j < possibleTeacherForClass[i].size(); j++){
    				int t = possibleTeacherForClass[i].get(j);
    				int p = priority[i].get(j);
    				out.print(t + " " + p + " ");
    			}
    			out.println();
    		}
    		out.println(this.conflictPairs.length);
    		for (int[] t : this.conflictPairs){
    			out.println(t[0] + " " + t[1]);
    		}
    		out.close();
    		
    		out = new PrintWriter(masterDataFilename);
    		
    		for(int i = 0; i < this.numClass; i++){
    			out.println(i + " " + input.getClasses()[i].getCourse().getName() + " timetable " + input.getClasses()[i].getTimeTableText());
    		}
    		for(int i = 0; i < this.numTeacher; i++){
    			out.println(i + " " + input.getTeachers()[i].getCode());
    		}
    		out.close();
    	}catch(Exception ex){
    		ex.printStackTrace();
    	}
    }
    public void writeData2PlainTextVerion2(String filename, String masterDataFilename){
    	try{
    		PrintWriter out = new PrintWriter(filename);
    		out.println(this.numTeacher + "  " + this.numClass);
    		ArrayList<Integer>[] clsOfTeacher = new ArrayList[numTeacher];
    		
    		for(int  t= 0; t < numTeacher; t++){
    			clsOfTeacher[t] = new ArrayList<Integer>();
    			for(int i = 0; i < numClass; i++){
    				if(possibleTeacherForClass[i].contains(t)) clsOfTeacher[t].add(i);
    			}
    			//out.print("[" + t + "]: ");
    			for(int i: clsOfTeacher[t]) out.print(i + " ");
    			out.println();
    			
    		}
    		for(int i = 0; i < numClass; i++)
    			out.print(creditOfClass[i] + " ");
    		out.println();
    		
    		for(int[] t: conflictPairs){
    			out.print(t[0] + " ");
    		}
    		for(int[] t: conflictPairs){
    			out.print(t[1] + " ");
    		}
    		
    		out.close();
    		
    		
    	}catch(Exception ex){
    		ex.printStackTrace();
    	}
    }

    public void convertPossibleTeacherForClass(TeacherClassAssignmentInput input) {

        this.possibleTeacherForClass = new ArrayList[this.numClass];
        this.priority = new ArrayList[this.numClass];

        for (int i = 0; i < this.numClass; i++) {

            this.possibleTeacherForClass[i] = new ArrayList<Integer>();
            this.priority[i] = new ArrayList<Integer>();

            for (int j = 0; j < this.numTeacher; j++) {

                for (CourseForTeacher courseForTeacher : input.getTeachers()[j].getCourses()) {

                    if (input.getClasses()[i].getCourse().getCode().equals(courseForTeacher.getCode())
                            && input.getClasses()[i].getCourse().getType().equals(courseForTeacher.getType())) {

                        this.possibleTeacherForClass[i].add(j);
                        this.priority[i].add(courseForTeacher.getPriority());
                        break;
                    }
                }
            }
        }
    }

    /**
     * Generate list of conflict pair
     *
     * @param input
     */
    private void convertConflictPairs(TeacherClassAssignmentInput input) {

        ArrayList<Integer[]> temp = new ArrayList<Integer[]>();

        for (int i = 0; i < this.numClass; i++) {

            for (int j = i + 1; j < this.numClass; j++) {

                if (isConflict(input.getClasses()[i], input.getClasses()[j])) {
                    Integer[] pair = new Integer[2];
                    pair[0] = i;
                    pair[1] = j;
                    temp.add(pair);
                }
            }
        }
        this.conflictPairs = new int[temp.size()][2];

        for (int i = 0; i < temp.size(); i++) {
            for (int j = 0; j < 2; j++) {
                this.conflictPairs[i][j] = temp.get(i)[j];
            }
        }
    }

    /**
     * Check time conflict between class1 and class2
     */
    public boolean isConflict(Class class1, Class class2) {
        for (Session session1 : class1.getTimeTable()) {
            for (Session session2 : class2.getTimeTable()) {
                if (session1.isConflict(session2)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @param input
     */
    public void convertCreditOfClass(TeacherClassAssignmentInput input) {
        this.creditOfClass = new int[this.numClass];
        for (int i = 0; i < this.numClass; i++) {
            this.creditOfClass[i] = input.getClasses()[i].getCredit();
        }
    }

    public void convertMaxCredit(TeacherClassAssignmentInput input) {
        this.maxCredit = new int[this.numTeacher];
        for (int i = 0; i < this.numTeacher; i++) {
            this.maxCredit[i] = input.getTeachers()[i].getMaxCredit();
        }
    }

    public int getMaxPossiblePriority() {
        int p = 0;

        for (int i = 0; i < this.numClass; i ++) {
            p += Collections.max(this.priority[i]);
        }

        return p;
    }
}
