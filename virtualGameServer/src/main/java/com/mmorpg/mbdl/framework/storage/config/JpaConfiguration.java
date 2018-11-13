package com.mmorpg.mbdl.framework.storage.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.github.xiaolyuh.aspect.LayeringAspect;
import com.github.xiaolyuh.manager.LayeringCacheManager;
import com.mmorpg.mbdl.framework.storage.core.Storage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

@Configuration
// @ComponentScan(basePackageClasses = {JpaConfiguration.class})
// 由于basePackages = {"com.mmorpg.mbdl.bussiness.**.dao"}，所以自定义的IStorage子接口必须位于com.mmorpg.mbdl.bussiness.**.dao下
@EnableJpaRepositories(basePackageClasses = JpaConfiguration.class,basePackages = {"com.mmorpg.mbdl.bussiness.**.dao"},
        repositoryBaseClass = Storage.class)
@ImportResource(locations = {"classpath*:applicationContext.xml"})
public class JpaConfiguration {
    @Bean
    @Autowired
    LayeringCacheManager layeringCacheManager(RedisTemplate redisTemplate){
        LayeringCacheManager layeringCacheManager = new LayeringCacheManager(redisTemplate);
        // 统计开关
        layeringCacheManager.setStats(true);
        return layeringCacheManager;
    }
    @Bean
    LayeringAspect layeringAspect(){
        return new LayeringAspect();
    }
    @Bean
    HibernateJpaDialect jpaDialect(){
        return new HibernateJpaDialect();
    }
    @Bean
    HibernateJpaVendorAdapter jpaVendorAdapter(){
        HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
        hibernateJpaVendorAdapter.setDatabase(Database.MYSQL);
        hibernateJpaVendorAdapter.setGenerateDdl(true);
        hibernateJpaVendorAdapter.setShowSql(false);
        hibernateJpaVendorAdapter.setDatabasePlatform("org.hibernate.dialect.MySQL55Dialect");
        return hibernateJpaVendorAdapter;
    }
    @Bean
    @Autowired
    LocalContainerEntityManagerFactoryBean entityManagerFactory(DruidDataSource druidDataSource){
        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryBean.setDataSource(druidDataSource);
        entityManagerFactoryBean.setPackagesToScan("com.mmorpg.**.entity");
        entityManagerFactoryBean.setJpaDialect(jpaDialect());
        entityManagerFactoryBean.setJpaVendorAdapter(jpaVendorAdapter());
        return entityManagerFactoryBean;
    }
    @Bean
    @Autowired
    JpaTransactionManager transactionManager(DruidDataSource druidDataSource){
        JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();
        return jpaTransactionManager;
    }
}
