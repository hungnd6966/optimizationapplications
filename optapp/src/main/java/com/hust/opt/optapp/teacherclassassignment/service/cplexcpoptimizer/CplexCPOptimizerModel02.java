package com.hust.opt.optapp.teacherclassassignment.service.cplexcpoptimizer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.cp.IloCP;

public class CplexCPOptimizerModel02 {
	
	private IloCP cp;
	private IloIntVar[] x;
	private IloIntVar[] nbCredits;
	private IloIntVar[] priority;
	private IloIntVar obj1; // maxAssign
	private IloIntVar obj2; // deltaAssign
	private IloIntVar obj3; // priority
	
	private int N;// number of classes
	private int M;// number of teachers;
	private int[] credit; // credit[i] is the number of credits of class i
	private ArrayList<Integer>[] D;//D[i] is the list of teachers for class i
	private ArrayList<Integer>[] P;// P[i] is the list of corresponding priority for teacher
	private int[][] conflict;// (conflict[k][0], conflict[k][1]) is a pair of conflicting classes (cannot assigned to the same teacher)
	private final int MAX_ASSIGNMENT = 30;
	private final int EXPECTED = 13;
	
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
	
	public void stateModel() throws IloException {
		cp = new IloCP();
		x = cp.intVarArray(N);
		
		for (int i=0; i<N; i++) {
			int[] domain = new int[D[i].size()];
			for (int j=0; j<domain.length; j++) {
				domain[j] = D[i].get(j).intValue();
			}
			x[i] = cp.intVar(domain);
		}
		
		int size = conflict.length;
		for (int i=0; i<size; i++) {
			int c1 = conflict[i][0];
			int c2 = conflict[i][1];
			cp.add(cp.neq(x[c1], x[c2]));
		}
		
		nbCredits = cp.intVarArray(M, 0, MAX_ASSIGNMENT);
		cp.add(cp.pack(nbCredits, x, credit));
		obj1 = cp.intVar(0, MAX_ASSIGNMENT);
		cp.addEq(obj1, cp.max(nbCredits));
		
		int tmp = Math.max(EXPECTED, MAX_ASSIGNMENT-EXPECTED);
		IloIntVar[] sqDelta = cp.intVarArray(M, 0, tmp*tmp);
		for (int i=0; i<M; i++) {
			cp.addEq(sqDelta[i], cp.square(cp.diff(this.EXPECTED, nbCredits[i])));
		}
		obj3 = cp.intVar(0, N*tmp*tmp);
		cp.addEq(cp.sum(sqDelta), obj3);
		
		priority = cp.intVarArray(N, -2, -1);		
		for (int i=0; i<N; i++) {
			for (int j=0; j<D[i].size(); j++) {
				cp.add(cp.imply(cp.eq(x[i], D[i].get(j).intValue()), cp.eq(priority[i], -P[i].get(j).intValue())));
			}
		}
		obj2 = cp.intVar(-2*N, 0);
		cp.addEq(cp.sum(priority), obj2);
		
		//cp.addMinimize(cp.sum(cp.prod(obj1, 10000), obj2, obj3));
		cp.add(cp.minimize(cp.staticLex(cp.prod(obj1, 10000), obj2, obj3)));
	}
	
	public void solve() throws IloException {
		// set CPU time limit (s)
		cp.setParameter(IloCP.DoubleParam.TimeLimit, 60);
		if (cp.solve()) {
			System.out.println("Solution status for solving by Cplex CP optimizer model 02: " + cp.getStatus());
			this.printSolution();
		} else {
			System.out.println("NON-SOLUTION FOUND :(");
		}
		cp.end();
	}
	
	public void printSolution() {
		ArrayList[] result = new ArrayList[M];
		for (int i=0; i<M; i++) {
			result[i] = new ArrayList<Integer>();
		}
		for (int i=0; i<N; i++) {
			int j = (int) cp.getValue(x[i]);
			System.out.println("X[" + i + "] = " + j + ", priority = " + (int) cp.getValue(priority[i]));
			result[j].add(i);
		}
		for (int i=0; i<M; i++) {
			System.out.print("Teacher " + i + ":\tnbCredits[" + i + "] = " + (int)cp.getValue(nbCredits[i]) + "\t");
			int size = result[i].size();
			for (int j=0; j<size; j++) {
				System.out.print(result[i].get(j) + ", ");
			}
			System.out.println();
		}
		System.out.println("Max assignment: " + (int)cp.getValue(obj1));
	}
	
	public static void main(String[] args) throws IloException {
		// TODO Auto-generated method stub
		CplexCPOptimizerModel02 test = new CplexCPOptimizerModel02();
		//test.loadData("data/teacherclassassignment/BCA_small.txt");
		//test.loadData("D:\\Documents\\BCA-SOICT\\bca_input.txt");
		test.loadData("data\\teacherclassassignment\\input_20182_1905_ext_0_format1.txt");
		test.stateModel();
		test.solve();

	}

}
