package wav.demon;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * UniqueHashMap utilizes two HashMaps to force the keys and values of the Map to both always be unique. It uses the structure
 * of:
 * <ul>
 * <li>HashMap&lt;Key, Value&gt; keyMap
 * <li>HashMap&lt;Value, Key&gt; valueMap
 * </ul>
 * It works mostly the same way as a normal HashMap, but it requires twice as much memory, and changes to the maps must be
 * done through the UniqueHashMap's methods. For example, when using entrySet(), keySet(), or valueSet(), you <b>must</b>
 * not attempt to modify the sets, or your will break invariants in the map. The keyMap and valueMap must match at all
 * times, and attempting to modify those sets will modify one map and not the other.
 */
public class UniqueHashMap<K, V> {

    private HashMap<K, V> keyMap = new HashMap<>();
    private HashMap<V, K> valueMap = new HashMap<>();

    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for the key, the old value is replaced.
     *
     * @param k key with which the specified value is to be associated
     * @param v value to be associated with the specified key
     *
     * @return the previous value associated with key, or null if there was no mapping for key.
     * (A null return can also indicate that the map previously associated null with key.)
     */
    public V put(K k, V v) {
        if (valueMap.containsKey(v)) {
            throw new ValueNotUniqueException(v.toString() + " is not unique.");
        } else {
            V oldValue = keyMap.put(k, v);
            valueMap.remove(oldValue);
            valueMap.put(v, k);
            return oldValue;
        }
    }

    /**
     * Returns the key to which the specified value is mapped, or null if this map contains no mapping for the value.
     * <p>
     * More formally, if this map contains a mapping from a value v to a key k such that (value==null ? v==null : value.equals(v)),
     * then this method returns k; otherwise it returns null. (There can be at most one such mapping.)
     * <p>
     * A return value of null does not necessarily indicate that the map contains no mapping for the value;
     * it's also possible that the map explicitly maps the value to null. The containsValue operation may be used to distinguish these two cases.
     *
     * @param v the value whose associated key is to be returned
     * @return the key to which the specified value is mapped, or null if this map contains no mapping for the value
     */
    public K getKeyFromValue(V v) {
        return valueMap.get(v);
    }

    /**
     * Returns the value to which the specified key is mapped, or null if this map contains no mapping for the key.
     * <p>
     * More formally, if this map contains a mapping from a key k to a value v such that (key==null ? k==null : key.equals(k)),
     * then this method returns v; otherwise it returns null. (There can be at most one such mapping.)
     * <p>
     * A return value of null does not necessarily indicate that the map contains no mapping for the key;
     * it's also possible that the map explicitly maps the key to null. The containsKey operation may be used to distinguish these two cases.
     *
     * @param k the key whose associated value is to be returned
     *
     * @return the value to which the specified key is mapped, or null if this map contains no mapping for the key
     */
    public V getValueFromKey(K k) {
        return keyMap.get(k);
    }

    /**
     * Returns true if this map contains a mapping for the specified key.
     *
     * @param k The key whose presence in this map is to be tested
     *
     * @return true if this map contains a mapping for the specified key.
     */
    public boolean containsKey(K k) {
        return keyMap.containsKey(k);
    }

    /**
     * Returns true if this map contains a mapping for the specified value.
     *
     * @param v The value whose presence in this map is to be tested
     *
     * @return true if this map contains a mapping for the specified value.
     */
    public boolean containsValue(V v) {
        return valueMap.containsKey(v);
    }

    /**
     * Returns a {@link java.util.Set Set} view of the mappings contained in this map.
     * <p>
     * <b>This should only be used in a read-only fashion.</b>
     * <p>
     * Changes to the entrySet will break invariants in the map, and the map will no longer function properly, as the two
     * sub-hashmaps would no longer match. You can use this for iterating over the map, but to remove keys from the map
     * you must use {@link wav.demon.UniqueHashMap#removeKey(Object) removeKey(Object)}, or to remove values you must use
     * {@link wav.demon.UniqueHashMap#removeValue(Object) removeValue(Object)}.
     *
     * @return a set view of the mappings contained in this map
     */
    public Set<Map.Entry<K, V>> entrySet() {
        return keyMap.entrySet();
    }

    /**
     * Removes all of the mappings from this map. The map will be empty after this call returns.
     */
    public void clear() {
        keyMap.clear();
        valueMap.clear();
    }

    /**
     * Returns true if this map contains no key-value mappings.
     *
     * @return true if this map contains no key-value mappings
     */
    public boolean isEmpty() {
        return keyMap.isEmpty();
    }

    /**
     * Returns a {@link java.util.Set Set} view of the keys contained in this map.
     * <p>
     * <b>This should only be used in a read-only fashion.</b>
     * <p>
     * Changes to the keySet will break invariants in the map, and the map will no longer function properly, as the two
     * sub-hashmaps would no longer match. You can use this for iterating over the map, but to remove keys from the map
     * you must use {@link wav.demon.UniqueHashMap#removeKey(Object) removeKey(Object)}, or to remove values you must use
     * {@link wav.demon.UniqueHashMap#removeValue(Object) removeValue(Object)}.
     *
     * @return a set view of the keys contained in this map
     */
    public Set<K> keySet() {
        return keyMap.keySet();
    }

    /**
     * Returns a {@link java.util.Set Set} view of the values contained in this map.
     * <p>
     * <b>This should only be used in a read-only fashion.</b>
     * <p>
     * Changes to the valueSet will break invariants in the map, and the map will no longer function properly, as the two
     * sub-hashmaps would no longer match. You can use this for iterating over the map, but to remove keys from the map
     * you must use {@link wav.demon.UniqueHashMap#removeKey(Object) removeKey(Object)}, or to remove values you must use
     * {@link wav.demon.UniqueHashMap#removeValue(Object) removeValue(Object)}.
     *
     * @return a set view of the values contained in this map
     */
    public Set<V> valueSet() {
        return valueMap.keySet();
    }

    /**
     * Returns the number of key-value mappings in this map.
     *
     * @return the number of key-value mappings in this map
     */
    public int size() {
        return keyMap.size();
    }

    /**
     * Removes the mapping for the specified key from this map if present.
     *
     * @param k key whose mapping is to be removed from the map
     *
     * @return the previous value associated with key, or null if there was no mapping for key.
     * (A null return can also indicate that the map previously associated null with key.)
     */
    public V removeKey(K k) {
        V v = keyMap.remove(k);
        valueMap.remove(v);
        return v;
    }

    /**
     * Removes the mapping for the specified value from this map if present.
     *
     * @param v value whose mapping is to be removed from the map
     *
     * @return the previous key associated with value, or null if there was no mapping for value.
     * (A null return can also indicate that the map previously associated null with value.)
     */
    public K removeValue(V v) {
        K k = valueMap.remove(v);
        keyMap.remove(k);
        return k;
    }
}

class ValueNotUniqueException extends RuntimeException {
    public ValueNotUniqueException() {
        super();
    }

    public ValueNotUniqueException(String message) {
        super(message);
    }
}