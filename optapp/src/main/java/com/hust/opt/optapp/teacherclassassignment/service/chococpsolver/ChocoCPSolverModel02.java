package com.hust.opt.optapp.teacherclassassignment.service.chococpsolver;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;

public class ChocoCPSolverModel02 {
	
	private Model model;
	private IntVar[] x; // x[i] = j indicates that subject i is assigned to the teacher j
	private IntVar[] nbCredits; // s[i] is the total credits that assigned to the teacher i
	
	private ArrayList<int[]> domain; // domain[i] is the domain of x[i]
	private int N;// number of classes
	private int M;// number of teachers;
	private int[] credit; // credit[i] is the number of credits of class i
	private int[][] p; // p[i][j] = k indicates that we can assign the subject i to the teacher j and k is the corresponding priority of the subject i
	private int[][] conflict;// (conflict[k][0], conflict[k][1]) is a pair of conflicting classes (cannot assigned to the same teacher)
	private final int MAX_ASSIGNMENT = 30;
	
	public void loadData(String filename) {
		try {
			Scanner in = new Scanner(new File(filename));
			N = in.nextInt();
			M = in.nextInt();
			p = new int[N][M];
			credit = new int[N];
			domain = new ArrayList<int[]>();
			
			for (int i = 0; i < N; i++) {
				int id = in.nextInt();
				credit[id] = in.nextInt();
				int k = in.nextInt();
				
				int[] dom = new int[k];
				for (int j = 0; j < k; j++) {
					int u = in.nextInt();
					p[i][u] = in.nextInt();
					dom[j] = u;
				}
				domain.add(dom);
			}
			int K = in.nextInt();
			conflict = new int[K][2];
			for (int k = 0; k < K; k++) {
				conflict[k][0] = in.nextInt();
				conflict[k][1] = in.nextInt();
			}
			in.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void stateModel() {
		model = new Model();
		x = new IntVar[N];
		nbCredits = new IntVar[M];
		
		for (int i=0; i<N; i++) {
			x[i] =  model.intVar(domain.get(i));
		}
		
		int tmp = conflict.length;
		for (int i=0; i<tmp; i++) {
			int c1 = conflict[i][0];
			int c2 = conflict[i][1];
			model.arithm(x[c1], "!=", x[c2]).post();
		}
		
		nbCredits = model.intVarArray("s", M, 0, MAX_ASSIGNMENT);
		model.binPacking(x, credit, nbCredits, 0).post();
		
	}
	
	public void solve() {
		long t0 = System.currentTimeMillis();
		System.out.println(new Date() + ": Start solving the problem with ChocoCPSolver model 02...");
		Solver solver = this.model.getSolver();
		solver.limitTime("300s");
		if (solver.solve()) {
			for (int i=0; i<N; i++) {
				System.out.println("x[" + i + "] = " + x[i].getValue());
			}
			int maxassign = 0;
			for (int i = 0; i < M; i++) {
				int t = nbCredits[i].getValue();
				System.out.println("s[" + i + "] = " + t);
				maxassign = maxassign<t?t:maxassign;
			}
			System.out.println("max assign: " + maxassign);
		} else if (solver.hasEndedUnexpectedly()) {
			System.out.println("Could not find a solution nor prove that none exists :(");
		} else {
			System.out.println("NON-SOLUTION :(");
		}
		System.out.println("Running time (ms): " + (System.currentTimeMillis() - t0));
	}
	
	public static void main(String[] args) {
		ChocoCPSolverModel02 test = new ChocoCPSolverModel02();
		// test.loadData("data/teacherclassassignment/BCA_small.txt");
		// test.loadData("data/teacherclassassignment/input_20182_1905_ext_4.txt");
		test.loadData("data\\teacherclassassignment\\input_20182_1905_ext_0_format1.txt");
		//test.loadData("D:\\Documents\\BCA-SOICT\\bca_input.txt");
		test.stateModel();
		test.solve();
	}
	

}
