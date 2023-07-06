/**
 * @class sokoban2sat_forbiddenPositions_reachability_counting_cnf_parallel
 * @author Miquel Bofill and Cristina Borralleras
 * @version 1.1
 * @date 2023-03-21
 * @brief Translation of a "Sokoban" instance to CNF, with reachability (without move actions), ensuring acyclicity by counting neighbours in path, parallel version
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

public class sokoban2sat_forbiddenPositions_reachability_counting_cnf_parallel {

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
    ///       + : character in a hole
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
    private static Pair<List<Integer>,Pair<Integer,String>> initialState(char[][] grid) throws Exception {
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
	
		Pair <Integer,String> pns = new Pair<Integer,String>(nBalls, s);
		return new Pair<List<Integer>,Pair<Integer,String>>(lforbidden, pns);
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


	private static String literal(String lit, Integer index, int time) {
		return lit + "_" + index + "_" + time;
	}

	private static void atMostOneAction(int nSteps, Set<Integer> l, DIMACSFormula f) {
		for (int i = 0; i < nSteps; ++i)
			for (Integer loc: l) {
				f.addClause(List.of(literal("!n", loc, i), literal("!s", loc, i)));
				f.addClause(List.of(literal("!n", loc, i), literal("!e", loc, i)));
				f.addClause(List.of(literal("!n", loc, i), literal("!w", loc, i)));
				f.addClause(List.of(literal("!s", loc, i), literal("!e", loc, i)));
				f.addClause(List.of(literal("!s", loc, i), literal("!w", loc, i)));
				f.addClause(List.of(literal("!e", loc, i), literal("!w", loc, i)));
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


	// There is a ball at location  l  at time  t (or  t+1, if  next)
    	private static List<String> ball_now_or_next(Integer l, int t, boolean next, Set<Integer> validLocations, Map<String,Integer> next2) {
		List<String> ls = new LinkedList<>();
		ls.add("#b_" + l + "_" + t);
		if (next) {
			Integer lnn = next2.get("n" + l);
			Integer lss = next2.get("s" + l);
			Integer lee = next2.get("e" + l);
			Integer lww = next2.get("w" + l);
			if (lnn != null && validLocations.contains(lnn))
				ls.add("s_" + lnn + "_" + t);
			if (lss != null && validLocations.contains(lss))
				ls.add("n_" + lss + "_" + t);
			if (lee != null && validLocations.contains(lee))
				ls.add("w_" + lee + "_" + t);
			if (lww != null && validLocations.contains(lww))
				ls.add("e_" + lww + "_" + t);
		}
		return ls;
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
	

	// @returns "At-Most-k(var_{i_1}^time, ..., var_{i_n}^time)" for i_1, ..., i_n in indexes
	private static List<List<String>> at_most(int k, String var, List<Integer> indexes, Integer time) {
		return at_most(k, var, indexes, time, false);
	}

	// @returns "At-Least-k(var_{i_1}^time, ..., var_{i_n}^time)" for i_1, ..., i_n in indexes
	private static List<List<String>> at_least(int k, String var, List<Integer> indexes, Integer time) {
		return at_most(indexes.size() - k, var, indexes, time, true);
	}
	
	// @returns "Exactly-k(var_{i_1}^time, ..., var_{i_n}^time)" for i_1, ..., i_n in indexes
	private static List<List<String>> exactly(int k, String var, List<Integer> indexes, Integer time) {
		List<List<String>> l = new LinkedList<>();
		l.addAll(at_most(k, var, indexes, time));
		l.addAll(at_least(k, var, indexes, time));
		return l;
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

	private static void ballVariables(boolean bis, int nSteps, Set<Integer> validLocations, Map<String,Integer> next2, DIMACSFormula f) {

		String suffix = bis? "bis" : "";
		String ball = "ball" + suffix;
		List<String> ls;

		for (Integer l : validLocations) {
			for (int t = 0; t < nSteps; ++t) {
				// Definition of  ball  variable
				ls = ball_now_or_next(l, t, bis, validLocations, next2);
				String b = ball + "_" + l + "_" + t;
				add2DIMACSFormula(implication2cnf(ls, List.of(b)), f);
				ls.add("!" + b);
				f.addClause(ls);
			}
		}
	}

	// bis = reachability considering next ball positions as occupied as well
	// target = prefix for the variable representing the target
	// nPath = number of path
	private static void reachabilityConstraints(boolean bis, int nSteps, String target, String nPath, Set<Integer> validLocations, Map<String,Integer> next2, Map<Integer, List<Integer>> validNeighbours, DIMACSFormula f) {

		String suffix = bis? "bis" : "";
		String ball = "ball" + suffix;
		String path_suffix = nPath.isEmpty()? "" : "_" + nPath;
		String path = "p" + suffix + path_suffix;
		String target_n = target + path_suffix;

	    // Exactly one target
		for (int t = 0; t < nSteps; ++t)
			for (List<String> clause : exactly(1, target_n, new LinkedList<>(validLocations), t))
				f.addClause(clause);

		for (Integer l : validLocations) {
			List<Integer> neighbours = validNeighbours.get(l);
			for (int t = 0; t < nSteps; ++t) {
			
				// Ball not in path
				f.addClause(List.of(literal("!" + ball, l, t), literal("!" + path, l, t))); 

			        // Path constraints
				f.addClause(List.of(literal("!#c", l, t), literal(path, l, t)));
				f.addClause(List.of(literal("!" + target_n, l, t), literal(path, l, t)));

				List<String> antecedent1 = new LinkedList<>();
				List<String> antecedent2 = new LinkedList<>();
				List<String> clause = new LinkedList<>();
				String l1 = literal("#c", l, t);
				String l2 = literal(target_n, l, t);
				antecedent1.add("!" + l1);
				antecedent1.add(l2);
				antecedent2.add(l1);
				antecedent2.add("!" + l2);
				for (List<String> succedent : exactly(1, path, neighbours, t)) {
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
				antecedent1.add(literal(target_n, l, t));
				antecedent1.add(literal("!" + path, l, t));
				if (neighbours.size() >= 2) {
					for (List<String> succedent : exactly(2, path, neighbours, t)) {
					
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


	private static void actionTransitions(int nSteps, int nBalls, Set<Integer> l, Set<Integer> lwall, Set<Integer> lwall2, String d, Map<String,Integer> next, Map<String,Integer> next2, DIMACSFormula f) {
		
		// No action allowed
		for (Integer j : lwall) {
			String s = "!" + d + "_" + j + "_";
			for (int i = 0; i < nSteps; ++i)
				f.addClause(List.of(s + i));
		}

		// No action allowed
		for (Integer j : lwall2)
			if (!lwall.contains(j)) {
				String s = "!" + d + "_" + j + "_";
				for (int i = 0; i < nSteps; ++i)
					f.addClause(List.of(s + i));
			}

		// Moving a ball -> not jumping
		for (int i = 0; i < nSteps; ++i) {
			String t = "_" + i;
			f.addClause(List.of("!baction" + t, "!jump" + t));
		}
			
		for (Integer j : l) {
			List<String> ls = new LinkedList<>();

			for (int i = 0; i < nSteps; ++i) {
				String s = j + "_" + i;
				// Actions require reachability
				f.addClause(List.of("jump_pos_" + s, "!jump_" + s));
				List<String> reachable = new LinkedList<String>();
				for (int n = 1; n <= nBalls; ++n)
					reachable.add("baction_pos_" + String.valueOf(n) + "_" + s);
				ls.clear();
				ls.add("!baction_" + s);
				ls.addAll(reachable);
				f.addClause(ls);

				f.addClause(List.of("!jump_" + s, "jump_" + i)); // Jumping somewhere
			}
			// Rest of preconditions and effects of ball movement actions (ROLL)
			if (!lwall.contains(j) && !lwall2.contains(j)) {
				for (int i = 0; i < nSteps; ++i) {
					String t = "_" + i;
					String t1 = "_" + (i + 1);
					String lj = "_" + j;
					String ljn = "_" + next.get(d + j);
					String ljnn = "_" + next2.get(d + j);
					
					f.addClause(List.of("!roll_" + d + lj + t, d + lj + t));
					f.addClause(List.of("roll_" + d + lj + t, "!" + d + lj + t));
					f.addClause(List.of("!" + d + lj + t, "baction" + lj + t)); // Moving a ball
					f.addClause(List.of("!" + d + lj + t, "baction" + t));

					add2DIMACSFormula(implication2cnf(List.of("roll_" + d + lj + t), List.of("!ball" + ljn + t1, "!ball" + ljnn + t, "roll_ball_" + d + lj + t)), f);
					add2DIMACSFormula(implication2cnf(List.of("roll_ball_" + d + lj + t), List.of("#b" + ljn + t, "#b" + ljnn + t1)), f);
				}				
			}
		}
	}


	private static void uselessJumps(int nSteps, Set<Integer> l,
									 Set<Integer> ln, Set<Integer> lnn,
									 Set<Integer> ls, Set<Integer> lss,
									 Set<Integer> le, Set<Integer> lee,
									 Set<Integer> lw, Set<Integer> lww,
									 Map<String,Integer> next,
									 Map<String,Integer> next2,
									 Map<Integer, List<Integer>> validNeighbours,
									 DIMACSFormula f) {

		for (Integer j : l) {
			for (int i = 0; i < nSteps; ++i) {
				String t = "_" + i;
				if ((ln.contains(j) || lnn.contains(j)) &&
					(ls.contains(j) || lss.contains(j)) &&
					(le.contains(j) || lee.contains(j)) &&
					(lw.contains(j) || lww.contains(j)))
					f.addClause(List.of("!jump_" + j + t));
				else {
					// Other useless jumps due to ball locations can be identified. Incomplete list:

					// Location is no next to a ball
					List<Integer> vn = validNeighbours.get(j); // There is at least one valid neighbour location
					List<String> lits = new LinkedList<>();
					for (Integer k : vn)
						lits.addAll(ball_now_or_next(k, i, false, l, next2));
					lits.add("!jump_" + j + t);
					f.addClause(lits);

				}
			}
		}

	}


	private static List<String> surroundingActions(Integer loc, Set<Integer> l, Map<String,Integer> next, int t) {
		Integer north = next.get("n" + loc);
		Integer south = next.get("s" + loc);
		Integer east = next.get("e" + loc);
		Integer west = next.get("w" + loc);
		List<String> lits = new LinkedList<>();
		if (north != null && l.contains(north)) lits.add("s_" + north + "_" + t);
		if (south != null && l.contains(south)) lits.add("n_" + south + "_" + t);
		if (east != null && l.contains(east)) lits.add("w_" + east + "_" + t);
		if (west != null && l.contains(west)) lits.add("e_" + west + "_" + t);
		return lits;
	}

    	private static void frameAxioms(int nSteps, Set<Integer> l,  Map<String,Integer> next, Map<String,Integer> next2, DIMACSFormula f) {
    		LinkedList<String> ls = new LinkedList<>();
		for (int i = 0; i < nSteps; ++i) {
			String t = "_" + i; String t1 = "_" + (i + 1);
			for (Integer j : l) {			
// old:f.addClause(List.of("!#c_" + j + t1, "r_" + j + t));					
				// Changes in balls require action
				List<String> s1 = surroundingActions(j, l, next, i);
				List<String> s2 = surroundingActions(j, l, next2, i);
				ls.clear();
				ls.add("!#b_" + j + t); ls.add("#b_" + j + t1); ls.addAll(s1);
				f.addClause(ls);
				if (!s2.isEmpty()) { // There exists some valid position at distance 2
					ls.clear();
					ls.add("#b_" + j + t); ls.add("!#b_" + j + t1); ls.addAll(s2);
					f.addClause(ls);
				}
				else { // No ball can appear
					f.addClause(List.of("#b_" + j + t, "!#b_" + j + t1));
				}
			}
		}
	
		for (int i = 1; i <= nSteps; ++i) {
			String t = "_" + i;
			String t0 = "_" + (i - 1);
			// Character at most in one location
			//ls.clear();
			for (int j : l) {
				//ls.add("#c_" + j + t);
				for (int k : l)
					if (j < k)
						f.addClause(List.of("!#c_" + j + t, "!#c_" + k + t));
				// Character only moves when jumping
				f.addClause(List.of("!#c_" + j + t, "jump_" + j + t0, "#c_" + j + t0));
			}

			// Character at least in one location
			//f.addClause(ls);
		}

    }


   	private static void mutexes(Integer l, String n, String s, List<Integer> ln, int t, DIMACSFormula f) {
		for (Integer p : ln)
			f.addClause(List.of(literal("!" + n, l, t),  literal("!" + s, p, t)));
   	}
	
	
	private static void incompatibilities(int nSteps, Set<Integer> validLocations, String n, String s, String e, String w, Map<String,Integer> next, Map<String,Integer> next2, DIMACSFormula f) {
		for (Integer l : validLocations) {
			List<Integer> ln = new LinkedList<>();
			List<Integer> le = new LinkedList<>();
			List<Integer> lw = new LinkedList<>();
			Integer lball = next.get(n+l);
			if (lball != null && validLocations.contains(lball)) {
				Integer lnn = next.get(n+lball);
				if (lnn != null && validLocations.contains(lnn)) {
					String n_lnn = n + lnn;
					String e_lnn = e + lnn;
					String w_lnn = w + lnn;
					String e_lball = e + lball;
					String w_lball = w + lball;
					Integer next_n_lnn = next.get(n_lnn);
					Integer next_e_lnn = next.get(e_lnn);
					Integer next_w_lnn = next.get(w_lnn);
					Integer next2_n_lnn = next2.get(n_lnn);
					Integer next2_e_lnn = next2.get(e_lnn);
					Integer next2_w_lnn = next2.get(w_lnn);
					Integer next_e_lball = next.get(e_lball);
					Integer next_w_lball = next.get(w_lball);
					Integer next2_e_lball = next2.get(e_lball);
					Integer next2_w_lball = next2.get(w_lball);
					ln.add(lnn);
					if (next_n_lnn != null && validLocations.contains(next_n_lnn))
						ln.add(next_n_lnn);
					if (next2_n_lnn != null && validLocations.contains(next2_n_lnn))
						ln.add(next2_n_lnn);
					if (next_w_lball != null && validLocations.contains(next_w_lball))
						lw.add(next_w_lball);
					if (next2_w_lball != null && validLocations.contains(next2_w_lball))
						lw.add(next2_w_lball);
					if (next_w_lnn != null && validLocations.contains(next_w_lnn))
						lw.add(next_w_lnn);
					if (next2_w_lnn != null && validLocations.contains(next2_w_lnn))
						lw.add(next2_w_lnn);
					if (next_e_lball != null && validLocations.contains(next_e_lball))
						le.add(next_e_lball);
					if (next2_e_lball != null && validLocations.contains(next2_e_lball))
						le.add(next2_e_lball);
					if (next_e_lnn != null && validLocations.contains(next_e_lnn))
						le.add(next_e_lnn);
					if (next2_e_lnn != null && validLocations.contains(next2_e_lnn))
						le.add(next2_e_lnn);
					for (int i = 0; i < nSteps; ++i) {
						mutexes(l, n, s, ln, i, f);
						mutexes(l, n, e, lw, i, f);
						mutexes(l, n, w, le, i, f);
					}
				} // otherwise action not possible
			} // otherwise action not possible
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

		Pair<List<Integer>,Pair<Integer,String>> p  = initialState(grid);
		List<Integer> lForbiddenPos = p.first;  //CRISTINA: posicions on no pot anar ball perquè seria unsat
		int nBalls = p.second.first;
	 	// Initial state to CNF
		String ini[] = p.second.second.split(" & ");
		for (String s : ini)
			f.addClause(List.of(s));
		// Forbidden Positions for a ball
		forbiddenPositions(nSteps, lForbiddenPos, f); 
		
		// At most one action per location
		// 	atMostOneAction(nSteps, l, f);
		
		// Incompatibilities
		incompatibilities(nSteps, l, "n", "s", "e", "w", next, next2, f);
		incompatibilities(nSteps, l, "s", "n", "e", "w", next, next2, f);
		incompatibilities(nSteps, l, "e", "w", "n", "s", next, next2, f);
		incompatibilities(nSteps, l, "w", "e", "n", "s", next, next2, f);

		// Ball variables
		ballVariables(false, nSteps, l, next2, f);
		ballVariables(true, nSteps, l, next2, f);

		// Reachability constraints
		Map<Integer,List<Integer>> validNeighbours = computeValidNeighbours(l, next);
		reachabilityConstraints(false, nSteps, "jump_pos", "", l, next2, validNeighbours, f);
		for (int n = 1; n <= nBalls; ++n)
			reachabilityConstraints(true, nSteps, "baction_pos", String.valueOf(n), l, next2, validNeighbours, f);
 

		// Action transitions  
		actionTransitions(nSteps, nBalls, l, ln, lnn, "n", next, next2, f);
		actionTransitions(nSteps, nBalls, l, ls, lss, "s", next, next2, f);
		actionTransitions(nSteps, nBalls, l, le, lee, "e", next, next2, f);
		actionTransitions(nSteps, nBalls, l, lw, lww, "w", next, next2, f);

		// Useless jumps
		uselessJumps(nSteps, l, ln, lnn, ls, lss, le, lee, lw, lww, next, next2, validNeighbours, f);


		// Frame axioms
		frameAxioms(nSteps, l, next, next2, f);


		// Goal
		goal(nSteps, l, f);
		System.out.println("c "+nSteps);
		f.print(out);
    }
    

}
