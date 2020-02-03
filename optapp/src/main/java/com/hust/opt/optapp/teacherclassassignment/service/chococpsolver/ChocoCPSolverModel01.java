package com.hust.opt.optapp.teacherclassassignment.service.chococpsolver;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.objective.ParetoOptimizer;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.view.OffsetView;

import net.bytebuddy.matcher.ModifierMatcher.Mode;

public class ChocoCPSolverModel01 {

	private Model model;
	private BoolVar[][] x; // x[i][j] = 1 indicates that subject i is assigned to the teacher j
	private IntVar[] nbCredits; // s[i] is the total credits that assigned to the teacher i
	private IntVar[] priority;
	private IntVar obj1; 
	private IntVar obj2; 
	private IntVar obj3;

	private int N;// number of classes
	private int M;// number of teachers;
	private int[] credit; // credit[i] is the number of credits of class i
	private int[][] p; // p[i][j] = k indicates that we can assign the subject i to the teacher j and k is the corresponding priority of the subject i
	private int[][] conflict;// (conflict[k][0], conflict[k][1]) is a pair of conflicting classes (cannot assigned to the same teacher)
	private final int MAX_ASSIGNMENT = 30;
	private final int EXPECTED = 13;
	
	
	public void loadData(String filename) {
		try {
			Scanner in = new Scanner(new File(filename));
			N = in.nextInt();
			M = in.nextInt();
			p = new int[N][M];
			credit = new int[N];

			for (int i = 0; i < N; i++) {
				int id = in.nextInt();
				credit[id] = in.nextInt();
				int k = in.nextInt();
				for (int j = 0; j < k; j++) {
					int u = in.nextInt();
					p[i][u] = in.nextInt();
				}
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
		x = new BoolVar[N][M];
		nbCredits = new IntVar[M];

		x = model.boolVarMatrix(N, M);
		nbCredits = model.intVarArray(M, 0, MAX_ASSIGNMENT);

		for (int i = 0; i < N; i++) {
			model.sum(x[i], "=", 1).post();
		}
		
		for (int i=0; i<N; i++) {
			int count=0;
			int t = 0;
			for (int j=0; j<M; j++) {
				if (p[i][j]>0) {
					count++;
					t = j;
				}
			}
			if (count==1) {
				model.arithm(x[i][t], "=", 1).post();
			}
		}

		for (int i = 0; i < N; i++) {
			for (int j = 0; j < M; j++) {
				if (p[i][j] == 0) {
					model.arithm(x[i][j], "=", 0).post();
				}
			}
		}

		int size = conflict.length;
		for (int i = 0; i < size; i++) {
			int c1 = conflict[i][0];
			int c2 = conflict[i][1];
			for (int j = 0; j < M; j++) {
				model.arithm(x[c1][j], "+", x[c2][j], "<", 2).post();
			}
		}

		for (int j = 0; j < M; j++) {
			IntVar[] tmp = new IntVar[N];
			int idx = 0;
			for (int i = 0; i < N; i++) {
				tmp[idx++] = model.intScaleView(x[i][j], credit[i]);
			}
			model.sum(tmp, "=", nbCredits[j]).post();
		}
		obj1 = model.intVar(0, MAX_ASSIGNMENT);
		model.max(obj1, nbCredits).post();
		
		int tmp = Math.max(EXPECTED, MAX_ASSIGNMENT - EXPECTED);
		IntVar[] sqDelta = model.intVarArray(M, 0, tmp*tmp);
		for (int i=0; i<M; i++) {
			model.square(sqDelta[i], new OffsetView(nbCredits[i], -EXPECTED)).post();
		}
		obj2 = model.intVar(0, tmp*tmp*M);
		model.max(obj2, sqDelta).post();
		
		priority = model.intVarArray(N, 1, 2);
		for (int i=0; i<N; i++) {
			model.scalar(x[i], p[i], "=", priority[i]).post();
		}
		obj3 = model.intVar(0, 2*N);
		model.sum(priority, "=", obj3).post();
		
		/*
		 * IntVar[] objs = new IntVar[] {obj1, obj2, obj3}; // maxAssignment, sum sqDelta, sum priority 
		 * int[] coeff = new int[] {999999, 1, -1}; 
		 * IntVar objectiveFunction = model.intVar(0, (int)1e8); 
		 * model.scalar(objs, coeff, "=", objectiveFunction).post(); 
		 * model.setObjective(model.MINIMIZE, objectiveFunction);
		 */
	}

	public void solve() {
		long t0 = System.currentTimeMillis();
		System.out.println(new Date() + ": Start solving the BCA problem with ChocoCPSolver model 01...");
		
		Solver solver = this.model.getSolver();
		solver.limitTime("300s");
		ParetoOptimizer obj = new ParetoOptimizer(true, new IntVar[] {obj1, obj2, obj3});
		solver.plugMonitor(obj);
		
		while (this.model.getSolver().solve()) {
			System.out.println("\nSolution found: ");
			ArrayList result[] = new ArrayList[M];
			for (int i=0; i<M; i++) {
				result[i] = new ArrayList<Integer>();
			}
			for (int i = 0; i < N; i++) {
				for (int j = 0; j < M; j++) {
					if (x[i][j].getValue() == 1) {
						System.out.println("x[" + i + "] = " + j + ", priority = " + priority[i].getValue());
						result[j].add(i);
						break;
					}
				}
			}
			for (int i=0; i<M; i++) {
				System.out.print("Teacher " + i + ":\tnbCredits[" + i + "] = " + nbCredits[i].getValue() + "\t");
				int size = result[i].size();
				for (int j=0; j<size; j++) {
					System.out.print(result[i].get(j) + ", ");
				}
				System.out.println();
			}
			System.out.println("max assign: " + obj1.getValue());
			System.out.println("Running time (ms): " + (System.currentTimeMillis() - t0));
			System.out.println("Searching for better solution...");
		}
	}

	public static void main(String[] args) throws ContradictionException {
		ChocoCPSolverModel01 test = new ChocoCPSolverModel01();
		//test.loadData("data/teacherclassassignment/input_20182_1905_ext_4.txt");
		// test.loadData("data/teacherclassassignment/BCA_small.txt");
		//test.loadData("D:\\Documents\\BCA-SOICT\\bca_input.txt");
		test.loadData("data\\teacherclassassignment\\input_20182_1905_ext_0_format1.txt");
		test.stateModel();
		test.solve();
	}

}
