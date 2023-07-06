/**
 * @class snowman2sat_cnf
 * @author Miquel Bofill and Cristina Borralleras
 * @version 1.3
 * @date 2023-01-02
 * @brief Translation of "A good snowman is hard to build" instance to CNF
*/

import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Set;
import java.util.TreeSet;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.PrintStream;

public class snowman2sat_cnf {

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
    
    /// @post Outputs a CNF for the the problem instance described by the input
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

	private static void actionTransitions(int nSteps, int nSnowman, Set<Integer> l, Set<Integer> lwall, Set<Integer> lwall2, String d, Map<String,Integer> next, Map<String,Integer> next2, DIMACSFormula f) {
		for (int i = 0; i < nSteps; ++i) {
			String t = "_" + i; String t1 = "_" + (i + 1);

			// Auxiliary variables
			for (Integer j : l) {
				String lj = "_" + j;

				f.addClause(List.of("!ball" + lj + t, "#bs" + lj + t, "#bm" + lj + t, "#bl" + lj + t));
				add2DIMACSFormula(implication2cnf(List.of("#bs" + lj + t, "#bm" + lj + t, "#bl" + lj + t), List.of("ball" + lj + t)), f);

				f.addClause(List.of("!ball_m_l" + lj + t, "#bm" + lj + t, "#bl" + lj + t));
				add2DIMACSFormula(implication2cnf(List.of("#bm" + lj + t, "#bl" + lj + t), List.of("ball_m_l" + lj + t)), f); // Unnecessary but helping

				if (!lwall.contains(j)) {
					String ljn = "_" + next.get(d + j);
					add2DIMACSFormula(implication2cnf(List.of("movec_" + d + lj + t), List.of("!#c" + lj + t1, "#c" + ljn + t1)), f);
					f.addClause(List.of("#c" + lj + t1, "!#c" + ljn + t1, "movec_" + d + lj + t)); // Unnecessary but helping
				}

				f.addClause(List.of("!grow_s" + lj + t, "#s" + lj + t, "#bs" + lj + t1));
				f.addClause(List.of("!grow_s" + lj + t, "!#s" + lj + t, "#bm" + lj + t1));

				f.addClause(List.of("!grow_m" + lj + t, "#s" + lj + t, "#bm" + lj + t1));
				f.addClause(List.of("!grow_m" + lj + t, "!#s" + lj + t, "#bl" + lj + t1));

			}

			// No action allowed
			for (Integer j : lwall)
				f.addClause(List.of("!#c_" + j + t, "!" + d + t));

			// Only move allowed
			for (Integer j : lwall2)
				if (!lwall.contains(j)) {
					String ln = "_" + next.get(d + j);
					f.addClause(List.of("!#c_" + j + t, "!" + d + t, "!#c_" + j + t1));
					f.addClause(List.of("!#c_" + j + t, "!" + d + t, "#c" + ln + t1));
					f.addClause(List.of("!#c_" + j + t, "!" + d + t, "!#bs" + ln + t));
					f.addClause(List.of("!#c_" + j + t, "!" + d + t, "!#bm" + ln + t));
					f.addClause(List.of("!#c_" + j + t, "!" + d + t, "!#bl" + ln + t));
				}

			for (Integer j : l)
				if (!lwall.contains(j) && !lwall2.contains(j)) {
					String lj = "_" + j;
					String ljn = "_" + next.get(d + j);
					String ljnn = "_" + next2.get(d + j);
					f.addClause(List.of("!#c" + lj + t, "!" + d + t, "move_" + d + lj + t, "push_" + d + lj + t, "roll_" + d + lj + t, "pop_" + d + lj + t));
					add2DIMACSFormula(implication2cnf(List.of("move_" + d + lj + t), List.of("movec_" + d + lj + t, "!ball" + ljn + t)), f);
					f.addClause(List.of("!push_" + d + lj + t, "movec_" + d + lj + t));
					f.addClause(List.of("!push_" + d + lj + t, "push_s_" + d + lj + t, "push_m_" + d + lj + t));
					add2DIMACSFormula(implication2cnf(List.of("push_s_" + d + lj + t), List.of("#bs" + ljn + t, "!#bm" + ljn + t, "!#bl" + ljn + t, "!#bs" + ljnn + t, "ball_m_l" + ljnn + t, "!#bs" + ljn + t1, "#bs" + ljnn + t1)), f);
					add2DIMACSFormula(implication2cnf(List.of("push_m_" + d + lj + t), List.of("!#bs" + ljn + t, "#bm" + ljn + t, "!#bl" + ljn + t, "!#bs" + ljnn + t, "!#bm" + ljnn + t, "#bl" + ljnn + t, "!#bm" + ljn + t1, "#bm" + ljnn + t1)), f);
					add2DIMACSFormula(implication2cnf(List.of("roll_" + d + lj + t), List.of("movec_" + d + lj + t, "!ball" + ljnn + t, "!#s" + ljnn + t1, "roll_ball_" + d + lj + t)), f);
					f.addClause(List.of("!roll_ball_" + d + lj + t, "roll_s_" + d + lj + t, "roll_m_" + d + lj + t, "roll_l_" + d + lj + t));
					add2DIMACSFormula(implication2cnf(List.of("roll_s_" + d + lj + t), List.of("#bs" + ljn + t, "!#bm" + ljn + t, "!#bl" + ljn + t, "!#bs" + ljn + t1, "grow_s" + ljnn + t)), f);
					add2DIMACSFormula(implication2cnf(List.of("roll_m_" + d + lj + t), List.of("!#bs" + ljn + t, "#bm" + ljn + t, "!#bl" + ljn + t, "!#bm" + ljn + t1, "grow_m" + ljnn + t)), f);
					add2DIMACSFormula(implication2cnf(List.of("roll_l_" + d + lj + t), List.of("!#bs" + ljn + t, "!#bm" + ljn + t, "#bl" + ljn + t, "!#bl" + ljn + t1, "#bl" + ljnn + t1)), f);
					add2DIMACSFormula(implication2cnf(List.of("pop_" + d + lj + t), List.of("#c" + lj + t1, "!ball" + ljnn + t, "!#s" + ljnn + t1, "pop_ball_" + d + lj + t)), f);
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
				Integer jn = next.get(d + j);
				Integer jo = inext(d.charAt(0), j, next);
				Integer joo = inext(d.charAt(0), j, next2);
				String lj = "_" + j;
				String ljn = "_" + jn;
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
				if (jn != null && l.contains(jn))
					f.addClause(List.of("!#c" + lj + t, "#c" + lj + t1, "!" + d + t, "#c" + ljn + t1));
				else
					f.addClause(List.of("!#c" + lj + t, "#c" + lj + t1, "!" + d + t));

				if (jo != null && l.contains(jo)) {

					f.addClause(List.of("#c" + lj + t, "!#c" + lj + t1, "!" + d + t, "#c" + ljo + t));

					f.addClause(List.of("!#bs" + lj + t, "#bs" + lj + t1, "!" + d + t, "#c" + ljo + t));

					f.addClause(List.of("!#bm" + lj + t, "#bm" + lj + t1, "!" + d + t, "#c" + ljo + t));
					f.addClause(List.of("!#bm" + lj + t, "#bm" + lj + t1, "!" + d + t, "!#bs" + lj + t));

					f.addClause(List.of("!#bl" + lj + t, "#bl" + lj + t1, "!" + d + t, "#c" + ljo + t));
					f.addClause(List.of("!#bl" + lj + t, "#bl" + lj + t1, "!" + d + t, "!#bs" + lj + t));
					f.addClause(List.of("!#bl" + lj + t, "#bl" + lj + t1, "!" + d + t, "!#bm" + lj + t));

					if (joo != null && l.contains(joo)) {

						f.addClause(List.of("#bs" + lj + t, "!#bs" + lj + t1, "!" + d + t, "#c" + ljoo + t));
						f.addClause(List.of("#bs" + lj + t, "!#bs" + lj + t1, "!" + d + t, "#bs" + ljo + t));

						f.addClause(List.of("#bm" + lj + t, "!#bm" + lj + t1, "!" + d + t, "#c" + ljoo + t));
						f.addClause(List.of("#bm" + lj + t, "!#bm" + lj + t1, "!" + d + t, "emerge_m_" + d + lj + t));

						f.addClause(List.of("#bl" + lj + t, "!#bl" + lj + t1, "!" + d + t, "#c" + ljoo + t));
						f.addClause(List.of("#bl" + lj + t, "!#bl" + lj + t1, "!" + d + t, "emerge_l_" + d + lj + t));

					}
					else {

						f.addClause(List.of("#bs" + lj + t, "!#bs" + lj + t1, "!" + d + t));
						f.addClause(List.of("#bm" + lj + t, "!#bm" + lj + t1, "!" + d + t));
						f.addClause(List.of("#bl" + lj + t, "!#bl" + lj + t1, "!" + d + t));

					}
				}
				else {

					f.addClause(List.of("#c" + lj + t, "!#c" + lj + t1, "!" + d + t));
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

		// Goal
		goal(nSteps, l, f);

		f.print(out);
    }

}
