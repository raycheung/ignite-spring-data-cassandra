# Apache Ignite with Spring Data for Apache Cassandra

This is a simple straight-forward integration example to leverage 
[Spring Data for Apache Cassandra](https://spring.io/projects/spring-data-cassandra) as a read-through & write-through 
cache store backend for the high-performance [Apache Ignite](https://ignite.apache.org/).

### Why not the [ignite-cassandra-store](https://apacheignite-mix.readme.io/docs/ignite-with-apache-cassandra)?
I found the implementation contains a lot of boilerplate code which goes all the way to repeat the implementation of CQL
query construction to Cassandra session management etc. -- which are all mostly handled with a typical Spring Boot 
application. If you would build the application with Spring Boot, you end up duplicating quite some configuration code.

This integration example demonstrates how to build the Apache Ignite cache store on top of the Spring Data offerings, 
which eliminates the duplication.

### Important note
There is a subtle requirement for the Apache Ignite cache store factory interface, which is `Serializable`. With that, I 
found some Spring beans cannot be directly injected with the `@SpringResource` annotation provided by Apache Ignite.
