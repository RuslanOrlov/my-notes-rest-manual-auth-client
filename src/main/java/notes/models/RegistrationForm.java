package notes.models;

import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import notes.dto.UserDtoClient;

@Data
public class RegistrationForm {
	
	@NotBlank(message = "Имя пользователя не может быть пустым!")
	private String username;
	private String password;
	private String confirm;
	private String email;
	
	public boolean isCorrectPassword() {
		return this.password.equals(this.confirm);
	}
	
	public UserDtoClient toUser(PasswordEncoder encoder) {
		return new UserDtoClient(
				null, this.username, encoder.encode(this.password), this.password, this.email);
	}
	
}
