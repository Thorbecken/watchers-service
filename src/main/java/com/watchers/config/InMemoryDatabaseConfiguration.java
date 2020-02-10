package com.watchers.config;

import org.apache.commons.dbcp.BasicDataSource;
import com.watchers.model.environment.World;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Objects;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.watchers.repository.inMemory",
        entityManagerFactoryRef = "inmemoryDatabaseEntityManagerFactory",
        transactionManagerRef = "inmemoryDatabaseTransactionManager")
@EnableTransactionManagement
public class InMemoryDatabaseConfiguration {

    @Bean
    @Primary
    @SuppressWarnings("WeakerAccess")
    @ConfigurationProperties("datasource.h2")
    public DataSourceProperties inMemoryDataSourceProperties() {
        return new DataSourceProperties();
    }


    @Bean
    @Primary
    @SuppressWarnings("WeakerAccess")
    @ConfigurationProperties("datasource.h2.configuration")
    public DataSource inMemoryDataSource() {
        return inMemoryDataSourceProperties().initializeDataSourceBuilder()
                .type(BasicDataSource.class).build();
    }

    @Primary
    @Bean(name = "inmemoryDatabaseEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean inmemoryDatabaseEntityManagerFactory(EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(inMemoryDataSource())
                .packages(World.class)
                .build();
    }

    @Primary
    @Bean
    public PlatformTransactionManager inmemoryDatabaseTransactionManager(
            final @Qualifier("inmemoryDatabaseEntityManagerFactory") LocalContainerEntityManagerFactoryBean inmemoryDatabaseEntityManager) {
        return new JpaTransactionManager(Objects.requireNonNull(inmemoryDatabaseEntityManager.getObject()));
    }
}
