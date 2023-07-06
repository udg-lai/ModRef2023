/**
 * @class snowman2sat_reachability_counting_cnf_parallel_invariants
 * @author Miquel Bofill
 * @version 1.4
 * @date 2023-04-08
 * @brief Translation of "A good snowman is hard to build" instance to CNF, with reachability (without move actions) ensuring acyclicity by counting neighbours in path, parallel version, with ball invariants using sequential counters
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

public class snowman2sat_reachability_counting_cnf_parallel_invariants {

    /// @pre Program has been called with argument <tt> n </tt> where n >= 0 is the
    ///      number of time steps of the desired plan
    ///
    ///      A problem instance is available in the standard input with the format:
    ///
    ///      xx#######
    ///      ##..1...#
    ///      #.##.##.#
    ///      #...'2..#
    ///      #..#.#..#
    ///      #...1...#
    ///      ##..q..##
    ///      ######### 
    ///
    ///      where
    ///
    ///       x : out of grid
    ///       # : wall
    ///       p : character with snow on the floor
    ///       q : character
    ///       1 : small ball
    ///       2 : medium ball
    ///       3 : small ball on top of a medium ball
    ///       4 : large ball
    ///       5 : small ball on top of a large ball
    ///       6 : medium ball on top of a large ball
    ///       7 : small ball on top of a medium ball on top of a large ball
    ///       ' : grass
    ///       . : snow
    ///
    ///       The grid is assumed to be rectangular and closed

    static int aux = 0; // For naming auxiliary variables
    
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
			line = buffer.readLine();
	    }
		char[][] grid = new char[l.size()][];
		int i = 0;
		for (String s : l)
			grid[i++] = s.toCharArray();
		return grid;
    }

    // Returns <number of snowmans, assertions for the initial state>
    // Replaces 'x' by '#' in the grid
    private static Pair<Integer,String> initialState(char[][] grid) throws Exception {
		int nRows = grid.length;
		int nCols = grid[0].length;
		int nChar = 0; // Number of characters (players)
		int nBall = 0; // Number of balls
		int nSmall = 0; // Number of small balls
		int nLarge = 0; // Number of large balls
		String s = ""; // = "\n;; Initial state\n";
		for (int i = 0; i < nRows; ++i) {
			for (int j = 0; j < nCols; ++j) {
				int loc = i * nCols + j + 1;
				switch (grid[i][j]) {
				case 'x':
					grid[i][j] = '#';
					break;
				case '#':
					break;
				case 'p':
					nChar++;
					s += "#c_" + loc + "_0 & ";
					s += "#snow_" + loc + "_0 & ";
					s += "!#bs_" + loc + "_0 & ";
					s += "!#bm_" + loc + "_0 & ";
					s += "!#bl_" + loc + "_0 & ";
					break;
				case 'q':
					nChar++;
					s += "#c_" + loc + "_0 & ";
					s += "!#snow_" + loc + "_0 & ";
					s += "!#bs_" + loc + "_0 & ";
					s += "!#bm_" + loc + "_0 & ";
					s += "!#bl_" + loc + "_0 & ";
					break;
				case '1':
					nBall++; nSmall++;
					s += "!#c_" + loc + "_0 & ";
					s += "!#snow_" + loc + "_0 & ";
					s += "#bs_" + loc + "_0 & ";
					s += "!#bm_" + loc + "_0 & ";
					s += "!#bl_" + loc + "_0 & ";
					break;
				case '2':
					nBall++;
					s += "!#c_" + loc + "_0 & ";
					s += "!#snow_" + loc + "_0 & ";
					s += "!#bs_" + loc + "_0 & ";
					s += "#bm_" + loc + "_0 & ";
					s += "!#bl_" + loc + "_0 & ";
					break;
				case '3':
					nBall += 2; nSmall++;
					s += "!#c_" + loc + "_0 & ";
					s += "!#snow_" + loc + "_0 & ";
					s += "#bs_" + loc + "_0 & ";
					s += "#bm_" + loc + "_0 & ";
					s += "!#bl_" + loc + "_0 & ";
					break;
				case '4':
					nBall++; nLarge++;
					s += "!#c_" + loc + "_0 & ";
					s += "!#snow_" + loc + "_0 & ";
					s += "!#bs_" + loc + "_0 & ";
					s += "!#bm_" + loc + "_0 & ";
					s += "#bl_" + loc + "_0 & ";
					break;
				case '5':
					nBall += 2; nSmall++; nLarge++;
					s += "!#c_" + loc + "_0 & ";
					s += "!#snow_" + loc + "_0 & ";
					s += "#bs_" + loc + "_0 & ";
					s += "!#bm_" + loc + "_0 & ";
					s += "#bl_" + loc + "_0 & ";
					break;
				case '6':
					nBall += 2; nLarge++;
					s += "!#c_" + loc + "_0 & ";
					s += "!#snow_" + loc + "_0 & ";
					s += "!#bs_" + loc + "_0 & ";
					s += "#bm_" + loc + "_0 & ";
					s += "#bl_" + loc + "_0 & ";
					break;
				case '7':
					nBall += 3; nSmall++; nLarge++;
					s += "!#c_" + loc + "_0 & ";
					s += "!#snow_" + loc + "_0 & ";
					s += "#bs_" + loc + "_0 & ";
					s += "#bm_" + loc + "_0 & ";
					s += "#bl_" + loc + "_0 & ";
					break;
				case '\'':
					s += "!#c_" + loc + "_0 & ";
					s += "!#snow_" + loc + "_0 & ";
					s += "!#bs_" + loc + "_0 & ";
					s += "!#bm_" + loc + "_0 & ";
					s += "!#bl_" + loc + "_0 & ";
					break;
				case '.':
					s += "!#c_" + loc + "_0 & ";
					s += "#snow_" + loc + "_0 & ";
					s += "!#bs_" + loc + "_0 & ";
					s += "!#bm_" + loc + "_0 & ";
					s += "!#bl_" + loc + "_0 & ";
					break;
				default:
					throw new Exception("Symbol '" + grid[i][j] + "' invalid in grid");
				}
			}
		}

		if (nChar != 1)
			throw new Exception("There must be one and only one character");

		if (nBall % 3 != 0)
			throw new Exception("Found " + nBall + " balls (should be a multiple of three)");

		int nSnowman = nBall / 3;
	
		if (nSmall < nSnowman)
			throw new Exception("Trivially unsatisfiable (" + nSmall + " are too few small balls)");
	
		if (nLarge > nSnowman)
			throw new Exception("Trivially unsatisfiable (" + nLarge + " are too many large balls)");
	
		return new Pair<Integer,String>(nSnowman, s);
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
		ls.add("#bs_" + l + "_" + t);
		ls.add("#bm_" + l + "_" + t);
		ls.add("#bl_" + l + "_" + t);
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

	// @returns "At-Most-k(var_{i_1}^time, ..., var_{i_n}^time)" for i_1, ..., i_n in indexes (binomial encoding)
	private static List<List<String>> binomial_at_most(int k, String var, List<Integer> indexes, Integer time) {
		return binomial_at_most(k, var, indexes, time, false);
	}

	// @returns "At-Least-k(var_{i_1}^time, ..., var_{i_n}^time)" for i_1, ..., i_n in indexes
	private static List<List<String>> binomial_at_least(int k, String var, List<Integer> indexes, Integer time) {
		return binomial_at_most(indexes.size() - k, var, indexes, time, true);
	}

	// @returns "Exactly-k(var_{i_1}^time, ..., var_{i_n}^time)" for i_1, ..., i_n in indexes
	private static List<List<String>> binomial_exactly(int k, String var, List<Integer> indexes, Integer time) {
		List<List<String>> l = new LinkedList<>();
		l.addAll(binomial_at_most(k, var, indexes, time));
		l.addAll(binomial_at_least(k, var, indexes, time));
		return l;
	}

	// @returns list of lists of literals
	private static List<List<String>> binomial_at_most(int k, String var, List<Integer> indexes, Integer time, boolean negated) {
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

	// @returns "At-Most-k(var_{i_1}^time, ..., var_{i_n}^time)" for i_1, ..., i_n in indexes
	private static List<List<String>> seq_at_most(int k, String var, List<Integer> indexes, Integer time) {
		return seq_at_most(k, var, indexes, time, false);
	}

	// @returns "At-Least-k(var_{i_1}^time, ..., var_{i_n}^time)" for i_1, ..., i_n in indexes
	private static List<List<String>> seq_at_least(int k, String var, List<Integer> indexes, Integer time) {
		return seq_at_most(indexes.size() - k, var, indexes, time, true);
	}

	// @returns "Exactly-k(var_{i_1}^time, ..., var_{i_n}^time)" for i_1, ..., i_n in indexes
	private static List<List<String>> seq_exactly(int k, String var, List<Integer> indexes, Integer time) {
		List<List<String>> l = new LinkedList<>();
		l.addAll(seq_at_most(k, var, indexes, time));
		l.addAll(seq_at_least(k, var, indexes, time));
		return l;
	}

	// @pre 0 <= k < n, n > 0;
	// @returns list of lists of literals (Sequential Counter Encoding by Sinz); aux is used for naming of auxiliary variables
	private static List<List<String>> seq_at_most(int k, String var, List<Integer> indexes, Integer time, boolean negated) {
		LinkedList<List<String>> r = new LinkedList<>();
		String v = (negated? "" : "!") + var;
		String suma = "suma_" + (++aux);
		int n = indexes.size();

		if (k == n - 1 && negated) {
			// at-least-one
			List<String> r1 = new LinkedList<>();
			for (Integer i : indexes)
				r1.add(literal(v, i, time));
			r.add(r1);
		}
		else {
			assert (k < n);
			if (k > 0) {
				r.add(List.of(literal(v, indexes.get(0), time), suma + "_1_1_" + time));

				for (int j = 2; j <= k; ++j)
					r.add(List.of("!" + suma + "_1_" + j + "_" + time));

				for (int i = 2; i < n; ++i) {
					r.add(List.of(literal(v, indexes.get(i - 1), time), suma + "_" + i + "_1_" + time));
					r.add(List.of("!" + suma + "_" + (i - 1) + "_1_" + time, suma + "_" + i + "_1_" + time));

					for (int j = 2; j <= k; ++j) {
						r.add(List.of(literal(v, indexes.get(i - 1), time), "!" + suma + "_" + (i - 1) + "_" + (j - 1) + "_" + time, suma + "_" + i + "_" + j + "_" + time));
						r.add(List.of("!" + suma + "_" + (i - 1) + "_" + j + "_" + time, suma + "_" + i + "_" + j + "_" + time));
					}

					r.add(List.of(literal(v, indexes.get(i - 1), time), "!" + suma + "_" + (i - 1) + "_" + k + "_" + time));
				}

				r.add(List.of(literal(v, indexes.get(n - 1), time), "!" + suma + "_" + (n - 1) + "_" + k + "_" + time));
			}
			else {
				// k = 0 => at-least-n
				for (Integer i : indexes)
					r.add(List.of(literal(v, i, time)));
			}
		}

		return r;
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
			for (List<String> clause : seq_exactly(1, target_n, new LinkedList<>(validLocations), t))
				f.addClause(clause);

		for (Integer l : validLocations) {

			List<Integer> neighbours = validNeighbours.get(l);

			for (int t = 0; t < nSteps; ++t) {

				// Balls not in path
				f.addClause(List.of(literal("!" + ball, l, t), literal("!" + path, l, t)));

				// OLD
				// Character always next to a ball: unnecessary, gets worse (already covered by actions)
				// out.print("(!c_" + l + "_" + t1);
				// for (Integer l1 : neighbours)
				// 	out.print(" | bs_" + l1 + "_" + t + " | bm_" + l1 + "_" + t + " | bl_" + l1 + "_" + t);
				// out.print(") & ");

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
				for (List<String> succedent : binomial_exactly(1, path, neighbours, t)) {
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
					for (List<String> succedent : binomial_exactly(2, path, neighbours, t)) {
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

	private static void auxiliaryVariables(int nSteps, Set<Integer> l, DIMACSFormula f) {
		List<String> ls = new LinkedList<>();
		for (int i = 0; i < nSteps; ++i) {
			String t = "_" + i; String t1 = "_" + (i + 1);

			for (Integer j : l) {
				String lj = "_" + j;

				ls.clear();
				ls.add("#bm" + lj + t); ls.add("#bl" + lj + t);
				add2DIMACSFormula(implication2cnf(ls, List.of("ball_m_l" + lj + t)), f); // Unnecessary but helping
				ls.add("!ball_m_l" + lj + t);
				f.addClause(ls);

				String s = "#snow" + lj + t; String ns = "!" + s;
				String bm = "#bm" + lj + t; String nbm = "!" + bm;
				String bl = "#bl" + lj + t; String nbl = "!" + bl;
				String bs1 = "#bs" + lj + t1; String nbs1 = "!" + bs1;
				String bm1 = "#bm" + lj + t1; String nbm1 = "!" + bm1;
				String bl1 = "#bl" + lj + t1; String nbl1 = "!" + bl1;

				f.addClause(List.of("!staym" + lj + t, nbm, bm1));
				f.addClause(List.of("!staym" + lj + t, nbm1, bm));
				f.addClause(List.of("!stayl" + lj + t, nbl, bl1));
				f.addClause(List.of("!stayl" + lj + t, nbl1, bl));

				ls.clear();
				ls.add("!grow_s" + lj + t); ls.add(s); ls.add(bs1);
				f.addClause(ls);
				ls.remove(ls.size() - 1); ls.add(nbm1);
				f.addClause(ls);
				ls.remove(ls.size() - 1); ls.add(nbl1);
				f.addClause(ls);

				ls.remove(ls.size() - 1);
				ls.remove(ls.size() - 1);
				ls.add(ns); ls.add(nbs1);
				f.addClause(ls);
				ls.remove(ls.size() - 1); ls.add(bm1);
				f.addClause(ls);
				ls.remove(ls.size() - 1); ls.add(nbl1);
				f.addClause(ls);

				ls.clear();
				ls.add("!grow_m" + lj + t); ls.add(s); ls.add(nbs1);
				f.addClause(ls);
				ls.remove(ls.size() - 1); ls.add(bm1);
				f.addClause(ls);
				ls.remove(ls.size() - 1); ls.add(nbl1);
				f.addClause(ls);

				ls.remove(ls.size() - 1);
				ls.remove(ls.size() - 1);
				ls.add(ns); ls.add(nbs1);
				f.addClause(ls);
				ls.remove(ls.size() - 1); ls.add(nbm1);
				f.addClause(ls);
				ls.remove(ls.size() - 1); ls.add(bl1);
				f.addClause(ls);
			}
		}
	}

 	private static void actionTransitions(int nSteps, int nSnowman, Set<Integer> l, Set<Integer> lwall, Set<Integer> lwall2, String d, Map<String,Integer> next, Map<String,Integer> next2, DIMACSFormula f) {

		int nBall = nSnowman * 3;

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
				for (int n = 1; n <= nBall; ++n)
					reachable.add("baction_pos_" + String.valueOf(n) + "_" + s);
				ls.clear();
				ls.add("!baction_" + s);
				ls.addAll(reachable);
				f.addClause(ls);

				f.addClause(List.of("!jump_" + s, "jump_" + i)); // Jumping somewhere
			}

			// Rest of preconditions and effects of ball movement actions
			if (!lwall.contains(j) && !lwall2.contains(j)) {
				for (int i = 0; i < nSteps; ++i) {
					String t = "_" + i;
					String t1 = "_" + (i + 1);
					String lj = "_" + j;
					String ljn = "_" + next.get(d + j);
					String ljnn = "_" + next2.get(d + j);

					ls.clear();
					ls.add("push_" + d + lj + t); ls.add("roll_" + d + lj + t); ls.add("pop_" + d + lj + t);
					add2DIMACSFormula(implication2cnf(ls, List.of(d + lj + t)), f);
					ls.add("!" + d + lj + t);
					f.addClause(ls);

					f.addClause(List.of("!" + d + lj + t, "baction" + lj + t)); // Moving a ball
					f.addClause(List.of("!" + d + lj + t, "baction" + t));

					// Push
					add2DIMACSFormula(implication2cnf(List.of("push_" + d + lj + t), List.of("!ball" + ljn + t1, "push_ball_" + d + lj + t)), f);
					f.addClause(List.of("!push_ball_" + d + lj + t, "push_s_" + d + lj + t, "push_m_" + d + lj + t));
					add2DIMACSFormula(implication2cnf(List.of("push_s_" + d + lj + t), List.of("#bs" + ljn + t, "!#bm" + ljn + t, "!#bl" + ljn + t, "!#bs" + ljnn + t, "ball_m_l" + ljnn + t, "#bs" + ljnn + t1, "staym" + ljnn + t, "stayl" + ljnn + t)), f);
					add2DIMACSFormula(implication2cnf(List.of("push_m_" + d + lj + t), List.of("!#bs" + ljn + t, "#bm" + ljn + t, "!#bl" + ljn + t, "!#bs" + ljnn + t, "!#bm" + ljnn + t, "#bl" + ljnn + t, "!#bs" + ljnn + t1, "#bm" + ljnn + t1, "#bl" + ljnn + t1)), f);

					// Roll
					add2DIMACSFormula(implication2cnf(List.of("roll_" + d + lj + t), List.of("!ball" + ljn + t1, "!ball" + ljnn + t, "!#snow" + ljnn + t1, "roll_ball_" + d + lj + t)), f);
					f.addClause(List.of("!roll_ball_" + d + lj + t, "roll_s_" + d + lj + t, "roll_m_" + d + lj + t, "roll_l_" + d + lj + t));
					add2DIMACSFormula(implication2cnf(List.of("roll_s_" + d + lj + t), List.of("#bs" + ljn + t, "!#bm" + ljn + t, "!#bl" + ljn + t, "grow_s" + ljnn + t)), f);
					add2DIMACSFormula(implication2cnf(List.of("roll_m_" + d + lj + t), List.of("!#bs" + ljn + t, "#bm" + ljn + t, "!#bl" + ljn + t, "grow_m" + ljnn + t)), f);
					add2DIMACSFormula(implication2cnf(List.of("roll_l_" + d + lj + t), List.of("!#bs" + ljn + t, "!#bm" + ljn + t, "#bl" + ljn + t, "!#bs" + ljnn + t1, "!#bm" + ljnn + t1, "#bl" + ljnn + t1)), f);

					// Pop
					add2DIMACSFormula(implication2cnf(List.of("pop_" + d + lj + t), List.of("!ball" + ljnn + t, "!#snow" + ljnn + t1, "pop_ball_" + d + lj + t)), f);
					f.addClause(List.of("!pop_ball_" + d + lj + t, "pop_s_" + d + lj + t, "pop_m_" + d + lj + t));
					add2DIMACSFormula(implication2cnf(List.of("pop_s_" + d + lj + t), List.of("#bs" + ljn + t, "ball_m_l" + ljn + t, "staym" + ljn + t, "stayl" + ljn + t, "!#bs" + ljn + t1, "grow_s" + ljnn + t)), f);
					add2DIMACSFormula(implication2cnf(List.of("pop_m_" + d + lj + t), List.of("!#bs" + ljn + t, "#bm" + ljn + t, "#bl" + ljn + t, "!#bs" + ljn + t1, "!#bm" + ljn + t1, "#bl" + ljn + t1, "grow_m" + ljnn + t)), f);
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

    private static void frameAxioms(int nSteps, Set<Integer> l,  Map<String,Integer> next, Map<String,Integer> next2, int nBall, DIMACSFormula f) {
		LinkedList<String> ls = new LinkedList<>();
		for (int i = 0; i < nSteps; ++i) {
			String t = "_" + i; String t1 = "_" + (i + 1);

			for (Integer j : l) {
				// Snow axioms
				f.addClause(List.of("#snow_" + j + t, "!#snow_" + j + t1));
				f.addClause(List.of("!#snow_" + j + t, "#snow_" + j + t1, "#bs_" + j + t1, "#bm_" + j + t1, "#bl_" + j + t1));

				// Changes in balls require action
				List<String> s1 = surroundingActions(j, l, next, i);
				List<String> s2 = surroundingActions(j, l, next2, i);
				ls.clear();
				ls.add("!#bs_" + j + t); ls.add("#bs_" + j + t1); ls.addAll(s1);
				f.addClause(ls);
				ls.clear();
				ls.add("!#bm_" + j + t); ls.add("#bm_" + j + t1); ls.addAll(s1);
				f.addClause(ls);
				ls.clear();
				ls.add("!#bl_" + j + t); ls.add("#bl_" + j + t1); ls.addAll(s1);
				f.addClause(ls);
				if (!s2.isEmpty()) { // There exists some valid position at distance 2
					ls.clear();
					ls.add("#bs_" + j + t); ls.add("!#bs_" + j + t1); ls.addAll(s2);
					f.addClause(ls);
					ls.clear();
					ls.add("#bm_" + j + t); ls.add("!#bm_" + j + t1); ls.addAll(s2);
					f.addClause(ls);
					ls.clear();
					ls.add("#bl_" + j + t); ls.add("!#bl_" + j + t1); ls.addAll(s2);
					f.addClause(ls);
				}
				else { // No ball can appear
					f.addClause(List.of("#bs_" + j + t, "!#bs_" + j + t1));
					f.addClause(List.of("#bm_" + j + t, "!#bm_" + j + t1));
					f.addClause(List.of("#bl_" + j + t, "!#bl_" + j + t1));
				}

				// At most one ball can appear in a location at a time
				f.addClause(List.of("#bs_" + j + t, "!#bs_" + j + t1, "#bm_" + j + t, "!#bm_" + j + t1));
				f.addClause(List.of("#bs_" + j + t, "!#bs_" + j + t1, "#bl_" + j + t, "!#bl_" + j + t1));
				f.addClause(List.of("#bm_" + j + t, "!#bm_" + j + t1, "#bl_" + j + t, "!#bl_" + j + t1));

			}
		}

		for (int i = 1; i <= nSteps; ++i) {
			String t = "_" + i;
			String t0 = "_" + (i - 1);

			// Character at most in one location
			ls.clear();
			for (int j : l) {
				ls.add("#c_" + j + t);
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

	private static void invariants(int nSteps, int nSnowman, List<Integer> l, DIMACSFormula f) {
		// #small balls >= #snowman
		// #large balls <= #snowman
		for (int i = 1; i < nSteps; ++i) { // The invariant is useless in the start and end states
			for (List<String> c : seq_at_most(nSnowman, "#bl", l, i))
				f.addClause(c);
			for (List<String> c : seq_at_most(nSnowman * 2, "#bm", l, i)) // Redundant
				f.addClause(c);
			for (List<String> c : seq_at_least(nSnowman, "#bs", l, i))
				f.addClause(c);
		}
	}

    private static void goal(int nSteps, Set<Integer> validLocations, DIMACSFormula f) {
		for (Integer loc : validLocations) {
			f.addClause(List.of("!#bs_" + loc + "_" + nSteps, "#bm_" + loc + "_" + nSteps));
			f.addClause(List.of("#bs_" + loc + "_" + nSteps, "!#bm_" + loc + "_" + nSteps));

			f.addClause(List.of("!#bm_" + loc + "_" + nSteps, "#bl_" + loc + "_" + nSteps));
			f.addClause(List.of("#bm_" + loc + "_" + nSteps, "!#bl_" + loc + "_" + nSteps));
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
				if (grid[i][j] != '#' && grid[i][j] != 'x')
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

		Pair<Integer,String> p  = initialState(grid);
		int nSnowman = p.first;
		int nBall = nSnowman * 3;

		// Initial state to CNF
		String ini[] = p.second.split(" & ");
		for (String s : ini)
			f.addClause(List.of(s));

		// At most one action per location
		//		atMostOneAction(nSteps, l, f);

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
		for (int n = 1; n <= nBall; ++n)
			reachabilityConstraints(true, nSteps, "baction_pos", String.valueOf(n), l, next2, validNeighbours, f);

		// Auxiliary variables
		auxiliaryVariables(nSteps, l, f);

		// Action transitions
		actionTransitions(nSteps, nSnowman, l, ln, lnn, "n", next, next2, f);
		actionTransitions(nSteps, nSnowman, l, ls, lss, "s", next, next2, f);
		actionTransitions(nSteps, nSnowman, l, le, lee, "e", next, next2, f);
		actionTransitions(nSteps, nSnowman, l, lw, lww, "w", next, next2, f);

		// Useless jumps
		uselessJumps(nSteps, l, ln, lnn, ls, lss, le, lee, lw, lww, next, next2, validNeighbours, f);

		// Frame axioms
		frameAxioms(nSteps, l, next, next2, nBall, f);

		// Invariants
		invariants(nSteps, nSnowman, new ArrayList<>(l), f);

		// Goal
		goal(nSteps, l, f);

		f.print(out);
    }

}
