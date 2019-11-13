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
@EnableJpaRepositories(basePackages = "ru.fheads.dao.sd",
        entityManagerFactoryRef = "sdEntityManagerFactory",
        transactionManagerRef = "sdTransactionManager")
public class SdDatasourceConfiguration {

    @Primary
    @Bean
    @ConfigurationProperties("spring.datasource3")
    public DataSourceProperties sdDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Primary
    @Bean
    @ConfigurationProperties("spring.datasource3.configuration")
    public DataSource sdDataSource() {
        return sdDataSourceProperties().initializeDataSourceBuilder()
                .type(HikariDataSource.class).build();
    }

    @Primary
    @Bean(name = "sdEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean sdEntityManagerFactory(EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(sdDataSource())
                .packages(Task.class)
                .build();
    }

    @Primary
    @Bean
    public PlatformTransactionManager sdTransactionManager(
            final @Qualifier("sdEntityManagerFactory") LocalContainerEntityManagerFactoryBean sdEntityManagerFactory) {
        return new JpaTransactionManager(sdEntityManagerFactory.getObject());
    }
}
