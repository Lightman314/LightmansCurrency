package io.github.lightman314.lightmanscurrency.util;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class MapAccessor<K,V> implements Map<K,V> {

    private final Map<K,V> map;
    public MapAccessor(Map<K,V> map) { this.map = map; }

    @Override
    public int size() { return this.map.size(); }
    @Override
    public boolean isEmpty() { return this.map.isEmpty(); }
    @Override
    public boolean containsKey(Object key) { return this.map.containsKey(key); }
    @Override
    public boolean containsValue(Object value) { return this.map.containsValue(value); }
    @Override
    public V get(Object key) { return this.map.get(key); }
    @Nullable
    @Override
    public V put(K key, V value) { throw new UnsupportedOperationException("Cannot put entries into this map!"); }
    @Override
    public V remove(Object key) { throw new UnsupportedOperationException("Cannot remove entries from this map!"); }
    @Override
    public void putAll(Map<? extends K, ? extends V> m) { throw new UnsupportedOperationException("Cannot put entries into this map!"); }
    @Override
    public void clear() { throw new UnsupportedOperationException("Cannot remove entries from this map!"); }
    @Override
    public Set<K> keySet() { return this.map.keySet(); }
    @Override
    public Collection<V> values() { return this.map.values(); }
    @Override
    public Set<Entry<K, V>> entrySet() { return this.map.entrySet(); }
}
