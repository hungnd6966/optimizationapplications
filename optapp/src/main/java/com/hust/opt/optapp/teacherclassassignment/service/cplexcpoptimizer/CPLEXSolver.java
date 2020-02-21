package com.hust.opt.optapp.teacherclassassignment.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;


public class CPLEXSolver {

	private int N;  // Number of classes.
	private int M;  // Number of teachers.
	private int[] credit;  // credit[i] is the number of credits of class i.
	private ArrayList<Integer>[] D;  // D[i] is the list of teachers for class i.
	private ArrayList<Integer>[] P;  // P[i] is the list of corresponding priority for teacher.
	private int[][] conflict;  // (conflict[k][0], conflict[k][1]) is a pair of conflicting classes (cannot assigned to the same teacher).
	private ArrayList<Integer>[] teacher_classes;  // teacher_classes[i]: list of classes that teacher[i] can teach.
	private ArrayList<Integer>[] p;
	private double maxCredit, minCredit;  // Stores max, min credit per teacher (from result of phase 1 and 2).
	private ArrayList[] optClasses;
	double[][] time = new double[7][3];
	
	// Modelling.
	IloCplex solver;
	IloIntVar[][] x;
	IloNumVar f;
	IloIntVar[] loadEqualExpValue;  // loadEqualExpValue[i] == 1 <- credit_per_teacher[i] == expValue.
	
	public void loadData(String filename){
		
		try {
			Scanner in = new Scanner(new File(filename));
			N = in.nextInt(); M = in.nextInt();
			D = new ArrayList[N];
			P = new ArrayList[N];
			teacher_classes = new ArrayList[M];
			p = new ArrayList[M];
			
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
			
			for (int i=0; i<M ; i++) {
				teacher_classes[i] = new ArrayList<Integer>();
				p[i] = new ArrayList<Integer>();
			}
			
			for (int i=0; i<N; i++) {
	
				for (int j=0; j<D[i].size(); j++) {
					teacher_classes[D[i].get(j)].add(i);
					p[D[i].get(j)].add(P[i].get(j));
				}
			}
			
//			// Test.
//			for (int teacher=0; teacher<M; teacher++) {
//				
//				System.out.print("Teacher " + teacher + ": ");
//				
//				for (int i=0; i<teacher_classes[teacher].size(); i++) {
//					System.out.print(teacher_classes[teacher].get(i) + ", ");
//				}
//				
//				System.out.println();
//			}
			
		} catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	
	public void exportSolution(String sheetName) throws IOException {		
		
		/*
		 * Export result to excel file.
		 */
		
		File file = new File("C:/Users/Asus/eclipse-workspace/optapp/data/solution/final_solution.xlsx");
		FileInputStream inputStream;
		inputStream = new FileInputStream(file);
		XSSFWorkbook wb = new XSSFWorkbook(inputStream);		
		XSSFSheet sheet = wb.createSheet(sheetName);
		
		int rownum = 0;
		int totalCredit;
		String Classes;
		Cell cell;
		Row row = sheet.createRow(rownum);
		
		cell = row.createCell(0, CellType.STRING);
		cell.setCellValue("Teacher");
		
		cell = row.createCell(1, CellType.STRING);
		cell.setCellValue("Classes");
		
		cell = row.createCell(2, CellType.STRING);
		cell.setCellValue("Total credit");		
		
		for (int i=0; i<M; i++) {
			
			rownum++;
			row = sheet.createRow(rownum);
			
			cell = row.createCell(0, CellType.NUMERIC);
			cell.setCellValue(i);
			
			cell = row.createCell(1, CellType.STRING);
			Classes = "";
			totalCredit = 0;
			
			for (int j=0; j<optClasses[i].size(); j++) {
				Classes += optClasses[i].get(j) + ", ";
				totalCredit += credit[(int) optClasses[i].get(j)];
			}
			
			cell.setCellValue(Classes);
			
			cell = row.createCell(2, CellType.NUMERIC);
			cell.setCellValue(totalCredit);
		}
			inputStream.close();
			FileOutputStream out = new FileOutputStream(file);
	        wb.write(out);
	        out.close();        
	}
	
	
	public double phase3(int expValue) {
		
		int[] optTeachers = new int[M];  // optTeachers[i] = 1: teacher i optimized.
										 //				   = 0: otherwise.
		optClasses = new ArrayList[M];  // optClasses[i]: list of classes that teachers i assigned in optimal solution.
		long startTime = 0, time = 0;
		
		for (int i=0; i<M; i++) {
			optTeachers[i] = 0;
			optClasses[i] = new ArrayList<Integer>();
		}	
		
		while (expValue >= minCredit) {
			
			x = new IloIntVar[M][N];
			
			try {			
				solver = new IloCplex();
				
				for (int i=0; i<M; i++) {
					for (int j=0; j<N; j++) {
						x[i][j] = solver.intVar(0,  0);
					}
					
					if (optTeachers[i] == 1) {
						for (int j=0; j<optClasses[i].size(); j++) {
							x[i][(int) optClasses[i].get(j)] = solver.intVar(1,  1);
						}
					} else {
						for (int j=0; j<teacher_classes[i].size(); j++) {
							x[i][teacher_classes[i].get(j)]= solver.intVar(0,  1);
						}
					}
				}
				
				// Constraints.
				// Constraint 1.
				IloLinearNumExpr[] flow_in = new IloLinearNumExpr[N];
				for (int i=0; i<N; i++) {
					flow_in[i] = solver.linearNumExpr();

					for (int j=0; j<M; j++) {
						flow_in[i].addTerm(1, x[j][i]);
					}

					solver.addEq(flow_in[i], 1);
				}
				
				// Constraint 2.
				for (int i=0; i<conflict.length; i++) {
					for (int j=0; j<M; j++) {
						solver.addLe(solver.sum(solver.prod(1, x[j][conflict[i][0]]),
												solver.prod(1, x[j][conflict[i][1]])),
									 1, "conflict" + i + "   " + j);
					}
				}
				
				// Constraint 3.
				IloLinearNumExpr[] credit_per_teacher = new IloLinearNumExpr[M];
				int bigConst = 10000;
				loadEqualExpValue = new IloIntVar[M];
				
				for (int i=0; i<M; i++) {
					
					if (optTeachers[i] == 0) {
						loadEqualExpValue[i] = solver.intVar(0, 1);
						credit_per_teacher[i] = solver.linearNumExpr();
						
						for (int j=0; j<N; j++) {
							credit_per_teacher[i].addTerm(credit[j]*1.0, x[i][j]);
						}
							
						solver.addLe(credit_per_teacher[i], expValue);		
						solver.addGe(credit_per_teacher[i], this.minCredit);
						
						
						solver.addLe(solver.sum(solver.prod(1, loadEqualExpValue[i]),
			    								solver.prod(bigConst, credit_per_teacher[i])),
									 1 + expValue*bigConst);
						
						solver.addGe(solver.sum(solver.prod(1, loadEqualExpValue[i]),
												solver.prod(-bigConst, credit_per_teacher[i])),
									 1 - expValue*bigConst);
	
					} else {
						loadEqualExpValue[i] = solver.intVar(0, 0);
					}	
				}
				
				// Objective function.
				solver.addMinimize(solver.sum(loadEqualExpValue));
				
				// Solves.
				startTime = System.currentTimeMillis();
				solver.solve();		
				time += System.currentTimeMillis() - startTime;
				
				// Prints + Extracts solution.
				System.out.println("\nExpect value = " + expValue);
				System.out.println("The smallest number of teacher whose total assigned credit equal " 
								 + expValue + " = " + (int)(solver.getObjValue() + 0.25));
				System.out.println("\n\nOptimal solution:");
				System.out.println("\n          Teacher          Classes, (total credits)");
				
				boolean check = false;
				
				for (int i=0; i<M; i++) {				
					
					if (optTeachers[i] == 0) {
						check = false;
						System.out.print("\n             " + i + "             ");
						
						if ((int)(solver.getValue(credit_per_teacher[i])+ 0.25) == expValue) {
							optTeachers[i] = 1;
							check = true;							
						}
						
						for (int j=0; j<N; j++) {
							if (solver.getValue(x[i][j]) > 0.75) {
								System.out.print(j + ", ");
								if (check) {
									optClasses[i].add(j);
								}
							}
						}
						
						System.out.println("(" + (int)solver.getValue(credit_per_teacher[i]) + ")");
					}	
				}	
				
				expValue--;
				
				// Releases resources.
				solver.end();
			} catch (IloException e) {
				e.printStackTrace();
			}		
		}
		
		return 1.0*time/1000;
	}
	
	
	public double phase2() {
		
		x = new IloIntVar[M][N];
		long time = 0;
		
		try {
			solver = new IloCplex();
			f = solver.intVar(0, 16);
			
			for (int i=0; i<M; i++) {
				for (int j=0; j<N; j++) {
					x[i][j] = solver.intVar(0, 0);
				}
			}
			
			for (int j=0; j<N; j++) {
				for (int i=0; i<D[j].size(); i++) {
					x[D[j].get(i)][j] = solver.intVar(0, 1);
				}
			}
			
			// Constraints.
			// Constraint 1.
			IloLinearNumExpr[] flow_in = new IloLinearNumExpr[N];
			for (int i=0; i<N; i++) {
				flow_in[i] = solver.linearNumExpr();

				for (int j=0; j<M; j++) {
					flow_in[i].addTerm(1, x[j][i]);
				}

				solver.addEq(flow_in[i], 1);
			}
			
			// Constraint 2.
			for (int i=0; i<conflict.length; i++) {
				for (int j=0; j<M; j++) {
					solver.addLe(solver.sum(solver.prod(1, x[j][conflict[i][0]]),
											solver.prod(1, x[j][conflict[i][1]])),
								 1);
				}
			}
			
			// Constraint 3.
			IloLinearNumExpr[] credit_per_teacher = new IloLinearNumExpr[M];
			
			for (int i=0; i<M; i++) {
				
				credit_per_teacher[i] = solver.linearNumExpr();
				
				for (int j=0; j<N; j++) {
					credit_per_teacher[i].addTerm(credit[j]*1.0, x[i][j]);
				}
				
				solver.addLe(credit_per_teacher[i], this.maxCredit);		
				solver.addGe(credit_per_teacher[i], f);
			}
			
			// Objective function.
			solver.addMaximize(f);
			
			// Solves.
			time = System.currentTimeMillis();
			solver.solve();
			time = System.currentTimeMillis() - time;
			
			// Prints + Extracts solution.
			this.minCredit = solver.getValue(f);
			System.out.println("\nMin credit = " + (int)(solver.getValue(f) + 0.25));
			//printSolution();
			
		} catch (IloException e) {
			e.printStackTrace();		
		}
		
		return 1.0*time/1000;
	}
	
	
	public double phase1(){
		
		x = new IloIntVar[M][N];
		long time = 0;
		
		try {
			solver = new IloCplex();
			f = solver.intVar(0, 16);
			
			for (int i=0; i<M; i++) {
				for (int j=0; j<N; j++) {
					x[i][j] = solver.intVar(0, 0);
				}
			}
			
			for (int j=0; j<N; j++) {
				for (int i=0; i<D[j].size(); i++) {
					x[D[j].get(i)][j] = solver.intVar(0, 1);
				}
			}
			
			// Constraints.
			// Constraint 1.
			IloLinearNumExpr[] flow_in = new IloLinearNumExpr[N];
			for (int i=0; i<N; i++) {
				flow_in[i] = solver.linearNumExpr();

				for (int j=0; j<M; j++) {
					flow_in[i].addTerm(1, x[j][i]);
				}

				solver.addEq(flow_in[i], 1);
			}
			
			// Constraint 2.
			for (int i=0; i<conflict.length; i++) {
				for (int j=0; j<M; j++) {
					solver.addLe(solver.sum(solver.prod(1, x[j][conflict[i][0]]),
											solver.prod(1, x[j][conflict[i][1]])),
								 1);
				}
			}
			
			// Constraint 3.
			IloLinearNumExpr[] credit_per_teacher = new IloLinearNumExpr[M];
		
			for (int i=0; i<M; i++) {
				
				credit_per_teacher[i] = solver.linearNumExpr();
				
				for (int j=0; j<N; j++) {
					credit_per_teacher[i].addTerm(credit[j]*1.0, x[i][j]);
				}
				
				solver.addLe(credit_per_teacher[i], f);
				
			}
			
			// Objective function.
			solver.addMinimize(f);
			
			// Solves.
			time = System.currentTimeMillis();
			solver.solve();
			time = System.currentTimeMillis() - time;
			
			// Prints + Extracts solution.
			this.maxCredit = solver.getValue(f);
			System.out.println("\nMax credit = " + (int)(solver.getValue(f) + 0.25));
			//printSolution();	
		} catch (IloException e) {
			e.printStackTrace();		
		}	
		
		return 1.0*time/1000;
	}
	
	
	public void printSolution(){				
		
		try {
			System.out.println("\n\nOptimal solution:");
			System.out.println("\n          Teacher          Classes, (total credits)");
			
			int count = 0;
			for (int i=0; i<M; i++) {
				count = 0;
				System.out.print("\n             " + i + "             ");
				
				for (int j=0; j<N; j++) {
					if (solver.getValue(x[i][j]) > 0.75) {
						System.out.print(j + ", ");
						count+= this.credit[j];
					}
				}
				
				System.out.println("(" + count + ")");
			}
		} catch (IloException e) {
			e.printStackTrace();
		}	
	}
	
	
	public static void main(String[] args) {
		
		CPLEXSolver s = new CPLEXSolver();
		String[] data = new String[] {"", "_khmt", "_ktmt", "_cnpm", "_attt", "_httt", "_ttmmt"};
		
		for (int i=0; i<data.length; i++) {
			
			s.loadData("data/final/20182/input_20182" + data[i] + ".txt");
			s.time[i][0] = s.phase1();
			s.time[i][1] = s.phase2();
			s.time[i][2] = s.phase3((int) s.maxCredit);
			
//			try {
//				s.exportSolution("20182" + data[i]);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
		}
		
		for (int i=0; i<data.length; i++) {
			System.out.println(data[i] + ":");
			System.out.println("Phase 1: " + s.time[i][0]);
			System.out.println("Phase 2: " + s.time[i][1]);
			System.out.println("Phase 3: " + s.time[i][2]);
			System.out.println("Total time: " + (s.time[i][0] + s.time[i][1] + s.time[i][2]));
		}
	}
}
