package hk.pragmatic.ignite;

import org.apache.ignite.resources.SpringResource;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import javax.cache.configuration.Factory;

public class CrudRepositoryCacheStoreFactory<K, V> implements Factory<CrudRepositoryCacheStore<K, V>> {

    private final Class<?> repositoryClass;

    @SpringResource(resourceName = "repository-factory")
    @SuppressWarnings("unused")
    private transient RepositoryFactorySupport factorySupport;

    CrudRepositoryCacheStoreFactory(Class<?> repositoryClass) {
        this.repositoryClass = repositoryClass;
    }

    @Override
    public CrudRepositoryCacheStore<K, V> create() {
        //noinspection unchecked
        return new CrudRepositoryCacheStore<>((CrudRepository<V, K>) factorySupport.getRepository(repositoryClass));
    }
}
