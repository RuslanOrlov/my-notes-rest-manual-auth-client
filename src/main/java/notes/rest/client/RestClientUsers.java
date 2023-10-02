package notes.rest.client;

import java.io.IOException;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;
import notes.dto.UserDtoClient;

@Slf4j
@Component
public class RestClientUsers {
	
	private RestTemplate restTemplate;
	private String url;
	private String urlForUser;

	public RestClientUsers() {
		this.restTemplate = new RestTemplate();
		
		// Добавление логгера к объекту RestTemplate
		this.restTemplate.getInterceptors().add(new ClientHttpRequestInterceptor() {
			
			@Override
			public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
					throws IOException {
				// логирование поступившего запроса 
				log.info("!!! RestClientUsers -> Request: {} {}", request.getMethod(), request.getURI());
				
				ClientHttpResponse response = execution.execute(request, body);
				
				// логирование полученного ответа 
				log.info("!!! RestClientUsers -> Response: {}", response);
				
				return response;
			}
		});
		
		this.url 		= "http://localhost:8080/api/users";
		this.urlForUser = "http://localhost:8080/api/users?username={value}";
	}
	
	public UserDtoClient postUser(UserDtoClient user) {
		return this.restTemplate.postForObject(this.url, user, UserDtoClient.class);
	}
	
	public UserDtoClient getUserByUsername(String username) {
		UserDtoClient dto = 
				this.restTemplate.getForObject(this.urlForUser, UserDtoClient.class, username);
		return dto;
	}
	
}
