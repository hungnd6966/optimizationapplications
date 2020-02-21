package com.hust.opt.optapp.teacherclassassignment.service.bcasolver.solver;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

import com.hust.opt.optapp.teacherclassassignment.service.bcasolver.DataIO;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cp.IloCP;

public class CPOptCplexSolverPhaseThree {
	
	DataIO io;

	private IloCP cp;
	private IloIntVar[] x;
	private IloIntVar nbCredits[];
	private IloNumExpr obj;

	private int N;// number of classes
	private int M;// number of teachers;
	private int[] credit; // credit[i] is the number of credits of class i
	private ArrayList<Integer>[] D;//D[i] is the list of teachers for class i
	private ArrayList<Integer>[] P;// P[i] is the list of corresponding priority for teacher
	private int[][] conflict;// (conflict[k][0], conflict[k][1]) is a pair of conflicting classes (cannot assigned to the same teacher)
	private int expected;

	public CPOptCplexSolverPhaseThree(DataIO io) {
		this.io = io;
	}
	
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
		
		nbCredits = cp.intVarArray(M, io.getFmin(), io.getFmax());
		cp.add(cp.pack(nbCredits, x, credit));

		obj = cp.numVar(0, io.getFmax());
		cp.addEq(obj, cp.standardDeviation(nbCredits));

		cp.add(cp.minimize(obj));
	}

	public void solve(String filename) throws IloException {
		this.loadData(filename);
		this.stateModel();
		// set CPU time limit (s)
		cp.setParameter(IloCP.DoubleParam.TimeLimit, 
				900);
		if (cp.solve()) {
			System.out.println("Solution status for solving by Cplex CP optimizer model: " + cp.getStatus());
			this.releaseSolution();
		} else {
			System.out.println("NON-SOLUTION FOUND :(");
		}
		cp.end();
	}

	public void releaseSolution() {
		int count = 0;
		for (int i=0; i<conflict.length; i++) {
			int c1 = conflict[i][0];
			int c2 = conflict[i][1];
			for (int j=0; j<M; j++) {
				if (cp.getValue(x[c1])==cp.getValue(x[c2])) {
					System.out.println("Error in solution: conflict classes: " + c1 + " " + c2);
					count ++;
				}
			}
		}
		
		for (int i=0; i<N; i++) {
			if (!D[i].contains((int)cp.getValue(x[i]))) {
				System.out.println("Error in solution: class " + i +  " can not be assigned to teacher " + cp.getValue(x[i]));
				count ++;
			}
		}
		
		if (count>0) {
			System.exit(-1);
		} else {
			int solution[] = new int[N];
			int credits[] =new int[M];
			for (int i=0; i<N; i++) {
				solution[i] = (int) cp.getValue(x[i]);
			}
			for (int i=0; i<M; i++) {
				credits[i] = (int) cp.getValue(nbCredits[i]);
			}
			io.setX(solution);
			io.setCredit(credits);
		}
		System.out.println("obj = " + cp.getValue(obj));
	}

	public static void main(String[] args) throws IloException {

	}

}