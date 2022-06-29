package org.pepfar.pdma.config;

import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.instrument.classloading.InstrumentationLoadTimeWeaver;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate5.HibernateExceptionTranslator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@ComponentScan(basePackages = { "org.pepfar.pdma.app.utils", "org.pepfar.pdma.app.data.service",
		"org.pepfar.pdma.app.data.repository", "org.pepfar.pdma.app.data.service.jpa",
		"org.pepfar.pdma.app.data.domain", "org.pepfar.pdma.app.data.domain.auditing" })
@EnableJpaRepositories(basePackages = { "org.pepfar.pdma.app.data.repository" })
public class DatabaseConfig implements InitializingBean {

	@Autowired
	private Environment env;

	private static final String[] MAPPINGS = { "META-INF/case.orm.xml", "META-INF/case-org.orm.xml",
			"META-INF/labtest.orm.xml", "META-INF/appointment.orm.xml", "META-INF/wr-case.orm.xml",
			"META-INF/mmd.orm.xml", "META-INF/tb-prophylaxis2.orm.xml", "META-INF/tb-treatment2.orm.xml" };

	@Bean
	public SpelAwareProxyProjectionFactory projectionFactory() {
		return new SpelAwareProxyProjectionFactory();
	}
	
	@Bean
	public DataSource dataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName(env.getProperty("spring.datasource.driver-class-name"));
		dataSource.setUrl(env.getProperty("spring.datasource.url"));
		dataSource.setUsername(env.getProperty("spring.datasource.username"));
		dataSource.setPassword(env.getProperty("spring.datasource.password"));

		return dataSource;
	}

	@Bean
	public PlatformTransactionManager transactionManager() {
		EntityManagerFactory factory = entityManagerFactory().getObject();
		return new JpaTransactionManager(factory);
	}

	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
		LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();

		HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		vendorAdapter.setGenerateDdl(Boolean.TRUE);

		factory.setDataSource(dataSource());
		factory.setJpaVendorAdapter(vendorAdapter);
		factory.setPackagesToScan("org.pepfar.pdma.app.data.domain", "org.pepfar.pdma.security.domain");

		Properties jpaProperties = new Properties();
		jpaProperties.put("hibernate.dialect", env.getProperty("spring.jpa.properties.hibernate.dialect"));
		jpaProperties.put("hibernate.max_fetch_depth",
				env.getProperty("spring.jpa.properties.hibernate.max_fetch_depth"));
		jpaProperties.put("hibernate.jdbc.fetch_size",
				env.getProperty("spring.jpa.properties.hibernate.jdbc.fetch_size"));
		jpaProperties.put("hibernate.jdbc.batch_size",
				env.getProperty("spring.jpa.properties.hibernate.jdbc.batch_size"));
		jpaProperties.put("hibernate.hbm2ddl.auto", env.getProperty("spring.jpa.hibernate.ddl-auto"));
		jpaProperties.put("hibernate.show_sql", env.getProperty("spring.jpa.show-sql"));

		jpaProperties.put("hibernate.c3p0.min_size", env.getProperty("spring.jpa.properties.hibernate.c3p0.min_size"));
		jpaProperties.put("hibernate.c3p0.max_size", env.getProperty("spring.jpa.properties.hibernate.c3p0.max_size"));
		jpaProperties.put("hibernate.c3p0.timeout", env.getProperty("spring.jpa.properties.hibernate.c3p0.timeout"));
		jpaProperties.put("hibernate.c3p0.max_statements",
				env.getProperty("spring.jpa.properties.hibernate.c3p0.max_statements"));
		jpaProperties.put("hibernate.c3p0.idle_test_period",
				env.getProperty("spring.jpa.properties.hibernate.c3p0.idle_test_period"));
		jpaProperties.put("hibernate.c3p0.acquire_increment",
				env.getProperty("spring.jpa.properties.hibernate.c3p0.acquire_increment"));
		jpaProperties.put("hibernate.c3p0.validate", env.getProperty("spring.jpa.properties.hibernate.c3p0.validate"));

		// ORM mappings
		factory.setMappingResources(MAPPINGS);

		factory.setJpaProperties(jpaProperties);
		factory.afterPropertiesSet();
		factory.setLoadTimeWeaver(new InstrumentationLoadTimeWeaver());

		return factory;
	}

	@Bean
	public HibernateExceptionTranslator hibernateExceptionTranslator() {
		return new HibernateExceptionTranslator();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
//		System.out.println(env.getProperty("spring.datasource.driver-class-name"));
//		System.out.println(env.getProperty("spring.datasource.url"));
	}

}
