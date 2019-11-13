package ru.fheads.configurations.datasources;

import com.zaxxer.hikari.HikariDataSource;
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
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ru.fheads.entities.Task;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "ru.fheads.dao.redmine",
        entityManagerFactoryRef = "redmineEntityManagerFactory",
        transactionManagerRef = "redmineTransactionManager")
public class RedmineDatasourceConfiguration {

    @Bean
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties redmineDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("app.datasource.configuration")
    public DataSource redmineDataSource() {
        return redmineDataSourceProperties().initializeDataSourceBuilder()
                .type(HikariDataSource.class).build();
    }

    @Bean(name = "redmineEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean redmineEntityManagerFactory(EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(redmineDataSource())
                .packages(Task.class)
                .build();
    }

    @Bean
    public PlatformTransactionManager redmineTransactionManager(
            final @Qualifier("redmineEntityManagerFactory") LocalContainerEntityManagerFactoryBean redmineEntityManagerFactory) {
        return new JpaTransactionManager(redmineEntityManagerFactory.getObject());
    }
}
