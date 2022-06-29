package org.pepfar.pdma.config;

import org.pepfar.pdma.security.filter.JsonToUrlEncodedAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;

@Configuration
@EnableResourceServer
public class OAuth2ResourceServerConfig extends ResourceServerConfigurerAdapter {

	@Autowired
	private DatabaseConfig dbConfig;

	@Autowired
	private JsonToUrlEncodedAuthenticationFilter jsonFilter;

	@Override
	public void configure(final HttpSecurity http) throws Exception {
		http.addFilterBefore(jsonFilter, ChannelProcessingFilter.class)

				.sessionManagement()

				.sessionFixation().migrateSession()
				
				.sessionCreationPolicy(SessionCreationPolicy.ALWAYS)

				.and().httpBasic().disable()

				.anonymous()

				.and().authorizeRequests()

//				.antMatchers("/public/**").anonymous()

				.antMatchers("/api/**").authenticated();

	}

	@Override
	public void configure(final ResourceServerSecurityConfigurer config) {
		config.tokenServices(tokenServices());
	}

	@Bean
	@Primary
	public DefaultTokenServices tokenServices() {
		final DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
		defaultTokenServices.setTokenStore(tokenStore());
		return defaultTokenServices;
	}

	@Bean
	public TokenStore tokenStore() {
		return new JdbcTokenStore(dbConfig.dataSource());
	}

}
