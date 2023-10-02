package notes.rest.client;

import org.springframework.security.web.csrf.CsrfToken;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * Данный класс требуется для того, чтобы принять CsrfToken от сервера, 
 * так как в противном случае (например, при попытке десериализовать ответ 
 * сервера в объект CsrfToken или DefaultCsrfToken) выходит ошибка: 
 *   com.fasterxml.jackson.databind.exc.InvalidDefinitionException: 
 *   Cannot construct instance of `org.springframework.security.web.
 *   csrf.DefaultCsrfToken` (no Creators, like default constructor, exist): 
 *   cannot deserialize from Object value (no delegate- or property-based Creator)
 * */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyCsrfToken implements CsrfToken {
	
	private static final long serialVersionUID = 1L;
	
	private String token;
	
	private String parameterName;

	private String headerName;
	
	@Override
	public String getHeaderName() {
		return this.headerName;
	}

	@Override
	public String getParameterName() {
		return this.parameterName;
	}

	@Override
	public String getToken() {
		return this.token;
	}

}
