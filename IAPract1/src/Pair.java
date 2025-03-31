
/**
 * Class Name: Pair
 *
 * Description:
 * Creates a pair object, similar to the pair in c++
 *
 * @author Grupo_IA
 * @version 1.0
 */


public class Pair<U, V> {
    public final U first;
    public final V second;

    public Pair(U first, V second) {
        this.first = first;
        this.second = second;
    }

    public U getFirst() {
        return first;
    }

    public V getSecond() {
        return second;
    }
}
