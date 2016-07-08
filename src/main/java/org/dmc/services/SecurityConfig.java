package org.dmc.services;

import javax.inject.Inject;

import org.dmc.services.security.AuthenticationExceptionHandler;
import org.dmc.services.security.UserPrincipalService;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsByNameServiceWrapper;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	
	@Inject
	private UserPrincipalService userPrincipalService;

	@Override
	protected void configure(AuthenticationManagerBuilder auth) {
		UserDetailsByNameServiceWrapper<PreAuthenticatedAuthenticationToken> wrapper = new UserDetailsByNameServiceWrapper<PreAuthenticatedAuthenticationToken>(userPrincipalService);
		
		PreAuthenticatedAuthenticationProvider preAuthenticatedProvider = new PreAuthenticatedAuthenticationProvider();
		preAuthenticatedProvider.setPreAuthenticatedUserDetailsService(wrapper);
		auth.authenticationProvider(preAuthenticatedProvider);
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		RequestHeaderAuthenticationFilter authFilter = new RequestHeaderAuthenticationFilter();
		authFilter.setPrincipalRequestHeader("AJP_eppn");
		authFilter.setCheckForPrincipalChanges(true);
		authFilter.setContinueFilterChainOnUnsuccessfulAuthentication(false);
		authFilter.setAuthenticationManager(authenticationManager());
		
		http
			.addFilter(authFilter)
			.authorizeRequests().anyRequest().permitAll()
			.and().exceptionHandling().authenticationEntryPoint(new AuthenticationExceptionHandler())
			.and().httpBasic().disable();
	}
	
}
