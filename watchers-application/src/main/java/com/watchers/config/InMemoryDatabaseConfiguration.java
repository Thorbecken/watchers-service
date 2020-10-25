package com.watchers.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Properties;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.watchers.repository.inmemory",
        entityManagerFactoryRef = "inmemoryDatabaseEntityManager",
        transactionManagerRef = "inmemoryDatabaseTransactionManager")
public class InMemoryDatabaseConfiguration {
    @Autowired
    private Environment env;

    public InMemoryDatabaseConfiguration() {
        super();
    }

    @Primary
    @Bean
    public LocalContainerEntityManagerFactoryBean inmemoryDatabaseEntityManager() {
        final LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(inmemoryDataSource());
        em.setPackagesToScan("com.watchers.model");

        final HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        final HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put("hibernate.hbm2ddl.auto", env.getProperty("spring.jpa.hibernate.ddl-auto"));
        properties.put("hibernate.dialect", env.getProperty("spring.jpa.database-platform"));
        properties.put("hibernate.jdbc.batch_size", env.getProperty("spring.jpa.properties.hibernate.jdbc.batch_size"));
        properties.put("hibernate.order_inserts", env.getProperty("spring.jpa.properties.hibernate.order_inserts"));
        properties.put("hibernate.order_updates", env.getProperty("spring.jpa.properties.hibernate.order_updates"));
        properties.put("hibernate.jdbc.batch_versioned_data", env.getProperty("spring.jpa.properties.hibernate.jdbc.batch_versioned_data"));
        properties.put("hibernate.generate_statistics", env.getProperty("spring.jpa.properties.hibernate.generate_statistics"));
        properties.put("hibernate.id.new_generator_mappings", env.getProperty("spring.jpa.properties.hibernate.id.new_generator_mappings"));
        properties.put("hhibernate.cache.use_second_level_cache", env.getProperty("spring.jpa.properties.hibernate.cache.use_second_level_cache"));
        properties.put("hibernate.globally_quoted_identifiers", env.getProperty("spring.jpa.properties.hibernate.globally_quoted_identifiers"));
        properties.put("hibernate.format_sql", env.getProperty("spring.jpa.properties.hibernate.format_sql"));
        properties.put("hibernate.show_sql", env.getProperty("spring.jpa.properties.hibernate.show_sql"));
        properties.put("hibernate.use_sql_comments", env.getProperty("spring.jpa.properties.hibernate.use_sql_comments"));
        properties.put("hibernate.type", env.getProperty("spring.jpa.properties.hibernate.type"));
        properties.put("hibernate.temp.use_jdbc_metadata_defaults", env.getProperty("spring.jpa.properties.hibernate.temp.use_jdbc_metadata_defaults"));
        properties.put("hibernate.naming.physical-strategy", env.getProperty("spring.jpa.hibernate.naming.physical-strategy"));
        properties.put("spring.h2.console.path", env.getProperty("spring.h2.console.path"));
        properties.put("spring.h2.console.enabled", env.getProperty("spring.h2.console.enabled"));
        properties.put("hibernate.event.merge.entity_copy_observer", env.getProperty("spring.jpa.hibernate.event.merge.entity_copy_observer"));
        em.setJpaPropertyMap(properties);

        return em;
    }

    @Primary
    @Bean
    public DataSource inmemoryDataSource() {
        final DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env.getProperty("spring.datasource.driverClassName"));
        dataSource.setUrl(env.getProperty("spring.datasource.url"));
        dataSource.setUsername(env.getProperty("spring.datasource.username"));
        dataSource.setPassword(env.getProperty("spring.datasource.password"));
        Properties properties = new Properties();
        properties.setProperty("spring.h2.console.path", env.getProperty("spring.h2.console.path"));
        properties.setProperty("spring.h2.console.enabled", env.getProperty("spring.h2.console.enabled"));
        dataSource.setConnectionProperties(properties);

        return dataSource;
    }

    @Primary
    @Bean
    public PlatformTransactionManager inmemoryDatabaseTransactionManager() {
        final JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(inmemoryDatabaseEntityManager().getObject());
        Properties properties = new Properties();
        properties.setProperty("spring.h2.console.path", env.getProperty("spring.h2.console.path"));
        properties.setProperty("spring.h2.console.enabled", env.getProperty("spring.h2.console.enabled"));
        transactionManager.setJpaProperties(properties);
        return transactionManager;
    }
}
