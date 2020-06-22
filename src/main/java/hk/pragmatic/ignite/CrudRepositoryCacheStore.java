package hk.pragmatic.ignite;

import org.apache.ignite.cache.store.CacheStore;
import org.apache.ignite.cache.store.CacheStoreSession;
import org.apache.ignite.lang.IgniteBiInClosure;
import org.apache.ignite.resources.CacheStoreSessionResource;
import org.springframework.data.repository.CrudRepository;

import javax.cache.Cache;
import javax.cache.integration.CacheLoaderException;
import javax.cache.integration.CacheWriterException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class CrudRepositoryCacheStore<K, V> implements CacheStore<K, V> {
    private static final String PENDING_MUTATIONS = "CASSANDRA_PENDING_MUTATIONS";

    private final CrudRepository<V, K> repository;

    @CacheStoreSessionResource
    @SuppressWarnings("unused")
    private CacheStoreSession cacheStoreSession;

    public CrudRepositoryCacheStore(CrudRepository<V, K> repository) {
        this.repository = repository;
    }

    @Override
    public void loadCache(IgniteBiInClosure<K, V> clo, Object... args) throws CacheLoaderException {
        // TODO
    }

    @Override
    @Deprecated
    public void sessionEnd(boolean commit) throws CacheWriterException {
        if (commit && cacheStoreSession.isWithinTransaction()) {
            List<Runnable> pended = pendingMutations();
            pended.forEach(Runnable::run);
            pended.clear();
        }
    }


    @Override
    public V load(K k) throws CacheLoaderException {
        return repository.findById(k).orElse(null);
    }

    @Override
    public Map<K, V> loadAll(Iterable<? extends K> iterable) throws CacheLoaderException {
        return StreamSupport.stream(iterable.spliterator(), false)
                .collect(Collectors.toMap(k -> (K) k, this::load));
    }

    @Override
    public void write(Cache.Entry<? extends K, ? extends V> entry) throws CacheWriterException {
        if (cacheStoreSession.isWithinTransaction()) {
            pend(() -> doWrite(entry));
            return;
        }
        doWrite(entry);
    }

    private List<Runnable> pendingMutations() {
        //noinspection unchecked
        return (List<Runnable>) cacheStoreSession.properties().get(PENDING_MUTATIONS);
    }

    private void pend(Runnable runnable) {
        List<Runnable> pended = pendingMutations();

        if (pended == null) {
            pended = new LinkedList<>();
            cacheStoreSession.properties().put(PENDING_MUTATIONS, pended);
        }

        pended.add(runnable);
    }

    @Override
    public void writeAll(Collection<Cache.Entry<? extends K, ? extends V>> collection) throws CacheWriterException {
        collection.forEach(this::write);
    }

    private void doWrite(Cache.Entry<? extends K, ? extends V> entry) {
        repository.save(entry.getValue());
    }

    @Override
    public void delete(Object key) throws CacheWriterException {
        if (cacheStoreSession.isWithinTransaction()) {
            pend(() -> doDelete(key));
            return;
        }
        doDelete(key);
    }

    private void doDelete(Object key) {
        //noinspection unchecked
        repository.deleteById((K) key);
    }

    @Override
    public void deleteAll(Collection<?> collection) throws CacheWriterException {
        collection.forEach(this::delete);
    }

}
