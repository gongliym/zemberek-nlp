package zemberek.core.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple hashmap with integer keys and T values. implements open address linear probing
 * algorithm. <p> Constraints: - Supports int key values in range (Integer.MIN_VALUE..Integer.MAX_VALUE];
 * - Does not implement Map interface - Size can be max 1 << 29 - Does not support remove. - Does
 * not implement Iterable. - Class is not thread safe.
 */
public final class IntMap<T> {

  private static final int DEFAULT_INITIAL_CAPACITY = 4;
  /**
   * Capacity of the map is expanded when size reaches to capacity * LOAD_FACTOR. This value is
   * selected to fit max 5 elements to 8 and 10 elements to a 16 sized map.
   */
  private static final float LOAD_FACTOR = 0.65f;

  private static final int MAX_CAPACITY = 1 << 29;

  // Special value to mark empty cells.
  private static final int EMPTY = Integer.MIN_VALUE;

  // Backing arrays for keys and value references.
  private int[] keys;
  private T[] values;

  // Number of keys in the map = size of the map.
  private int keyCount;

  // When size reaches a threshold, backing arrays are expanded.
  private int threshold;

  /**
   * Map capacity is always a power of 2. With this property, integer modulo operation (key %
   * capacity) can be replaced with (key & (capacity - 1)) We keep (capacity - 1) value in this
   * variable.
   */
  private int modulo;

  public IntMap() {
    this(DEFAULT_INITIAL_CAPACITY);
  }

  /**
   * @param capacity initial internal array size. It must be a positive number. If value is not a
   * power of two, size will be the nearest larger power of two.
   */
  @SuppressWarnings("unchecked")
  public IntMap(int capacity) {
    capacity = adjustInitialCapacity(capacity);
    keys = new int[capacity];
    values = (T[]) new Object[keys.length];
    Arrays.fill(keys, EMPTY);
    modulo = keys.length - 1;
    threshold = (int) (capacity * LOAD_FACTOR);
  }

  private int adjustInitialCapacity(int capacity) {
    if (capacity < 1) {
      throw new IllegalArgumentException("Capacity must > 0: " + capacity);
    }
    long k = 1;
    while (k < capacity) {
      k <<= 1;
    }
    if (k > MAX_CAPACITY) {
      throw new IllegalArgumentException("Map too large: " + capacity);
    }
    return (int) k;
  }

  public int capacity() {
    return keys.length;
  }

  public int size() {
    return keyCount;
  }

  private void checkKey(int key) {
    if (key == EMPTY) {
      throw new IllegalArgumentException("Illegal key: " + key);
    }
  }

  public void put(int key, T value) {
    checkKey(key);
    if (keyCount == threshold) {
      expand();
    }
    int loc = locate(key);
    if (loc >= 0) {
      values[loc] = value;
    } else {
      loc = -loc - 1;
      keys[loc] = key;
      values[loc] = value;
      keyCount++;
    }
  }

  /**
   * @return The value {@code T} taht is mapped to given {@code key}. or  {@code null} If key does
   * not exist,
   * @throws IllegalArgumentException if key is {@code Integer.MIN_INT}
   */
  public T get(int key) {
    checkKey(key);
    int slot = key & modulo;
    // Test the lucky first shot.
    if (key == keys[slot]) {
      return values[slot];
    }
    // Continue linear probing otherwise
    while (true) {
      slot = (slot + 1) & modulo;
      final int t = keys[slot];
      if (t == key) {
        return values[slot];
      }
      if (t == EMPTY) {
        return null;
      }
    }
  }

  public boolean containsKey(int key) {
    return locate(key) >= 0;
  }

  /**
   * Returns the array of keys in the map.
   */
  public int[] getKeys() {
    int[] keyArray = new int[keyCount];
    int c = 0;
    for (int key : keys) {
      if (key != EMPTY) {
        keyArray[c++] = key;
      }
    }
    return keyArray;
  }

  /**
   * Returns the array of keys in the map.
   */
  public List<T> getValues() {
    List<T> result = new ArrayList<>();
    for (int i = 0; i < keys.length; i++) {
      if (keys[i] >= 0) {
        result.add(values[i]);
      }
    }
    return result;
  }

  private int locate(int key) {
    int slot = key & modulo;
    while (true) {
      final int k = keys[slot];
      // If slot is empty, return its location
      if (k == EMPTY) {
        return -slot - 1;
      }
      if (k == key) {
        return slot;
      }
      slot = (slot + 1) & modulo;
    }
  }

  private int newCapacity() {
    long size = (long) (keys.length * 2);
    if (keys.length > MAX_CAPACITY) {
      throw new RuntimeException("Map is too large.");
    }
    return (int) size;
  }

  /**
   * Expands backing arrays by doubling their capacity.
   */
  private void expand() {
    int capacity = newCapacity();
    IntMap<T> h = new IntMap<>(capacity);
    for (int i = 0; i < keys.length; i++) {
      if (keys[i] != EMPTY) {
        h.put(keys[i], values[i]);
      }
    }
    this.keys = h.keys;
    this.values = h.values;
    this.threshold = h.threshold;
    this.modulo = h.modulo;
  }
}