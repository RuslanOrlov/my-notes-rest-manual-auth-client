package notes.rest.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;
import notes.dto.NoteDtoClient;
import notes.props.PropsForCurUser;

@Slf4j
@Component
public class RestClientNotes {
	
	private RestTemplate restTemplate;
	
	private PropsForCurUser userProps;
	
	private String urlWithoutId;
	private String urlWithoutIdSort1;
	private String urlWithoutIdSort2;
	private String urlWithId;
	private String urlCount;
	private String urlCountWithQuery;
	private String urlQuery;
	private String urlPagingQuery;
	
	public RestClientNotes(PropsForCurUser userProps) {
		
		this.userProps = userProps;
		
		/*
		 * Из официальной документации на RestTemplate: 
		 * Note that the standard JDK HTTP library does not support 
		 * the HTTP PATCH method.Configure the Apache HttpComponents 
		 * or OkHttp request factory to enable PATCH.
		 * 
		 * Так как стандартный RestTemplate не поддерживает метод 
		 * PATCH напрямую, то для обхода этой проблемы требуется 
		 * добавить поддержку метода PATCH через библиотеку Apache 
		 * HttpComponents. Для этого в pom.xml была добавлена зави-
		 * симость "httpclient5" библиотеки "org.apache.httpcomponents.client5", 
		 * а здесь в RestTemplate выполнена настройка его RequestFactory с 
		 * использованием HttpComponentsClientHttpRequestFactory, которому 
		 * передан подходящий HttpClient. 
		 * 
		 * */
		
		// Настройка объекта RestTemplate на обработку PATCH запросов
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpComponentsClientHttpRequestFactory requestFactory = 
				new HttpComponentsClientHttpRequestFactory(httpClient);
		this.restTemplate = new RestTemplate(requestFactory);
		
		// Добавление логгера к объекту RestTemplate
		this.restTemplate.getInterceptors().add(new ClientHttpRequestInterceptor() {
			
			@Override
			public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
					throws IOException {
				// логирование поступившего запроса 
				log.info("!!! RestClientNotes -> Request: {} {}", request.getMethod(), request.getURI());
				
				ClientHttpResponse response = execution.execute(request, body);
				
				// логирование полученного ответа 
				log.info("!!! RestClientNotes -> Response: {}", response);
				
				return response;
			}
		});
		
		this.urlWithoutId 		= "http://localhost:8080/api/notes";
		this.urlWithoutIdSort1 	= "http://localhost:8080/api/notes?sort={field}";
		this.urlWithoutIdSort2 	= "http://localhost:8080/api/notes?sort={field}"
														+ "&page={page}&size={size}";
		this.urlWithId 			= "http://localhost:8080/api/notes/{id}";
		this.urlCount 			= "http://localhost:8080/api/notes/count";
		this.urlCountWithQuery 	= "http://localhost:8080/api/notes/count?value={value}";
		this.urlQuery 			= "http://localhost:8080/api/notes?value={value}";
		this.urlPagingQuery 	= "http://localhost:8080/api/notes?value={value}"
														+ "&offset={offset}&limit={limit}";
	}
	
	public List<NoteDtoClient> getAllNotes() {
		List<NoteDtoClient> notes = new ArrayList<>();
				
		/*
		 * Первый вариант передачи аутентификационных данных на сервер:
		 *  - в явном виде через заголовок "Authorization"
		 * */
		/*
		HttpHeaders httpHeaders = new HttpHeaders();
		
		httpHeaders.add("Authorization", this.userProps.getAuthorizationHeader());
		httpHeaders.add("Accept", "application/json");
		
		HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
		
		ResponseEntity<NoteDtoClient[]> responseEntity = 
				this.restTemplate.exchange(
						this.urlWithoutIdSort1, HttpMethod.GET, 
						requestEntity, NoteDtoClient[].class, "id");*/
		/*---------------------------------------------------------*/
		
		/*
		 * Второй вариант передачи аутентификационных данных на сервер:
		 *  - в явном виде через заголовок "Authorization", используется 
		 *    сокращенная форма создания заголовка с помощью метода setBasicAuth()
		 * */
		/*
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setBasicAuth(
				this.userProps.getCurrentUser().getUsername(), 
				this.userProps.getCurrentUser().getOpenpass());
		
		HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
		
		ResponseEntity<NoteDtoClient[]> responseEntity = 
				this.restTemplate.exchange(
						this.urlWithoutIdSort1, HttpMethod.GET, 
						requestEntity, NoteDtoClient[].class, "id");*/
		/*---------------------------------------------------------*/
		
		/*
		 * Третий вариант передачи аутентификационных данных на сервер:
		 *  - используется RestTemplateBuilder для настройки RestTemplate, 
		 *    который всегда передает заголовок "Authorization"
		 * */
		/**/
		RestTemplate restTemplateHttpBasic =  new RestTemplateBuilder()
				.basicAuthentication(
						this.userProps.getCurrentUser().getUsername(), 
						this.userProps.getCurrentUser().getOpenpass())
				.build();
		
		// В третьем варианте не требуется настройка HttpClient и RequestFactory для 
		// поддержки настройки обработки PATCH запросов, скорее всего потому что эта 
		// настройка выполняется по умолчанию при использовании RestTemplateBuilder. 
		/* CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpComponentsClientHttpRequestFactory requestFactory = 
				new HttpComponentsClientHttpRequestFactory(httpClient);
		restTemplateHttpBasic.setRequestFactory(requestFactory); */
				
		ResponseEntity<NoteDtoClient[]> responseEntity = 
				restTemplateHttpBasic.exchange(
						this.urlWithoutIdSort1, HttpMethod.GET, 
						null, NoteDtoClient[].class, "id");
		/*---------------------------------------------------------*/
		
		if (responseEntity.getStatusCode().is2xxSuccessful()) {
			notes = Arrays.asList(responseEntity.getBody());
		}
		
		return notes;
	}
		
	public List<NoteDtoClient> getAllNotes(Integer curPage, Integer pageSize) {
		List<NoteDtoClient> notes = new ArrayList<>();
		
		HttpHeaders httpHeaders = new HttpHeaders();
		
		// Добавляем заголовок "Authorization"
		httpHeaders.add("Authorization", this.userProps.getAuthorizationHeader());
		
		HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
		
		ResponseEntity<NoteDtoClient[]> responseEntity = 
				this.restTemplate.exchange(
						this.urlWithoutIdSort2, HttpMethod.GET, 
						requestEntity, NoteDtoClient[].class, "id", curPage, pageSize);
		
		if (responseEntity.getStatusCode().is2xxSuccessful()) {
			notes = Arrays.asList(responseEntity.getBody());
		}
		
		return notes;
	}
	
	public List<NoteDtoClient> getAllNotes(String value) {
		List<NoteDtoClient> notes = new ArrayList<>();
		
		HttpHeaders httpHeaders = new HttpHeaders();
		
		// Добавляем заголовок "Authorization"
		httpHeaders.add("Authorization", this.userProps.getAuthorizationHeader());
		
		HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
		
		ResponseEntity<NoteDtoClient[]> responseEntity = 
				this.restTemplate.exchange(
						this.urlQuery, HttpMethod.GET, 
						requestEntity, NoteDtoClient[].class, value);
		
		if (responseEntity.getStatusCode().is2xxSuccessful()) {
			notes = Arrays.asList(responseEntity.getBody());
		}
		
		return notes;
	}
	
	public List<NoteDtoClient> getAllNotes(Integer curPage, Integer pageSize, String value) {
		List<NoteDtoClient> notes = new ArrayList<>();
		
		HttpHeaders httpHeaders = new HttpHeaders();
		
		// Добавляем заголовок "Authorization"
		httpHeaders.add("Authorization", this.userProps.getAuthorizationHeader());
		
		HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
		
		ResponseEntity<NoteDtoClient[]> responseEntity = 
				this.restTemplate.exchange(
						this.urlPagingQuery, HttpMethod.GET, 
						requestEntity, NoteDtoClient[].class, value, curPage*pageSize, pageSize);
		
		if (responseEntity.getStatusCode().is2xxSuccessful()) {
			notes = Arrays.asList(responseEntity.getBody());
		}
		
		return notes;
	}
		
	public Integer countAll(Boolean isFiltering, String value) {
		ResponseEntity<Integer> responseEntity = 
				ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		
		HttpHeaders httpHeaders = new HttpHeaders();
		
		// Добавляем заголовок "Authorization"
		httpHeaders.add("Authorization", this.userProps.getAuthorizationHeader());
		
		HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
		
		if (isFiltering)
			responseEntity = this.restTemplate.exchange(
									this.urlCountWithQuery, HttpMethod.GET, 
										requestEntity, Integer.class, value);
		else
			responseEntity = this.restTemplate.exchange(
									this.urlCount, HttpMethod.GET, 
										requestEntity, Integer.class);
		
		if (responseEntity.getStatusCode().is2xxSuccessful())
			return responseEntity.getBody();
		return 0;
	}
	
	public NoteDtoClient getNoteById(Long id) {
		HttpHeaders httpHeaders = new HttpHeaders();
		
		// Добавляем заголовок "Authorization"
		httpHeaders.add("Authorization", this.userProps.getAuthorizationHeader());
		
		HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
		
		ResponseEntity<NoteDtoClient> responseEntity = 
				this.restTemplate.exchange(this.urlWithId, HttpMethod.GET, 
										requestEntity, NoteDtoClient.class, id);
		
		if (responseEntity.getStatusCode().is2xxSuccessful()) {
			return responseEntity.getBody();
		}
		return null;
	}
	
	public NoteDtoClient postNote(NoteDtoClient note) {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		
		// Добавляем заголовок "Authorization"
		httpHeaders.add("Authorization", this.userProps.getAuthorizationHeader());
		
		// Запрашиваем на сервере новый CSRF токен
		CsrfToken csrfToken = 
				this.restTemplate.getForObject(
						"http://localhost:8080/api/csrf", MyCsrfToken.class);
		
		// Добавляем заголовок "X-CSRF-TOKEN" со значением запрошенного токена
		httpHeaders.add("X-CSRF-TOKEN", csrfToken.getToken());
		
		HttpEntity<NoteDtoClient> requestEntity = new HttpEntity<NoteDtoClient>(note, httpHeaders);
		
		ResponseEntity<NoteDtoClient> responseEntity = 
				this.restTemplate.postForEntity(this.urlWithoutId, requestEntity, 
												NoteDtoClient.class);
		
		if (responseEntity.getStatusCode().is2xxSuccessful()) {
			return responseEntity.getBody();
		}
		return null;
	}
	
	// Это первая версия метода patchNote, которая получает от контроллера MVC 
	// промежуточный DTO объект NoteDtoClient с изменениями редактирования 
	// исходного объекта Note
	// - И ДАЛЕЕ направляет PATCH запрос с этим промежуточным объектом в REST 
	//   контроллер по указанному URL для сохранения изменений в исходном объекте 
	//   Note в БД
	// - И ДАЛЕЕ получает от REST контроллера ответ с обновленным в БД исходным 
	//   объектом Note в форме DTO объекта NoteDtoClient. 
	/**/
	public NoteDtoClient patchNote(NoteDtoClient patch, Long id) {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		
		// Добавляем заголовок "Authorization"
		httpHeaders.add("Authorization", this.userProps.getAuthorizationHeader());
		
		// Запрашиваем на сервере новый CSRF токен
		CsrfToken csrfToken = 
				this.restTemplate.getForObject(
						"http://localhost:8080/api/csrf", MyCsrfToken.class);
		
		// Добавляем заголовок "X-CSRF-TOKEN" со значением запрошенного токена
		httpHeaders.add("X-CSRF-TOKEN", csrfToken.getToken());
		
		HttpEntity<NoteDtoClient> requestEntity = 
				new HttpEntity<NoteDtoClient>(patch, httpHeaders);
		
		ResponseEntity<NoteDtoClient> responseEntity = 
				this.restTemplate.exchange(this.urlWithId, HttpMethod.PATCH, 
						requestEntity, NoteDtoClient.class, id);
		
		if (responseEntity.getStatusCode().is2xxSuccessful()) {
			return responseEntity.getBody();
		}
		return null;
	}

	// Это вторая версия метода patchNote, которая получает от контроллера MVC 
	// промежуточный ассоциативный массив Map с изменениями редактирования исходного 
	// объекта Note (в клиенте обрабатывается в форме DTO объекта NoteDtoClient) 
	// - И ДАЛЕЕ направляет PATCH запрос с этим промежуточным ассоциативным массивом 
	//   Map в REST контроллер на сервер по указанному URL для сохранения изменений 
	//   в исходном объекте Note в БД
	// - И ДАЛЕЕ получает от REST контроллера ответ с обновленным в БД исходным 
	//   объектом Note в форме DTO объекта NoteDtoClient. 
	/**/
	public NoteDtoClient patchNote(Map<String, Object> patch, Long id) {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		
		// Добавляем заголовок "Authorization"
		httpHeaders.add("Authorization", this.userProps.getAuthorizationHeader());
		
		// Запрашиваем на сервере новый CSRF токен
		CsrfToken csrfToken = 
				this.restTemplate.getForObject(
						"http://localhost:8080/api/csrf", MyCsrfToken.class);
		
		// Добавляем заголовок "X-CSRF-TOKEN" со значением запрошенного токена
		httpHeaders.add("X-CSRF-TOKEN", csrfToken.getToken());
		
		HttpEntity<Map<String, Object>> requestEntity = 
				new HttpEntity<Map<String, Object>>(patch, httpHeaders);
		
		ResponseEntity<NoteDtoClient> responseEntity = 
				this.restTemplate.exchange(this.urlWithId, HttpMethod.PATCH, 
						requestEntity, NoteDtoClient.class, id);
		
		/*
		// Создаем RestTemplate, настроенный на HttpBasic аутентификацию 
		RestTemplate restTemplateHttpBasic =  new RestTemplateBuilder()
				.basicAuthentication(
						this.userProps.getCurrentUser().getUsername(), 
						this.userProps.getCurrentUser().getOpenpass())
				.build();
		
		// Запрашиваем на сервере новый CSRF токен
		// --------------------------------------------------------------------------
		// КРАЙНЕ ВАЖНО: запрашивать CSRF токен нужно с помощью того же RestTemplate, 
		// через который будет также выполняться основной запрос в БД. В нашем случае 
		// оба запроса выполняются с помощью "restTemplateHttpBasic". 
		CsrfToken csrfToken = 
				restTemplateHttpBasic.getForObject(
						"http://localhost:8080/api/csrf", MyCsrfToken.class);
		
		// Добавляем заголовок "X-CSRF-TOKEN" со значением запрошенного токена
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("X-CSRF-TOKEN", csrfToken.getToken());
		
		HttpEntity<Map<String, Object>> requestEntity = 
				new HttpEntity<Map<String, Object>>(patch, httpHeaders);
		
		// Выполняем основной запрос в БД 
		ResponseEntity<NoteDtoClient> responseEntity = 
				restTemplateHttpBasic.exchange(
						this.urlWithId, HttpMethod.PATCH, 
						requestEntity, NoteDtoClient.class, id);
		*/
		
		if (responseEntity.getStatusCode().is2xxSuccessful()) {
			return responseEntity.getBody();
		}
		return null;
	}
	
	public void deleteNote(Long id) {
		HttpHeaders httpHeaders = new HttpHeaders();
		
		// Добавляем заголовок "Authorization"
		httpHeaders.add("Authorization", this.userProps.getAuthorizationHeader());
		
		// Запрашиваем на сервере новый CSRF токен
		CsrfToken csrfToken = 
				this.restTemplate.getForObject(
						"http://localhost:8080/api/csrf", MyCsrfToken.class);
		
		// Добавляем заголовок "X-CSRF-TOKEN" со значением ранее запрошенного токена
		httpHeaders.add("X-CSRF-TOKEN", csrfToken.getToken());
		
		HttpEntity<Object> requestEntity = new HttpEntity<>(httpHeaders);
		
		this.restTemplate.exchange(this.urlWithId, HttpMethod.DELETE, 
										requestEntity, NoteDtoClient.class, id);
	}	
	
}
