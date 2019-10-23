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
import ru.fheads.entities.Task;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "ru.fheads.dao.crm",
        entityManagerFactoryRef = "crmEntityManagerFactory",
        transactionManagerRef = "crmTransactionManager")
public class CrmDatasourceConfiguration {

    @Bean
    @ConfigurationProperties("spring.datasource2")
    public DataSourceProperties crmDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("spring.datasource2.configuration")
    public DataSource crmDataSource() {
        return crmDataSourceProperties().initializeDataSourceBuilder()
                .type(HikariDataSource.class).build();
    }

    @Bean(name = "crmEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean crmEntityManagerFactory(EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(crmDataSource())
                .packages(Task.class)
                .build();
    }

    @Bean
    public PlatformTransactionManager crmTransactionManager(
            final @Qualifier("crmEntityManagerFactory") LocalContainerEntityManagerFactoryBean crmEntityManagerFactory) {
        return new JpaTransactionManager(crmEntityManagerFactory.getObject());
    }
}
