package com.sample.source;

import com.sample.source.property.Label;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

@Configuration
@MapperScan(annotationClass = Mapper.class, basePackageClasses = SourceApplication.class, sqlSessionFactoryRef = "sqlSessionFactory")
public class DataBaseConfig {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.hikari")
    static DataSource dataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Primary
    @Bean
    static SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        factoryBean.setMapperLocations(
            resolver.getResources("classpath:mybatis-mapper/*.xml"));
        factoryBean.setTypeAliases(Label.class);
        factoryBean.getObject()
            .getConfiguration()
            .setMapUnderscoreToCamelCase(true);
        return factoryBean.getObject();
    }

}
