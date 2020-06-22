package hk.pragmatic.ignite.repository;

import org.springframework.data.cassandra.repository.MapIdCassandraRepository;
import org.springframework.stereotype.Repository;

@Repository("model-repository")
public interface ModelRepository extends MapIdCassandraRepository<Model> {
}
