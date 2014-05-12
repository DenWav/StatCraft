package wav.demon;

import com.avaje.ebean.validation.NotNull;

import javax.annotation.Nullable;
import java.util.*;

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
 *
 * @see java.util.HashMap
 */
public class UniqueHashMap<K, V> extends AbstractMap implements Map, Cloneable {

    private HashMap<K, V> keyMap = new HashMap<>();
    private HashMap<V, K> valueMap = new HashMap<>();

    private Class<K> keyType;
    private Class<V> valueType;

    /**
     * Use this method to create a UniqueHashMap, so that the types for the key and value are stored. This is for type-
     * checks later.
     *
     * @param keyType the type of Object to be used for the key, so String.class, for example, is valid.
     * @param valueType the type of Object to be used for the value.
     *
     * @param <K> The type of the key.
     * @param <V> The type of the value.
     *
     * @return a UniqueHashMap&lt;K, V&gt;
     */
    public static <K, V> UniqueHashMap create(Class<K> keyType, Class<V> valueType) {
        return new UniqueHashMap<>(keyType, valueType);
    }

    /**
     * Use this method to create a UniqueHashMap, so that the types for the key and value are stored. This is for type-
     * checks later. This has a third field where a map can be given, where the fields of the Map will be copied into
     * the UniqueHashMap.
     *
     * @param keyType the type of Object to be used for the key, so String.class, for example, is valid.
     * @param valueType the type of Object to be used for the value.
     * @param m the map whose fields should be copied into the UniqueHashMap
     *
     * @param <K> The type of the key.
     * @param <V> The type of the value.
     *
     * @return a UniqueHashMap&lt;K, V&gt; with fields copied from m
     */
    public static <K, V> UniqueHashMap create(Class<K> keyType, Class<V> valueType, Map<K, V> m) {
        UniqueHashMap<K, V> umap = new UniqueHashMap<>(keyType, valueType);
        umap.putAll(m);
        return umap;
    }

    /**
     * Private constructor to prevent use.
     */
    private UniqueHashMap(){}

    /**
     * Private constructor to be called by the #create(Class<K> keyType, Class<V> valueType) method.
     *
     * @param keyType the type of Object to be used for the key, so String.class, for example, is valid.
     * @param valueType the type of Object to be used for the value.
     */
    private UniqueHashMap(Class<K> keyType, Class<V> valueType) {
        this.keyType = keyType;
        this.valueType = valueType;
    }

    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for the key, the old value is replaced.
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     *
     * @return the previous value associated with key, or null if there was no mapping for key.
     * (A null return can also indicate that the map previously associated null with key.)
     */
    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    public V put(Object key, Object value) {
        // do a type check
        if ((key == null || this.keyType.isAssignableFrom(key.getClass())) &&
                (value == null || this.valueType.isAssignableFrom(value.getClass()))) {
            if (valueMap.containsKey(value)) {
                throw new ValueNotUniqueException(value == null ? "null" : value.toString() + " is not unique.");
            } else {
                // add the key-value pair to the map
                V oldValue = keyMap.put((K) key, (V) value);
                valueMap.remove(oldValue);
                valueMap.put((V) value, (K) key);
                return oldValue;
            }
        } else {
            return null;
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
     * @param value the value whose associated key is to be returned
     * @return the key to which the specified value is mapped, or null if this map contains no mapping for the value
     */
    @Nullable
    public K getKeyFromValue(V value) { return valueMap.get(value); }

    /**
     * Returns the value to which the specified key is mapped, or null if this map contains no mapping for the key.
     * <p>
     * More formally, if this map contains a mapping from a key k to a value v such that (key==null ? k==null : key.equals(k)),
     * then this method returns v; otherwise it returns null. (There can be at most one such mapping.)
     * <p>
     * A return value of null does not necessarily indicate that the map contains no mapping for the key;
     * it's also possible that the map explicitly maps the key to null. The containsKey operation may be used to distinguish these two cases.
     *
     * @param key the key whose associated value is to be returned
     *
     * @return the value to which the specified key is mapped, or null if this map contains no mapping for the key
     */
    @Nullable
    public V getValueFromKey(K key) { return keyMap.get(key); }

    /**
     * Returns the individual key-HashMap. This map has structure of HashMap&lt;Key, Value&gt;. This Map should only be
     * used in a read-only fashion, any changes to this HashMap that is not done through this parent class will cause the
     * map to no longer function properly.
     *
     * @return the internal HashMap&lt;Key, Value&gt;
     */
    @NotNull
    public HashMap<K, V> getKeyMap() { return keyMap; }

    /**
     * Returns the individual value-HashMap. This map has structure of HashMap&lt;Value, Key&gt;. This Map should only be
     * used in a read-only fashion, any changes to this HashMap that is not done through this parent class will cause the
     * map to no longer function properly.
     *
     * @return the internal HashMap&lt;Value, Key&gt;
     */
    @NotNull
    public HashMap<V, K> getValueMap() { return  valueMap; }

    /**
     * Returns true if this map contains a mapping for the specified key.
     *
     * @param key The key whose presence in this map is to be tested
     *
     * @return true if this map contains a mapping for the specified key.
     */
    @Override
    public boolean containsKey(Object key) {
        return (key == null || this.keyType.isAssignableFrom(key.getClass())) && keyMap.containsKey(key);
    }

    /**
     * Returns true if this map contains a mapping for the specified value.
     *
     * @param value The value whose presence in this map is to be tested
     *
     * @return true if this map contains a mapping for the specified value.
     */
    @Override
    public boolean containsValue(Object value) {
        return (value == null || this.valueType.isAssignableFrom(value.getClass())) && valueMap.containsKey(value);
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
    @Override
    @NotNull
    public Set<Map.Entry<K, V>> entrySet() { return keyMap.entrySet(); }

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
    @Override
    public boolean isEmpty() { return keyMap.isEmpty(); }

    /**
     * <b>This method is here to satisfy the Map interface, but it just calls {@link #getValueFromKey(K key) getValueFromKey()}
     * internally.</b>
     * <p>
     * Returns the value to which the specified key is mapped, or null if this map contains no mapping for the key.
     * <p>
     * More formally, if this map contains a mapping from a key k to a value v such that (key==null ? k==null : key.equals(k)),
     * then this method returns v; otherwise it returns null. (There can be at most one such mapping.)
     * <p>
     * A return value of null does not necessarily indicate that the map contains no mapping for the key;
     * it's also possible that the map explicitly maps the key to null. The containsKey operation may be used to distinguish these two cases.
     *
     * @param key the key whose associated value is to be returned
     *
     * @return the value to which the specified key is mapped, or null if this map contains no mapping for the key
     */
    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    public Object get(Object key) {
        // type check
        if (key == null || this.keyType.isAssignableFrom(key.getClass()))
            return getValueFromKey((K)key);
        else
            return null;
    }

    /**
     * <b>This method is here to satisfy the Map interface, but it just calls {@link #removeKey(K key) removeKey()}
     * internally.</b>
     * <p>
     * Removes the mapping for the specified key from this map if present.
     *
     * @param key key whose mapping is to be removed from the map
     *
     * @return the previous value associated with key, or null if there was no mapping for key.
     * (A null return can also indicate that the map previously associated null with key.)
     */
    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    public Object remove(Object key) {
        // type check
        if (key == null || this.keyType.isAssignableFrom(key.getClass()))
            return removeKey((K) key);
        else
            return null;
    }


    /**
     * Copies all of the mappings from the specified map to this map. These mappings will replace any mappings that this
     * map had for any of the keys currently in the specified map.
     *
     * @param m mappings to be stored in this map
     *
     * @throws java.lang.NullPointerException if the specified map is null
     * @throws java.lang.ClassCastException if the provided map's keys or values are of the wrong type.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void putAll(Map m) {
        // null check
        if (m == null) {
            throw new NullPointerException("Specified map is null!");
        } else {
            // loop through map and manually add the  entries to the map
            for (Object pair : m.entrySet()) {
                Object key = ((Entry) pair).getKey();
                Object value = ((Entry) pair).getValue();
                // do a type check
                if ((key == null || this.keyType.isAssignableFrom(key.getClass())) &&
                        (value == null || this.valueType.isAssignableFrom(value.getClass()))) {
                    put(key, value);
                } else {
                    throw new ClassCastException("Either the key or value type of the specified map is incorrect.");
                }
            }
        }
    }

    /**
     * Returns a {@link java.util.Set Set} view of the keys contained in this map.
     * <p>
     * <b>This should only be used in a read-only fashion.</b>
     * <p>
     * Changes to the keySet will break invariants in the map, and the map will no longer function properly, as the two
     * sub-hashmaps would no longer match. You can use this for iterating over the map, but to remove keys from the map
     * you must use {@link #removeKey(K) removeKey()}, or to remove values you must use
     * {@link #removeValue(V) removeValue()}.
     *
     * @return a set view of the keys contained in this map
     */
    @Override
    @NotNull
    public Set<K> keySet() { return keyMap.keySet(); }

    /**
     * Returns a {@link java.util.Set Set} view of the values contained in this map.
     * <p>
     * <b>This should only be used in a read-only fashion.</b>
     * <p>
     * Changes to the valueSet will break invariants in the map, and the map will no longer function properly, as the two
     * sub-hashmaps would no longer match. You can use this for iterating over the map, but to remove keys from the map
     * you must use {@link #removeKey(K) removeKey()}, or to remove values you must use
     * {@link #removeValue(V) removeValue()}.
     *
     * @return a set view of the values contained in this map
     */
    @NotNull
    public Set<V> valueSet() { return valueMap.keySet(); }

    /**
     * Returns a Collection view of the values contained in this map. The collection is backed by the map, so changes to
     * the map are reflected in the collection. The collection <b>does not support</b> element removal, this must only
     * be used in a read-only fashion. It does not support the add or addAll operations.
     *
     * @return a collection view of the values contained in this map
     */
    @Override
    @NotNull
    public Collection values() { return keyMap.values(); }

    /**
     * Returns a Collection view of the keys contained in this map. The collection is backed by the map, so changes to
     * the map are reflected in the collection. The collection <b>does not support</b> element removal, this must only
     * be used in a read-only fashion. It does not support the add or addAll operations.
     *
     * @return a collection view of the keys contained in this map
     */
    @NotNull
    public Collection keys() { return valueMap.values(); }

    /**
     * Returns the number of key-value mappings in this map.
     *
     * @return the number of key-value mappings in this map
     */
    @Override
    public int size() { return keyMap.size(); }

    /**
     * Removes the mapping for the specified key from this map if present.
     *
     * @param key key whose mapping is to be removed from the map
     *
     * @return the previous value associated with key, or null if there was no mapping for key.
     * (A null return can also indicate that the map previously associated null with key.)
     */
    @Nullable
    public V removeKey(K key) {
        V value = keyMap.remove(key);
        valueMap.remove(value);
        return value;
    }

    /**
     * Removes the mapping for the specified value from this map if present.
     *
     * @param value value whose mapping is to be removed from the map
     *
     * @return the previous key associated with value, or null if there was no mapping for value.
     * (A null return can also indicate that the map previously associated null with value.)
     */
    @Nullable
    public K removeValue(V value) {
        K key = valueMap.remove(value);
        keyMap.remove(key);
        return key;
    }

    /**
     * Returns a shallow copy of this HashMap instance: the keys and values themselves are not cloned.
     *
     * @return a shallow copy of this map
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object clone() {
        UniqueHashMap<K, V> result = null;
        try {
            result = (UniqueHashMap<K, V>) super.clone();
        } catch (CloneNotSupportedException e) {
            // nope
        }
        HashMap resKeyMap = (HashMap<K, V>)getKeyMap().clone();
        HashMap resValueMap = (HashMap<V, K>)getValueMap().clone();

        if (result != null) {
            result.getKeyMap().putAll(resKeyMap);
            result.getValueMap().putAll(resValueMap);
        }

        return result;
    }
}

class ValueNotUniqueException extends RuntimeException {
    public ValueNotUniqueException() { super(); }

    public ValueNotUniqueException(String message) { super(message); }
}
