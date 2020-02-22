package com.hust.opt.optapp.teacherclassassignment.service.bcasolver;

import com.hust.opt.optapp.teacherclassassignment.service.bcasolver.solver.BasicSolver;
import com.hust.opt.optapp.teacherclassassignment.service.bcasolver.solver.CPOptCplexSolverPhaseThree;
import com.hust.opt.optapp.teacherclassassignment.service.bcasolver.solver.CplexMipSolverPhaseOne;
import com.hust.opt.optapp.teacherclassassignment.service.bcasolver.solver.CplexMipSolverPhaseTwo;
import com.hust.opt.optapp.teacherclassassignment.service.bcasolver.solver.OpenCBLSSolverPhaseThree;

public class Main extends BasicSolver {

	private DataIO io;

	public void option01(String input) throws Exception {
		// cplex cp optimizer
		
		io = new DataIO();
		io.loadData(input);
		System.out.println("\n\n\n\n\n" + input);
		long t0 = System.currentTimeMillis();
		solver = new CplexMipSolverPhaseOne(io);
		solver.solve();
		solver.releaseSolution();
		// io.releaseOutput("data/output/phase1.txt");
		System.out.println("F max: " + io.getFmax());

		solver = new CplexMipSolverPhaseTwo(io);
		solver.solve();
		solver.releaseSolution();
		// io.releaseOutput("data/output/phase2.txt");
		System.out.println("F min: " + io.getFmin());
		
		CPOptCplexSolverPhaseThree cp = new CPOptCplexSolverPhaseThree(io);
		cp.solve(input);
		//io.releaseOutput("data/output/cpopt.txt");
	}

	public void option02(String input) throws Exception {
		//opencbls solver
		
		io = new DataIO();
		io.loadData(input);
		System.out.println("\n\n\n\n\n" + input);
		long t0 = System.currentTimeMillis();
		
		solver = new CplexMipSolverPhaseOne(io);
		solver.solve();
		solver.releaseSolution();
		 io.releaseOutput("data/output/phase1.txt");
		System.out.println("F max: " + io.getFmax());

		solver = new CplexMipSolverPhaseTwo(io);
		solver.solve();
		solver.releaseSolution();
		 io.releaseOutput("data/output/phase2.txt");
		System.out.println("F min: " + io.getFmin());

		solver = new OpenCBLSSolverPhaseThree(io, t0);
		solver.solve();
		solver.releaseSolution();
		 io.releaseOutput("data/output/phase4.txt");
	}

	public static void main(String[] args) throws Exception {
		Main test = new Main();
		
		String input = "data/teacherclassassignment/final/20182/input_20182.txt";
		test.option01(input); // cplex cp optimizer
		test.option02(input); // opencbls
	}
	
}
