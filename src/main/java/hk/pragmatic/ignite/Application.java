package hk.pragmatic.ignite;

import com.datastax.oss.driver.api.core.CqlSession;
import hk.pragmatic.ignite.repository.Model;
import hk.pragmatic.ignite.repository.ModelRepository;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.IgniteSpring;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.mapping.BasicMapId;
import org.springframework.data.cassandra.core.mapping.MapId;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;
import org.springframework.data.cassandra.repository.support.CassandraRepositoryFactory;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import java.time.LocalDate;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@EnableCassandraRepositories(basePackageClasses = hk.pragmatic.ignite.repository.ModelRepository.class)
public class Application {

    private static final String KEYSPACE_NAME = "ignite_cassandra";
    private static final String MODEL_CACHE = "ModelCache";

    @Bean
    CqlSession cqlSession() {
        return CqlSession.builder().withKeyspace(KEYSPACE_NAME).build();
    }

    @Bean
    CassandraTemplate cassandraTemplate(CqlSession cqlSession) {
        return new CassandraTemplate(cqlSession);
    }

    @Bean("repository-factory")
    RepositoryFactorySupport repositoryFactorySupport(CassandraTemplate cassandraTemplate) {
        return new CassandraRepositoryFactory(cassandraTemplate);
    }

    @Bean
    Ignite ignite(ApplicationContext ctx) throws IgniteCheckedException {
        final IgniteConfiguration cfg = new IgniteConfiguration();

        cfg.setCacheConfiguration(
                new CacheConfiguration<MapId, Model>(MODEL_CACHE)
                        .setReadThrough(true)
                        .setWriteThrough(true)
                        .setCacheStoreFactory(new CrudRepositoryCacheStoreFactory<>(ModelRepository.class)));

        return IgniteSpring.start(cfg, ctx);
    }

    public static void main(String[] args) {
        final ConfigurableApplicationContext app = SpringApplication.run(Application.class);
        final Ignite ignite = app.getBean(Ignite.class);

        final IgniteCache<MapId, Model> modelCache = ignite.cache(MODEL_CACHE);

        final MapId id = BasicMapId.id("key", "Marvel").with("date", LocalDate.of(2020, 6, 20));
        final Model val = Model.builder()
                .key((String) id.get("key"))
                .date((LocalDate) id.get("date"))
                .someText("Hello World!")
                .aNumeric(616)
                .build();
        modelCache.put(id, val);

        final Model model = modelCache.get(id);
        assert (model != null);
        assert (model.equals(val));

        Ignition.stopAll(true);
        System.exit(0);
    }

}
