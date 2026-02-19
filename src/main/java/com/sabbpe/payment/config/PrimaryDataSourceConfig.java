package com.sabbpe.payment.config;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import org.springframework.transaction.PlatformTransactionManager;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.sabbpe.payment.repository",
        entityManagerFactoryRef = "entityManagerFactory",
        transactionManagerRef = "transactionManager"
)
public class PrimaryDataSourceConfig {

    // =====================================================
    // ✅ PRIMARY DATASOURCE PROPERTIES
    // =====================================================
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties primaryProperties() {
        return new DataSourceProperties();
    }

    // =====================================================
    // ✅ PRIMARY DATASOURCE
    // =====================================================
    @Bean(name = "dataSource")
    @Primary
    public DataSource dataSource() {
        return primaryProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    // =====================================================
    // ✅ ENTITY MANAGER FACTORY
    // =====================================================
    @Bean(name = "entityManagerFactory")
    @Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            EntityManagerFactoryBuilder builder) {

        // ⭐ IMPORTANT: Hibernate properties must be added manually
        Map<String, Object> properties = new HashMap<>();

        // creates/updates tables automatically
        properties.put("hibernate.hbm2ddl.auto", "update");

        // optional but recommended
        properties.put("hibernate.show_sql", true);
        properties.put("hibernate.format_sql", true);

        return builder
                .dataSource(dataSource())
                .packages("com.sabbpe.payment.entity") // entity scan
                .persistenceUnit("primary")
                .properties(properties)
                .build();
    }

    // =====================================================
    // ✅ TRANSACTION MANAGER
    // =====================================================
    @Bean(name = "transactionManager")
    @Primary
    public PlatformTransactionManager transactionManager(
            @Qualifier("entityManagerFactory")
            EntityManagerFactory emf) {

        return new JpaTransactionManager(emf);
    }
}
