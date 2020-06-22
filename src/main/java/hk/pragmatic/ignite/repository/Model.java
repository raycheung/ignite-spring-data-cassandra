package hk.pragmatic.ignite.repository;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.LocalDate;

@Table("models")
@Value
@Builder
@Accessors(fluent = true)
public class Model {

    @PrimaryKeyColumn(name = "unique_key", type = PrimaryKeyType.PARTITIONED)
    String key;

    @PrimaryKeyColumn(name = "date", type = PrimaryKeyType.PARTITIONED)
    LocalDate date;

    @Column("some_text")
    String someText;

    @Column("a_numeric")
    Integer aNumeric;

}
