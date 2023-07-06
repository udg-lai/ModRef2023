/**
 * @class snowman2sat_reachability_counting_cnf_invariants
 * @author Miquel Bofill
 * @version 1.1
 * @date 2023-01-02
 * @brief Translation of "A good snowman is hard to build" instance to CNF, with reachability (without move actions), ensuring acyclicity by counting neighbours in path, and with ball invariants using sequential counters
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

public class snowman2sat_reachability_counting_cnf_invariants {

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
					s += "#s_" + loc + "_0 & ";
					s += "!#bs_" + loc + "_0 & ";
					s += "!#bm_" + loc + "_0 & ";
					s += "!#bl_" + loc + "_0 & ";
					break;
				case 'q':
					nChar++;
					s += "#c_" + loc + "_0 & ";
					s += "!#s_" + loc + "_0 & ";
					s += "!#bs_" + loc + "_0 & ";
					s += "!#bm_" + loc + "_0 & ";
					s += "!#bl_" + loc + "_0 & ";
					break;
				case '1':
					nBall++; nSmall++;
					s += "!#c_" + loc + "_0 & ";
					s += "!#s_" + loc + "_0 & ";
					s += "#bs_" + loc + "_0 & ";
					s += "!#bm_" + loc + "_0 & ";
					s += "!#bl_" + loc + "_0 & ";
					break;
				case '2':
					nBall++;
					s += "!#c_" + loc + "_0 & ";
					s += "!#s_" + loc + "_0 & ";
					s += "!#bs_" + loc + "_0 & ";
					s += "#bm_" + loc + "_0 & ";
					s += "!#bl_" + loc + "_0 & ";
					break;
				case '3':
					nBall += 2; nSmall++;
					s += "!#c_" + loc + "_0 & ";
					s += "!#s_" + loc + "_0 & ";
					s += "#bs_" + loc + "_0 & ";
					s += "#bm_" + loc + "_0 & ";
					s += "!#bl_" + loc + "_0 & ";
					break;
				case '4':
					nBall++; nLarge++;
					s += "!#c_" + loc + "_0 & ";
					s += "!#s_" + loc + "_0 & ";
					s += "!#bs_" + loc + "_0 & ";
					s += "!#bm_" + loc + "_0 & ";
					s += "#bl_" + loc + "_0 & ";
					break;
				case '5':
					nBall += 2; nSmall++; nLarge++;
					s += "!#c_" + loc + "_0 & ";
					s += "!#s_" + loc + "_0 & ";
					s += "#bs_" + loc + "_0 & ";
					s += "!#bm_" + loc + "_0 & ";
					s += "#bl_" + loc + "_0 & ";
					break;
				case '6':
					nBall += 2; nLarge++;
					s += "!#c_" + loc + "_0 & ";
					s += "!#s_" + loc + "_0 & ";
					s += "!#bs_" + loc + "_0 & ";
					s += "#bm_" + loc + "_0 & ";
					s += "#bl_" + loc + "_0 & ";
					break;
				case '7':
					nBall += 3; nSmall++; nLarge++;
					s += "!#c_" + loc + "_0 & ";
					s += "!#s_" + loc + "_0 & ";
					s += "#bs_" + loc + "_0 & ";
					s += "#bm_" + loc + "_0 & ";
					s += "#bl_" + loc + "_0 & ";
					break;
				case '\'':
					s += "!#c_" + loc + "_0 & ";
					s += "!#s_" + loc + "_0 & ";
					s += "!#bs_" + loc + "_0 & ";
					s += "!#bm_" + loc + "_0 & ";
					s += "!#bl_" + loc + "_0 & ";
					break;
				case '.':
					s += "!#c_" + loc + "_0 & ";
					s += "#s_" + loc + "_0 & ";
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
	private static List<List<String>> binomial_at_most(int k, String var, List<Integer> indexes, Integer time) {
		return binomial_at_most(k, var, indexes, time, false);
	}

	// @returns "At-Least-k(var_{i_1}^time, ..., var_{i_n}^time)" for i_1, ..., i_n in indexes
	private static List<List<String>> binomial_at_least(int k, String var, List<Integer> indexes, Integer time) {
		return binomial_at_most(indexes.size() - k, var, indexes, time, true);
	}

	// @returns list of lists of literals (binomial encoding)
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

	private static void reachabilityConstraints(PrintStream out, int nSteps, Set<Integer> validLocations, Map<Integer, List<Integer>> validNeighbours, DIMACSFormula f) {

		for (Integer l : validLocations) {
			List<Integer> neighbours = validNeighbours.get(l);
			for (int t = 0; t < nSteps; ++t) {
				int t1 = t + 1;

				// Balls not in path
				f.addClause(List.of(literal("!ball", l, t), literal("!p", l, t1)));

				// OLD
				// Character always next to a ball: unnecessary, gets worse (already covered by actions)
				// out.print("(!c_" + l + "_" + t1);
				// for (Integer l1 : neighbours)
				// 	out.print(" | bs_" + l1 + "_" + t + " | bm_" + l1 + "_" + t + " | bl_" + l1 + "_" + t);
				// out.print(") & ");

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
				// Binomial encoding best suited for small at-most-k
				for (List<String> succedent : binomial_at_least(1, "p", neighbours, t1)) {
					clause.clear();
					clause.addAll(antecedent1);
					clause.addAll(succedent);
					f.addClause(clause);
					clause.clear();
					clause.addAll(antecedent2);
					clause.addAll(succedent);
					f.addClause(clause);
				}
				if (neighbours.size() > 1)
					for (List<String> succedent : binomial_at_most(1, "p", neighbours, t1)) {
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
					for (List<String> succedent : binomial_at_least(2, "p", neighbours, t1)) {
						clause.clear();
						clause.addAll(antecedent1);
						clause.addAll(succedent);
						f.addClause(clause);
					}
					if (neighbours.size() > 2)
						for (List<String> succedent : binomial_at_most(2, "p", neighbours, t1)) {
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
		for (int i = 0; i < nSteps; ++i) {
			String t = "_" + i; String t1 = "_" + (i + 1);

			for (Integer j : l) {
				String lj = "_" + j;

				f.addClause(List.of("!ball" + lj + t, "#bs" + lj + t, "#bm" + lj + t, "#bl" + lj + t));
				add2DIMACSFormula(implication2cnf(List.of("#bs" + lj + t, "#bm" + lj + t, "#bl" + lj + t), List.of("ball" + lj + t)), f);

				f.addClause(List.of("!ball_m_l" + lj + t, "#bm" + lj + t, "#bl" + lj + t));
				add2DIMACSFormula(implication2cnf(List.of("#bm" + lj + t, "#bl" + lj + t), List.of("ball_m_l" + lj + t)), f); // Unnecessary but helping

				f.addClause(List.of("!grow_s" + lj + t, "#s" + lj + t, "#bs" + lj + t1));
				f.addClause(List.of("!grow_s" + lj + t, "!#s" + lj + t, "#bm" + lj + t1));

				f.addClause(List.of("!grow_m" + lj + t, "#s" + lj + t, "#bm" + lj + t1));
				f.addClause(List.of("!grow_m" + lj + t, "!#s" + lj + t, "#bl" + lj + t1));
			}
		}
	}

	private static void actionTransitions(int nSteps, int nSnowman, Set<Integer> l, Set<Integer> lwall, Set<Integer> lwall2, String d, Map<String,Integer> next, Map<String,Integer> next2, DIMACSFormula f) {
		for (int i = 0; i < nSteps; ++i) {
			String t = "_" + i; String t1 = "_" + (i + 1);

			// No action allowed
			for (Integer j : lwall)
				f.addClause(List.of("!#c_" + j + t1, "!" + d + t));

			// No action allowed
			for (Integer j : lwall2)
				if (!lwall.contains(j))
					f.addClause(List.of("!#c_" + j + t1, "!" + d + t));

			for (Integer j : l)
				if (!lwall.contains(j) && !lwall2.contains(j)) {
					String lj = "_" + j;
					String ljn = "_" + next.get(d + j);
					String ljnn = "_" + next2.get(d + j);
					f.addClause(List.of("!#c" + lj + t1, "!" + d + t, "push_" + d + lj + t, "roll_" + d + lj + t, "pop_" + d + lj + t));
					f.addClause(List.of("!push_" + d + lj + t, "push_s_" + d + lj + t, "push_m_" + d + lj + t));
					add2DIMACSFormula(implication2cnf(List.of("push_s_" + d + lj + t), List.of("#bs" + ljn + t, "!#bm" + ljn + t, "!#bl" + ljn + t, "!#bs" + ljnn + t, "ball_m_l" + ljnn + t, "!#bs" + ljn + t1, "#bs" + ljnn + t1)), f);
					add2DIMACSFormula(implication2cnf(List.of("push_m_" + d + lj + t), List.of("!#bs" + ljn + t, "#bm" + ljn + t, "!#bl" + ljn + t, "!#bs" + ljnn + t, "!#bm" + ljnn + t, "#bl" + ljnn + t, "!#bm" + ljn + t1, "#bm" + ljnn + t1)), f);
					add2DIMACSFormula(implication2cnf(List.of("roll_" + d + lj + t), List.of("!ball" + ljnn + t, "!#s" + ljnn + t1, "roll_ball_" + d + lj + t)), f);
					f.addClause(List.of("!roll_ball_" + d + lj + t, "roll_s_" + d + lj + t, "roll_m_" + d + lj + t, "roll_l_" + d + lj + t));
					add2DIMACSFormula(implication2cnf(List.of("roll_s_" + d + lj + t), List.of("#bs" + ljn + t, "!#bm" + ljn + t, "!#bl" + ljn + t, "!#bs" + ljn + t1, "grow_s" + ljnn + t)), f);
					add2DIMACSFormula(implication2cnf(List.of("roll_m_" + d + lj + t), List.of("!#bs" + ljn + t, "#bm" + ljn + t, "!#bl" + ljn + t, "!#bm" + ljn + t1, "grow_m" + ljnn + t)), f);
					add2DIMACSFormula(implication2cnf(List.of("roll_l_" + d + lj + t), List.of("!#bs" + ljn + t, "!#bm" + ljn + t, "#bl" + ljn + t, "!#bl" + ljn + t1, "#bl" + ljnn + t1)), f);
					add2DIMACSFormula(implication2cnf(List.of("pop_" + d + lj + t), List.of("!ball" + ljnn + t, "!#s" + ljnn + t1, "pop_ball_" + d + lj + t)), f);
					f.addClause(List.of("!pop_ball_" + d + lj + t, "pop_s_" + d + lj + t, "pop_m_" + d + lj + t));
					add2DIMACSFormula(implication2cnf(List.of("pop_s_" + d + lj + t), List.of("#bs" + ljn + t, "ball_m_l" + ljn + t, "!#bs" + ljn + t1, "grow_s" + ljnn + t)), f);
					add2DIMACSFormula(implication2cnf(List.of("pop_m_" + d + lj + t), List.of("!#bs" + ljn + t, "#bm" + ljn + t, "#bl" + ljn + t, "!#bm" + ljn + t1, "grow_m" + ljnn + t)), f);
				}
		}
	}

    private static void frameAxioms(int nSteps, Set<Integer> l, DIMACSFormula f) {
		for (int i = 0; i < nSteps; ++i) {
			String t = "_" + i; String t1 = "_" + (i + 1);

			for (Integer j : l) {
				f.addClause(List.of("#s_" + j + t, "!#s_" + j + t1));
				f.addClause(List.of("!#s_" + j + t, "#s_" + j + t1, "#bs_" + j + t1, "#bm_" + j + t1, "#bl_" + j + t1));

				// At most one ball can appear in a location at a time
				f.addClause(List.of("#bs_" + j + t, "!#bs_" + j + t1, "#bm_" + j + t, "!#bm_" + j + t1));
				f.addClause(List.of("#bs_" + j + t, "!#bs_" + j + t1, "#bl_" + j + t, "!#bl_" + j + t1));
				f.addClause(List.of("#bm_" + j + t, "!#bm_" + j + t1, "#bl_" + j + t, "!#bl_" + j + t1));
			}
		}

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

				// Auxiliary variables
				if (jo != null && l.contains(jo)) {
					f.addClause(List.of("!emerge_m_" + d + lj + t, "#bs" + ljo + t, "#bm" + ljo + t));
					f.addClause(List.of("!emerge_m_" + d + lj + t, "#bs" + ljo + t, "!#s" + lj + t));

					f.addClause(List.of("!emerge_m_" + d + lj + t, "!#bs" + ljo + t, "#s" + lj + t));

					f.addClause(List.of("!emerge_l_" + d + lj + t, "#bm" + ljo + t, "!#bs" + ljo + t));
					f.addClause(List.of("!emerge_l_" + d + lj + t, "#bm" + ljo + t, "#bl" + ljo + t));

					f.addClause(List.of("!emerge_l_" + d + lj + t, "!#bm" + ljo + t, "!#bs" + ljo + t));
					f.addClause(List.of("!emerge_l_" + d + lj + t, "!#bm" + ljo + t, "#s" + lj + t));
				}

				// Axioms

				if (jo != null && l.contains(jo)) {

					f.addClause(List.of("!#bs" + lj + t, "#bs" + lj + t1, "!" + d + t, "#c" + ljo + t1));

					f.addClause(List.of("!#bm" + lj + t, "#bm" + lj + t1, "!" + d + t, "#c" + ljo + t1));
					f.addClause(List.of("!#bm" + lj + t, "#bm" + lj + t1, "!" + d + t, "!#bs" + lj + t));

					f.addClause(List.of("!#bl" + lj + t, "#bl" + lj + t1, "!" + d + t, "#c" + ljo + t1));
					f.addClause(List.of("!#bl" + lj + t, "#bl" + lj + t1, "!" + d + t, "!#bs" + lj + t));
					f.addClause(List.of("!#bl" + lj + t, "#bl" + lj + t1, "!" + d + t, "!#bm" + lj + t));

					if (joo != null && l.contains(joo)) {

						f.addClause(List.of("#bs" + lj + t, "!#bs" + lj + t1, "!" + d + t, "#c" + ljoo + t1));
						f.addClause(List.of("#bs" + lj + t, "!#bs" + lj + t1, "!" + d + t, "#bs" + ljo + t));

						f.addClause(List.of("#bm" + lj + t, "!#bm" + lj + t1, "!" + d + t, "#c" + ljoo + t1));
						f.addClause(List.of("#bm" + lj + t, "!#bm" + lj + t1, "!" + d + t, "emerge_m_" + d + lj + t));

						f.addClause(List.of("#bl" + lj + t, "!#bl" + lj + t1, "!" + d + t, "#c" + ljoo + t1));
						f.addClause(List.of("#bl" + lj + t, "!#bl" + lj + t1, "!" + d + t, "emerge_l_" + d + lj + t));

					}
					else {

						f.addClause(List.of("#bs" + lj + t, "!#bs" + lj + t1, "!" + d + t));
						f.addClause(List.of("#bm" + lj + t, "!#bm" + lj + t1, "!" + d + t));
						f.addClause(List.of("#bl" + lj + t, "!#bl" + lj + t1, "!" + d + t));

					}
				}
				else {

					f.addClause(List.of("!#bs" + lj + t, "#bs" + lj + t1, "!" + d + t));
					f.addClause(List.of("!#bm" + lj + t, "#bm" + lj + t1, "!" + d + t));
					f.addClause(List.of("!#bl" + lj + t, "#bl" + lj + t1, "!" + d + t));
					f.addClause(List.of("#bs" + lj + t, "!#bs" + lj + t1, "!" + d + t));
					f.addClause(List.of("#bm" + lj + t, "!#bm" + lj + t1, "!" + d + t));
					f.addClause(List.of("#bl" + lj + t, "!#bl" + lj + t1, "!" + d + t));

				}
			}
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

		// Exactly one action per time step
		exactlyOneAction(nSteps, f);

		// Reachability constraints
		Map<Integer,List<Integer>> validNeighbours = computeValidNeighbours(l, next);
		reachabilityConstraints(out, nSteps, l, validNeighbours, f);

		// Auxiliary variables
		auxiliaryVariables(nSteps, l, f);

		// Action transitions
		actionTransitions(nSteps, nSnowman, l, ln, lnn, "n", next, next2, f);
		actionTransitions(nSteps, nSnowman, l, ls, lss, "s", next, next2, f);
		actionTransitions(nSteps, nSnowman, l, le, lee, "e", next, next2, f);
		actionTransitions(nSteps, nSnowman, l, lw, lww, "w", next, next2, f);

		// Frame axioms
		frameAxioms(nSteps, l, f);
		frameAxioms(nSteps, l, "n", next, next2, f);
		frameAxioms(nSteps, l, "s", next, next2, f);
		frameAxioms(nSteps, l, "e", next, next2, f);
		frameAxioms(nSteps, l, "w", next, next2, f);

		// Invariants
		invariants(nSteps, nSnowman, new ArrayList<>(l), f);

		// Goal
		goal(nSteps, l, f);

		f.print(out);
    }

}
