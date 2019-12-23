package com.hust.opt.optapp.teacherclassassignment.service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import localsearch.constraints.basic.NotEqual;
import localsearch.model.ConstraintSystem;
import localsearch.model.LocalSearchManager;
import localsearch.model.VarIntLS;
import localsearch.search.TabuSearch;



public class OpenCBLSSolver {
	private int N;// number of classes
	private int M;// number of teachers;
	private int[] credit; // credit[i] is the number of credits of class i
	private ArrayList<Integer>[] D;//D[i] is the list of teachers for class i
	private ArrayList<Integer>[] P;// P[i] is the list of corresponding priority for teacher
	private int[][] conflict;// (conflict[k][0], conflict[k][1]) is a pair of conflicting classes (cannot assigned to the same teacher)
	
	// modelling
	LocalSearchManager mgr;
	VarIntLS[] X;
	ConstraintSystem S;
	
	public void loadData(String filename){
		try{
			Scanner in = new Scanner(new File(filename));
			N = in.nextInt(); M = in.nextInt();
			D = new ArrayList[N];
			P = new ArrayList[N];
			credit = new int[N];
			for(int i= 0; i < N; i++){
				D[i] = new ArrayList<Integer>();
				P[i] = new ArrayList<Integer>();
			}
			for(int i = 0; i < N; i++){
				int id = in.nextInt();
				credit[id] = in.nextInt();
				int k = in.nextInt();
				for(int j = 0; j < k; j++){
					int t = in.nextInt();
					int p = in.nextInt();
					D[id].add(t);
					P[id].add(p);
				}
			}
			int K = in.nextInt();
			conflict = new int[K][2];
			for(int k = 0; k < K; k++){
				conflict[k][0] = in.nextInt();
				conflict[k][1] = in.nextInt();
			}
			in.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public void printSolution(){
		int totalCourses = 0;
		int totalCredits = 0;
		int maxCourses = 0;
		int minCourses = Integer.MAX_VALUE;
		int maxCredits = 0;
		int minCredits = Integer.MAX_VALUE;
		for(int t = 0; t < M; t++){
			int nbCourses = 0;
			int nbCredits = 0;
			System.out.print("teacher " + t + ": ");
			for(int i = 0; i < N; i++) if(X[i].getValue() == t){
				System.out.print(i + " ");
				nbCourses++;
				nbCredits += credit[i];
				totalCourses ++;
				totalCredits += credit[i];
			}
			if(maxCourses < nbCourses) maxCourses = nbCourses;
			if(minCourses > nbCourses) minCourses = nbCourses;
			if(maxCredits < nbCredits) maxCredits = nbCredits;
			if(minCredits > nbCredits) minCredits = nbCredits;
			System.out.println(", nbCOurses = " + nbCourses + ", nbCredits = " + nbCredits);
			
			
		}
		System.out.println("nbCourses = " + totalCourses + ", nbTeachers = " + M + ", totalCredits = " + totalCredits +
				", maxCourse = " + maxCourses + ", minCourses = " + minCourses + ", maxCredits = " + maxCredits + ", minCredits = " + minCredits);
		for(int i = 0; i < N; i++){
			System.out.println("X[" + i + "] = " + X[i].getValue());
		}
	}
	public void stateModel(){
		mgr = new LocalSearchManager();
		X = new VarIntLS[N];
		for(int i = 0; i < N; i++){
			Set<Integer> Di = new HashSet<Integer>();
			for(int v: D[i]) Di.add(v);
			X[i] = new VarIntLS(mgr,Di);
		}
		S = new ConstraintSystem(mgr);
		for(int[] t : conflict){
			S.post(new NotEqual(X[t[0]], X[t[1]]));
		}
		
		mgr.close();
	}
	public void search(){
		TabuSearch ts = new TabuSearch();
		ts.search(S, 50, 10000, 10000, 100);
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		OpenCBLSSolver s = new OpenCBLSSolver();
		//s.loadData("data/input_20182_1905.txt");
		s.loadData("data/input_20182_1905_ext_4.txt");
		s.stateModel();
		s.search();
		s.printSolution();
	}

}
