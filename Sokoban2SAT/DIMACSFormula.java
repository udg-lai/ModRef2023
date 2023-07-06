/**
 * @class DIMACSFormula
 * @author Miquel Bofill
 * @version 1.1
 * @date 2022-11-15
 * @brief A DIMACS formula with atom names
*/

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Iterator;
import java.io.PrintStream;

public class DIMACSFormula {

    private ArrayList<String> _names; ///< atom names (position 0 not used)
    private HashMap<String,Integer> _numbers; ///< variable number for each name
    private ArrayList<LinkedList<Integer>> _formula; ///< DIMACS Formula

    public DIMACSFormula() {
        _names = new ArrayList<>();
        _names.add("void"); // Position 0 not used
        _numbers = new HashMap<>();
        _formula = new ArrayList<>();
    }

    /// @brief The name for a variable
    /// @returns The name for atom number \p i
    private String name(int i) {
        return _names.get(i);
    }

    /// @brief The number for a variable
    /// @returns The number for atom with name \p s
    private Integer number(String s) {
        return _numbers.get(s);
    }

    /// @brief Adds a clause
    /// @pre \p clause not empty and negated literals begin with !
    /// @post \p clause belongs to the formula
    public void addClause(List<String> clause) {
        if (clause != null && !clause.isEmpty()) {
            LinkedList<Integer> iclause = new LinkedList<>();
            for (String literal : clause) {
                int sign = 1;
                String atom = literal;
                if (literal.charAt(0) == '!') {
                    sign = -1;
                    atom = literal.substring(1);
                }
                Integer number = number(atom);
                if (number == null) {
                    // New atom
                    number = _names.size();
                    _numbers.put(atom, number);
                    _names.add(atom);
                }
                iclause.add(sign * number);
            }
            _formula.add(iclause);
        }
    }
    
    //CRISTINA: used when using optilog for incremental solving
    public ArrayList<LinkedList<Integer>> getClauses(){
    	return _formula;
    }
    public void clear(){
    	_formula.clear();
    }
    //end-CRISTINA

    // Too slow
    // Use print (see below)
    @Override
    public String toString() {
        String s = "";

        // Comments
        int i = 0;
        Iterator<String> it = _names.iterator();
        it.next(); // Skip position 0
        while (it.hasNext())
            s += "c " + (++i) + " " + it.next() + "\n";

        // p cnf <variables> <clauses>
        s += "p cnf " + i + " " + _formula.size() + "\n";

        // Clauses
        for (List<Integer> clause : _formula) {
            for (Integer literal : clause)
                s += literal + " ";
            s += "0\n";
        }

        return s;
    }

    public void print(PrintStream out) {
        // Comments
        int i = 0;
        Iterator<String> it = _names.iterator();
        it.next(); // Skip position 0
        while (it.hasNext())
            out.println("c " + (++i) + " " + it.next());

        // p cnf <variables> <clauses>
        out.println("p cnf " + i + " " + _formula.size());

        // Clauses
        for (List<Integer> clause : _formula) {
            for (Integer literal : clause)
                out.print(literal + " ");
            out.println("0");
        }

    }

}
