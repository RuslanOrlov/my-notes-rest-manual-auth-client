package notes.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import notes.dto.UserDtoClient;
import notes.models.UserClient;
//import lombok.extern.slf4j.Slf4j;
import notes.rest.client.RestClientUsers;

//@Slf4j
@Configuration
public class SecurityConfiguration {

    @Bean
    PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

    @Bean
    UserDetailsService userDetailsService(RestClientUsers restClientUsers) {
		/*
		 * Лямбда-функция реализует метод loadUserByUsername() интерфейса 
		 * UserDetailsService и возвращает службу хранения учетных записей 
		 * пользователей (то есть объект UserDetailsService)
		 * */
		return username -> {
			UserDtoClient dto = restClientUsers.getUserByUsername(username); 
			UserClient user = null; 
			if (dto != null) {
				user = new UserClient(dto);
				return user;
			}
			throw new UsernameNotFoundException("User '"  + username + "' not found!");
		};
	}

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    	return http
				.authorizeHttpRequests( (authorizeHttpRequests) ->
						authorizeHttpRequests
							.requestMatchers("/notes-list", "/notes-list/**").authenticated()
							.requestMatchers("/", "/**").permitAll() )
				
				.formLogin( (formLogin) -> 
						formLogin
							.loginPage("/login") )
				.logout( (logout) -> 
						logout
							.logoutSuccessUrl("/"))
				
				.build();
	}
}
