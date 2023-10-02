package notes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDtoClient {
	private Long id;
	private String username;
	private String password;
	private String openpass;
	private String email;
}
