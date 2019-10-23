package ru.fheads.configurations.datasources;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ru.fheads.entities.SavedTask;
import ru.fheads.entities.Task;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "ru.fheads.dao.dashboard",
        entityManagerFactoryRef = "dashboardEntityManagerFactory",
        transactionManagerRef = "dashboardTransactionManager")
public class DashboardDatasourceConfiguration {

    @Bean
    @ConfigurationProperties("spring.datasource4")
    public DataSourceProperties dashboardDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("spring.datasource4.configuration")
    public DataSource dashboardDataSource() {
        return dashboardDataSourceProperties().initializeDataSourceBuilder()
                .type(HikariDataSource.class).build();
    }

    @Bean(name = "dashboardEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean dashboardEntityManagerFactory(EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(dashboardDataSource())
                .packages(SavedTask.class)
                .build();
    }

    @Bean
    public PlatformTransactionManager dashboardTransactionManager(
            final @Qualifier("dashboardEntityManagerFactory") LocalContainerEntityManagerFactoryBean dashboardEntityManagerFactory) {
        return new JpaTransactionManager(dashboardEntityManagerFactory.getObject());
    }
}
