/**
 * @class sokoban2sat_forbiddenPositions_reachability_counting_cnf
 * @author Miquel Bofill and Cristina Borralleras
 * @version 1.1
 * @date 2023-01-02
 * @brief Translation of a "Sokoban" instance to CNF, with reachability (without move actions), ensuring acyclicity by counting neighbours in path
*/

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Set; 
import java.util.TreeSet;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.PrintStream;

public class sokoban2sat_forbiddenPositions_reachability_counting_cnf {

    /// @pre Program has been called with argument <tt> n </tt> where n >= 0 is the
    ///      number of time steps of the desired plan
    ///
    ///      A problem instance is available in the standard input with the format:
    ///
    /// ## ####
    /// ####  ####
    ///  # $ $.  #
    /// ## #  .$ #
    /// #   ##.###
    /// #  $  . #
    /// # @ #   #
    /// #  ######
    /// ####
    ///
    ///      where 
    ///
    ///       [white space] : free cell
    ///       # : wall 
    ///       @ : character
    ///       $ : ball
    ///       . : hole
    ///       * : ball in a hole
    ///       
    ///
    ///       The grid is assumed to be rectangular and closed with possible cells out of walls
    ///	      The input is transformed filling the cells out of walls with walls (#) 
    ///
    /// ##########
    /// ####  ####
    /// ## $ $.  #
    /// ## #  .$ #
    /// #   ##.###
    /// #  $  . ##
    /// # @ #   ##
    /// #  #######
    /// ##########
    
      
    /// @post Outputs a CNF for the problem instance described by the input
    ///       State variables are prefixed by #
    public static void main(String[] args) throws Exception {
		int nSteps = 0;
		try {
			nSteps = Integer.parseInt(args[0]);
			assert nSteps >= 0;
		}
		catch (Exception e) {
			System.err.println("The program needs an integer n >= 0 as argument denoting the number of steps of the desired plan.");
			System.exit(0);
		}
		translate(System.in, System.out, nSteps);
    }

    
    // @post completes the grid replacing the white spaces out of walls with # 
    private static LinkedList<String> completeGrid(LinkedList<String> l, int nColumns) {
    		LinkedList<String> newl = new LinkedList<String>();
    		String first_last_Line = "#".repeat(nColumns); 	
    		newl.add(first_last_Line); 
    		for (int num = 1; num<l.size()-1; num++) {
	    	  String s = l.get(num);
	    	  int first = s.indexOf('#');
	    	  int last = s.lastIndexOf('#'); 
	    	  String firstPart = "";
	    	  if (first>0) firstPart = "#".repeat(first);
	    	  String lastPart = "";
	    	  if (last < nColumns-1) lastPart = "#".repeat(nColumns-last-1);
	    	  s = firstPart+s.substring(first,last+1)+lastPart;
		  newl.add(s);
	      	}
	      	newl.add(first_last_Line);
	      	return newl;
    }

    /// @post Reads the description of the initial state and returns it
    private static char[][] readGrid(InputStream in) throws Exception {
		BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
		LinkedList<String> l = new LinkedList<String>();
		String line = buffer.readLine();
		int nColumns = line.length();
	    	l.add(line);
	    	line = buffer.readLine();
	    	while (line != null && line.length() != 0) {
			l.add(line);
			nColumns = Math.max(line.length(),nColumns);
			line = buffer.readLine();
	    	}
	    	l = completeGrid(l,nColumns); 
		char[][] grid = new char[l.size()][];
		int i = 0;
		for (String s : l){
			//System.out.println(s);
			grid[i++] = s.toCharArray();
		} 
		return grid;
    }
    
    //If loc (position [i,j] in the grid) is included in lforbidden if it is a forbidden position (corner) for a ball
    //pre: i neither first nor last row, j neither first nor last column (due to: loc contains @,$ or ' ')
    private static void check_forbidden(int loc, int i, int j, List<Integer> lforbidden, char[][] grid){
    	boolean upP = grid[i-1][j] == '#';
    	boolean downP = grid[i+1][j] == '#';
    	boolean leftP = grid[i][j-1] == '#';
    	boolean rightP = grid[i][j+1] == '#';
    	if ((upP && leftP) || (upP && rightP) || (downP && leftP) || (downP && rightP))
    		lforbidden.add(loc);
    }
    
    // Returns <number of holes = balls, assertions for the initial state>
    // assumed there can be only one player
    // c for character, h for hole, b for ball  
    private static Pair<List<Integer>,String> initialState(char[][] grid) throws Exception {
    		List<Integer> lforbidden = new LinkedList<>();
		int nRows = grid.length;
		int nCols = grid[0].length;
		int nChar = 0; // Number of characters (players)
		int nBalls = 0; // Number of balls
		int nHoles = 0; // Number of holes
		String s = ""; // = "\n;; Initial state\n";
		for (int i = 0; i < nRows; ++i) {
			for (int j = 0; j < nCols; ++j) {
				int loc = i * nCols + j + 1;
				switch (grid[i][j]) {
				case '#':
					break;
				case '@':   
					nChar++;
					s += "#c_" + loc + "_0 & ";
					s += "!#b_" + loc + "_0 & ";
					s += "!#h_" + loc + "_0 & ";
					check_forbidden(loc, i, j, lforbidden, grid);
					break;
				case '$':  
					nBalls++;
					s += "!#c_" + loc + "_0 & ";
					s += "#b_" + loc + "_0 & ";
					s += "!#h_" + loc + "_0 & ";
					check_forbidden(loc, i, j, lforbidden, grid);
					break;
				case '.':
					nHoles++;
					s += "!#c_" + loc + "_0 & ";
					s += "!#b_" + loc + "_0 & ";
					s += "#h_" + loc + "_0 & ";
					break;
				case '*':
					nHoles++; nBalls++;
					s += "!#c_" + loc + "_0 & ";
					s += "#b_" + loc + "_0 & ";
					s += "#h_" + loc + "_0 & ";
					break;
				case '+':
					nHoles++; nChar++;
					s += "#c_" + loc + "_0 & ";
					s += "!#b_" + loc + "_0 & ";
					s += "#h_" + loc + "_0 & ";
					break;
				case ' ':
					s += "!#c_" + loc + "_0 & ";
					s += "!#b_" + loc + "_0 & ";
					s += "!#h_" + loc + "_0 & ";
					check_forbidden(loc, i, j, lforbidden, grid);
					break;
				default:
					throw new Exception("Symbol '" + grid[i][j] + "' invalid in grid");
				}
			}
		}

		if (nChar != 1)
			throw new Exception("There must be one and only one character");

		if (nBalls != nHoles)
			throw new Exception("Found " + nBalls + " balls and " + nHoles + "holes. They should coincide");

		if (nBalls == 0)
			throw new Exception("There must be at least one ball and one hole");
	
	
		return new Pair<List<Integer>,String>(lforbidden, s);
    }

    // Key = d + l where  d  is a direction ('n', 's', 'e', 'w') and  l  is the number of the location; Value is the number of the location next to  l  in the direction  d
    private static Map<String,Integer> computeNextRelation(int nRows, int nCols) {
		TreeMap<String,Integer> m = new TreeMap<>();
		int loc = 1;
		for (int i = 0; i < nRows; ++i) {
			for (int j = 1; j <= nCols; ++j, ++loc) {
				if (loc > nCols) // Not first row
					m.put("n" + loc, loc - nCols);
				if (loc <= nCols * (nRows - 1)) // Not last row
					m.put("s" + loc, loc + nCols);
				if (loc % nCols != 1) // Not first column
					m.put("w" + loc, loc - 1);
				if (loc % nCols != 0) // Not last column
					m.put("e" + loc, loc + 1);
			}
		}
		return m;
    }

    // Next to the next (in the same direction)
    private static Map<String,Integer> computeNext2Relation(Map<String,Integer> next) {
		TreeMap<String,Integer> m = new TreeMap<>();
		for (Map.Entry<String,Integer> e : next.entrySet()) {
			String k = e.getKey();
			Integer j = next.get(k.substring(0,1) + e.getValue());
			if (j != null)
				m.put(k, j);
		}
		return m;
    }

    private static void	computeSetsNext2Wall(Set<Integer> l, Set<Integer> ln, Set<Integer> lnn, String d, Map<String,Integer> next, Map<String,Integer> next2, char[][] grid) {
		int nCols = grid[0].length;
		for(Integer i : l)
			if (grid[(i - 1) / nCols][(i - 1) % nCols] != '#') {
				Integer j = next.get(d + i);
				Integer k = next2.get(d + i);
				if (j != null && grid[(j - 1) / nCols][(j - 1) % nCols] == '#')
					ln.add(i);
				if (k != null && grid[(k - 1) / nCols][(k - 1) % nCols] == '#')
					lnn.add(i);
			}
    }

	private static void exactlyOneAction(int nSteps, DIMACSFormula f) {
		for (int i = 0; i < nSteps; ++i) {
			f.addClause(List.of("n_" + i, "s_" + i, "e_" + i, "w_" + i));
			f.addClause(List.of("!n_" + i, "!s_" + i));
			f.addClause(List.of("!n_" + i, "!e_" + i));
			f.addClause(List.of("!n_" + i, "!w_" + i));
			f.addClause(List.of("!s_" + i, "!e_" + i));
			f.addClause(List.of("!s_" + i, "!w_" + i));
			f.addClause(List.of("!e_" + i, "!w_" + i));
		}
	}

	/// @pre \p antecedent denotes a disjunction of literals, \p consequent denotes a conjunction of literals
	/// @returns corresponding CNF
	private static List<List<String>> implication2cnf(List<String> antecedent, List<String> consequent) {
		List<List<String>> cnf = new LinkedList<>();
		for (String a : antecedent)
			for (String c : consequent) {
				List<String> clause = new LinkedList<>();
				if (a.charAt(0) == '!')
					clause.add(a);
				else
					clause.add("!" + a);
				clause.add(c);
				cnf.add(clause);
			}
		return cnf;
	}

	private static void add2DIMACSFormula(List<List<String>> cnf, DIMACSFormula f) {
		for (List<String> clause : cnf)
			f.addClause(clause);
	}

	private static Map<Integer,List<Integer>> computeValidNeighbours(Set<Integer> validLocations, Map<String,Integer> next) {
		TreeMap<Integer,List<Integer>> r = new TreeMap<>();
		for (Integer p : validLocations) {
			ArrayList<Integer> validNeighbours = new ArrayList<>();
			Integer n1 = next.get("n" + p);
			Integer n2 = next.get("s" + p);
			Integer n3 = next.get("e" + p);
			Integer n4 = next.get("w" + p);
			if (validLocations.contains(n1))
				validNeighbours.add(n1);
			if (validLocations.contains(n2))
				validNeighbours.add(n2);
			if (validLocations.contains(n3))
				validNeighbours.add(n3);
			if (validLocations.contains(n4))
				validNeighbours.add(n4);
			r.put(p,validNeighbours);
		}
		return r;
	}

	private static String literal(String lit, Integer index, int time) {
		return lit + "_" + index + "_" + time;
	}

	// @returns "At-Most-k(var_{i_1}^time, ..., var_{i_n}^time)" for i_1, ..., i_n in indexes
	private static List<List<String>> at_most(int k, String var, List<Integer> indexes, Integer time) {
		return at_most(k, var, indexes, time, false);
	}

	// @returns "At-Least-k(var_{i_1}^time, ..., var_{i_n}^time)" for i_1, ..., i_n in indexes
	private static List<List<String>> at_least(int k, String var, List<Integer> indexes, Integer time) {
		return at_most(indexes.size() - k, var, indexes, time, true);
	}

	// @returns list of lists of literals
	private static List<List<String>> at_most(int k, String var, List<Integer> indexes, Integer time, boolean negated) {
		LinkedList<List<String>> r = new LinkedList<>();
		String neg = (negated? "" : "!");
		List<List<Integer>> ll = lists(k + 1, indexes);
		for (List<Integer> l : ll) {
			LinkedList<String> r1 = new LinkedList<>();
			for (Integer i : l)
				r1.add(literal(neg + var, i, time));
			r.add(r1);
		}
		return r;
	}

    // @returns the list of subsets of  k  elements of  l
	private static List<List<Integer>> lists(int k, List<Integer> l) {
		List<List<Integer>> ll = new LinkedList<List<Integer>>();
		if (k == 0)
			ll.add(new LinkedList<>());
		else if (l.size() >= k) {
			List<Integer> lr = l.subList(1,l.size());
			List<List<Integer>> lm = lists(k - 1, lr);
			for (List<Integer> m : lm) {
				LinkedList<Integer> lt = new LinkedList<>();
				lt.add(l.get(0));
				lt.addAll(m);
				ll.add(lt);
			}
			ll.addAll(lists(k,lr));
		}
		return ll;
	}
	
	private static void forbiddenPositions(int nSteps, List<Integer> forbiddenLocs, DIMACSFormula f){
		for (int i = 0; i < nSteps; ++i) 
			for (Integer loc : forbiddenLocs)
				f.addClause(List.of("!#b_" + loc + "_" + i)); 
	}

	private static void reachabilityConstraints(PrintStream out, int nSteps, Set<Integer> validLocations, Map<Integer, List<Integer>> validNeighbours, DIMACSFormula f) {

		for (Integer l : validLocations) {
			List<Integer> neighbours = validNeighbours.get(l);
			for (int t = 0; t < nSteps; ++t) {
				int t1 = t + 1;

				// Ball not in path
				f.addClause(List.of(literal("!#b", l, t), literal("!p", l, t1))); 


			        // Path constraints
				f.addClause(List.of(literal("!#c", l, t), literal("p", l, t1)));
				f.addClause(List.of(literal("!#c", l, t1), literal("p", l, t1)));

				List<String> antecedent1 = new LinkedList<>();
				List<String> antecedent2 = new LinkedList<>();
				List<String> clause = new LinkedList<>();
				String l1 = literal("#c", l, t);
				String l2 = literal("#c", l, t1);
				antecedent1.add("!" + l1);
				antecedent1.add(l2);
				antecedent2.add(l1);
				antecedent2.add("!" + l2);
				for (List<String> succedent : at_least(1, "p", neighbours, t1)) {
					clause.clear();
					clause.addAll(antecedent1);
					clause.addAll(succedent);
					f.addClause(clause);
					clause.clear();
					clause.addAll(antecedent2);
					clause.addAll(succedent);
					f.addClause(clause);
				}
				for (List<String> succedent : at_most(1, "p", neighbours, t1)) {
					clause.clear();
					clause.addAll(antecedent1);
					clause.addAll(succedent);
					f.addClause(clause);
					clause.clear();
					clause.addAll(antecedent2);
					clause.addAll(succedent);
					f.addClause(clause);
				}


				antecedent1.clear();
				antecedent1.add(literal("#c", l, t));
				antecedent1.add(literal("#c", l, t1));
				antecedent1.add(literal("!p", l, t1));
				if (neighbours.size() >= 2) {
					for (List<String> succedent : at_least(2, "p", neighbours, t1)) {
						clause.clear();
						clause.addAll(antecedent1);
						clause.addAll(succedent);
						f.addClause(clause);
					}
					for (List<String> succedent : at_most(2, "p", neighbours, t1)) {
						clause.clear();
						clause.addAll(antecedent1);
						clause.addAll(succedent);
						f.addClause(clause);
					}
				}
				else
					f.addClause(antecedent1);

			}
		}

	}


	private static void actionTransitions(int nSteps, Set<Integer> l, Set<Integer> lwall, Set<Integer> lwall2, String d, Map<String,Integer> next, Map<String,Integer> next2, DIMACSFormula f) {
		for (int i = 0; i < nSteps; ++i) {
			String t = "_" + i; String t1 = "_" + (i + 1);

			// No action allowed
			for (Integer j : lwall)
				f.addClause(List.of("!#c_" + j + t1, "!" + d + t));

			// No action allowed
			for (Integer j : lwall2)
				if (!lwall.contains(j)){
					f.addClause(List.of("!#c_" + j + t1, "!" + d + t));
				}
					
			// roll
			for (Integer j : l)
				if (!lwall.contains(j) && !lwall2.contains(j)) {
					String lj = "_" + j;
					String ljn = "_" + next.get(d + j);
					String ljnn = "_" + next2.get(d + j);
					f.addClause(List.of("!#c" + lj + t1, "!" + d + t, "#b" + ljn + t));
					f.addClause(List.of("!#c" + lj + t1, "!" + d + t, "!#b" + ljn + t1));
					f.addClause(List.of("!#c" + lj + t1, "!" + d + t, "!#b" + ljnn + t));
					f.addClause(List.of("!#c" + lj + t1, "!" + d + t, "#b" + ljnn + t1));
				}
		}
	}

    private static void frameAxioms(int nSteps, Set<Integer> l, DIMACSFormula f) {
    	/*
		for (int i = 0; i < nSteps; ++i) {
			String t = "_" + i; String t1 = "_" + (i + 1);

			for (Integer j : l) {
				//new holes cannot appear
				//f.addClause(List.of("#h_" + j + t, "!#h_" + j + t1));
				//holes cannot disappear
				//f.addClause(List.of("!#h_" + j + t, "#h_" + j + t1));
				// character and ball cannot be at the same location at the same time
				f.addClause(List.of("!#c_" + j + t, "!#b_" + j + t));  //POTSER ÉS REDUNDANT
			}
		}
	*/
		// Character at most in one place
		for (int i = 1; i <= nSteps; ++i) {
			String t = "_" + i;
			for (int j : l)
				for (int k : l)
					if (j < k)
						f.addClause(List.of("!#c_" + j + t, "!#c_" + k + t));
		}

    }

    // returns k such that next.get(d + k) is j
    private static Integer inext(char d, Integer j, Map<String,Integer> next) {
		Integer k = null;
		for (Map.Entry<String,Integer> e : next.entrySet()) {
			if (e.getValue().equals(j)) {
				String key = e.getKey();
				if (key.charAt(0) == d)
					return Integer.parseInt(key.substring(1));
			}
		}
		return k;
    }

    private static void frameAxioms(int nSteps, Set<Integer> l, String d, Map<String,Integer> next, Map<String,Integer> next2, DIMACSFormula f) {
		for (int i = 0; i < nSteps; ++i) {
			String t = "_" + i; String t1 = "_" + (i + 1);
	    
			for (Integer j : l) {
				Integer jo = inext(d.charAt(0), j, next);
				Integer joo = inext(d.charAt(0), j, next2);
				String lj = "_" + j;
				String ljo = "_" + jo;
				String ljoo = "_" + joo;


				// Axioms

				if (jo != null && l.contains(jo)) {

					f.addClause(List.of("!#b" + lj + t, "#b" + lj + t1, "!" + d + t, "#c" + ljo + t1));

					if (joo != null && l.contains(joo)) {

						f.addClause(List.of("#b" + lj + t, "!#b" + lj + t1, "!" + d + t, "#c" + ljoo + t1));
						f.addClause(List.of("#b" + lj + t, "!#b" + lj + t1, "!" + d + t, "#b" + ljo + t));
					}
					else {
						f.addClause(List.of("#b" + lj + t, "!#b" + lj + t1, "!" + d + t));
					}
				}
				else {
					f.addClause(List.of("!#b" + lj + t, "#b" + lj + t1, "!" + d + t));
					f.addClause(List.of("#b" + lj + t, "!#b" + lj + t1, "!" + d + t));
					
				}
			}
		}
	}

    private static void goal(int nSteps, Set<Integer> validLocations, DIMACSFormula f) {
		for (Integer loc : validLocations) {
			f.addClause(List.of("!#b_" + loc + "_" + nSteps, "#h_" + loc + "_0" ));
			f.addClause(List.of("#b_" + loc + "_" + nSteps, "!#h_" + loc + "_0" ));
		}
    }
    
    private static void translate(InputStream in, PrintStream out, int nSteps) throws Exception {
		DIMACSFormula f = new DIMACSFormula();
		char[][] grid = readGrid(in);
	
		int nRows = grid.length;
		int nCols = grid[0].length;
		int nLocs = nRows * nCols;
		Set<Integer> l = new TreeSet<>(); // Set of valid locations

		int loc = 1;
		for (int i = 0; i < nRows; i++) {
			for (int j = 0; j < nCols; j++, ++loc) {
				if (grid[i][j] != '#')
					l.add(loc);
			}
		}


		Map<String,Integer> next = computeNextRelation(nRows, nCols); // Key = d + l where  d  is a direction ('n', 's', 'e', 'w') and  l  is the number of the location; Value is the number of the location next to  l  in the direction  d
		Map<String,Integer> next2 = computeNext2Relation(next); // Next to the next (in the same direction)
		Set<Integer> ln = new TreeSet<>(); // Set of valid positions with a wall in the north
		Set<Integer> lnn = new TreeSet<>(); // Set of valid positions with a wall two steps ahead in the north
		Set<Integer> ls = new TreeSet<>(); // Set of valid positions with a wall in the south
		Set<Integer> lss = new TreeSet<>(); // Set of valid positions with a wall two steps ahead in the south
		Set<Integer> le = new TreeSet<>(); // Set of valid positions with a wall in the east
		Set<Integer> lee = new TreeSet<>(); // Set of valid positions with a wall two steps ahead in the east
		Set<Integer> lw = new TreeSet<>(); // Set of valid positions with a wall in the west
		Set<Integer> lww = new TreeSet<>(); // Set of valid positions with a wall two steps ahead in the west

		computeSetsNext2Wall(l, ln, lnn, "n", next, next2, grid);
		computeSetsNext2Wall(l, ls, lss, "s", next, next2, grid);
		computeSetsNext2Wall(l, le, lee, "e", next, next2, grid);
		computeSetsNext2Wall(l, lw, lww, "w", next, next2, grid);

		Pair<List<Integer>,String> p  = initialState(grid);
		List<Integer> lForbiddenPos = p.first;  //CRISTINA: posicions on no pot anar ball perquè seria unsat

	 	// Initial state to CNF
		String ini[] = p.second.split(" & ");
		for (String s : ini)
			f.addClause(List.of(s));
		// Forbidden Positions for a ball
		forbiddenPositions(nSteps, lForbiddenPos, f); 
		// Exactly one action per time step
		exactlyOneAction(nSteps, f); 

		// Reachability constraints
		Map<Integer,List<Integer>> validNeighbours = computeValidNeighbours(l, next);
		reachabilityConstraints(out, nSteps, l, validNeighbours, f); 

		// Action transitions  
		actionTransitions(nSteps, l, ln, lnn, "n", next, next2, f);
		actionTransitions(nSteps, l, ls, lss, "s", next, next2, f);
		actionTransitions(nSteps, l, le, lee, "e", next, next2, f);
		actionTransitions(nSteps, l, lw, lww, "w", next, next2, f);

		// Frame axioms
		frameAxioms(nSteps, l, f);
		frameAxioms(nSteps, l, "n", next, next2, f);
		frameAxioms(nSteps, l, "s", next, next2, f);
		frameAxioms(nSteps, l, "e", next, next2, f);
		frameAxioms(nSteps, l, "w", next, next2, f);

		// Goal
		goal(nSteps, l, f);
		System.out.println("c "+nSteps);
		f.print(out);
    }

}
