package notes.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NoteDtoClient {
	private Long id;
	
	@Size(max = 255, message = "Длина наименования не может превышать 255 символов!")
	private String name;
	
	@Size(max = 255, message = "Длина описания не может превышать 255 символов!")
	private String description;
	
	private Boolean isDeleted = false;
	private LocalDateTime createdAt = LocalDateTime.now();
	private LocalDateTime updatedAt;}
