package infrastructure;

/**
 * Created by Ciprian on 12/19/2015.
 */
public class Pair<K, V> {

    private final K element0;
    private final V element1;

    public Pair(K element0, V element1) {
        this.element0 = element0;
        this.element1 = element1;
    }

    public static <K, V> Pair<K, V> createPair(K element0, V element1) {
        return new Pair<K, V>(element0, element1);
    }

    public K getElement0() {
        return element0;
    }

    public V getElement1() {
        return element1;
    }

}