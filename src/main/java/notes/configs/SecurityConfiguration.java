package notes.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
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
							.requestMatchers("/notes-list", "/notes-list/**", 
												"/api/notes", "/api/notes/**").authenticated()
							.requestMatchers("/", "/**").permitAll()
							.requestMatchers(HttpMethod.POST, "/api/users").permitAll() )
				
				/*
				 * Ниже следующая настройка исключает (отключает) защиту CSRF при выполнении 
				 * запросов POST, PUT, PATCH, DELETE по ниже указанному пути. При отсутствии 
				 * такой настройки при выполнении запросов POST, PUT, PATCH, DELETE выходит 
				 * ошибка запрета доступа к запрошеному ресурсу по ниже указанному пути: 
				 *   org.springframework.web.client.HttpClientErrorException$Forbidden: 403 : 
				 *   "{"timestamp":"2023-08-30T12:51:25.411+00:00","status":403,"error":
				 *   "Forbidden","message":"Forbidden","path":"/api/notes"}"
				 * */
				.csrf( (csrf) -> csrf.ignoringRequestMatchers("/api/users") )
				
				/* 
				 * При отсутствии ниже следующей настройки ".httpBasic()" выходит ошибка: 
				 *   "org.springframework.web.client.UnknownContentTypeException: 
				 *   Could not extract response: no suitable HttpMessageConverter 
				 *   found for response type [class [Lnotes.models.Note;] and content 
				 *   type [text/html;charset=UTF-8]"
				 * */
				.httpBasic(Customizer.withDefaults())
				
				.formLogin( (formLogin) -> 
						formLogin
							.loginPage("/login") )
				
				.build();
	}
}
