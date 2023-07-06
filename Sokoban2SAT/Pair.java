/** @file Pair.java
    @brief A generic pair
*/

/** @class Pair
    @brief Generic pair
 */
public class Pair<S,T> {
    public S first;
    public T second;
    
	public Pair(S first, T second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public boolean equals(Object o) {
        boolean r = false;
        if (o != null && o instanceof Pair<?,?>) {
            Pair<?,?> p = (Pair<?,?>)o;
            r = this.first.equals(p.first) && this.second.equals(p.second);
        }
        return r;
    }

    @Override
    public int hashCode() {
        return this.first.hashCode() + this.second.hashCode();
    }

}
