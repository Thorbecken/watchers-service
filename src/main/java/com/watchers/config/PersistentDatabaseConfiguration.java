package com.watchers.config;

import org.apache.commons.dbcp.BasicDataSource;
import com.watchers.model.environment.World;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;

import javax.sql.DataSource;
import java.util.Objects;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.watchers.repository.postgres",
        entityManagerFactoryRef = "persistentDatabaseEntityManagerFactory",
        transactionManagerRef = "persistentDatabaseTransactionManager")
public class PersistentDatabaseConfiguration {

    @Bean
    @SuppressWarnings("WeakerAccess")
    @ConfigurationProperties("datasource.persistent")
    public DataSourceProperties persistentDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @SuppressWarnings("WeakerAccess")
    @ConfigurationProperties(value = "datasource.persistent.configuration")
    public DataSource persistentDataSource() {
        return persistentDataSourceProperties().initializeDataSourceBuilder()
                .type(BasicDataSource.class).build();
    }

    @Bean(name = "persistentDatabaseEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean persistentDatabaseEntityManagerFactory(
            EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(persistentDataSource())
                .packages(World.class)
                .build();
    }

    @Bean
    public PlatformTransactionManager persistentTransactionManager(
            final @Qualifier("persistentDatabaseEntityManagerFactory") LocalContainerEntityManagerFactoryBean persistentDatabaseEntityManagerFactory) {
        return new JpaTransactionManager(Objects.requireNonNull(persistentDatabaseEntityManagerFactory.getObject()));
    }
}