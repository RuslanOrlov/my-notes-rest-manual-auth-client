package notes.controllers;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.itextpdf.text.DocumentException;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
import notes.dto.NoteDtoClient;
import notes.props.PropsForNote;
import notes.rest.client.RestClientNotes;
import notes.tools.PDFGenerator;

//@Slf4j
@Controller
@RequestMapping("/notes-list")
@Data
public class NotesController {
	
	private RestClientNotes restClientNotes;
	
	private PropsForNote noteProps;
	
	public NotesController(RestClientNotes restClientNotes, PropsForNote noteProps) {
		this.restClientNotes = restClientNotes;
		this.noteProps = noteProps;
	}

	/*
	 * Ниже представлены методы управления постраничным просмотром данных
	 * 
	 * */
	
	@GetMapping("/paging")
	public String swithPaging() {
		if (this.noteProps.getIsPaging())
			this.noteProps.setIsPaging(false);
		else 
			this.noteProps.setIsPaging(true);
		return "redirect:/notes-list";
	}
	
	@GetMapping("/first")
	public String firstPage() {
		this.noteProps.setCurPage(0);
		return "redirect:/notes-list";
	}
	
	@GetMapping("/prev")
	public String prevPage() {
		if (this.noteProps.getCurPage() > 0) {
			this.noteProps.setCurPage(this.noteProps.getCurPage() - 1);
		}
		return "redirect:/notes-list";
	}
	
	@GetMapping("/next")
	public String nextPage() {
		Integer countNotes = this.restClientNotes.countAll(
										this.noteProps.getIsFiltering(), 
										this.noteProps.getFilteringValue());
		
		if (this.noteProps.getCurPage() < this.noteProps.getTotalPages(countNotes))
			this.noteProps.setCurPage(this.noteProps.getCurPage() + 1);
		
		return "redirect:/notes-list";
	}

	@GetMapping("/last")
	public String lastPage() {
		Integer countNotes = this.restClientNotes.countAll(
										this.noteProps.getIsFiltering(), 
										this.noteProps.getFilteringValue());
		
		this.noteProps.setCurPage(this.noteProps.getTotalPages(countNotes));
		
		return "redirect:/notes-list";
	}
	
	@PostMapping("/change-page-size")
	public String changePageSize(@ModelAttribute("props") PropsForNote props) {
		if (props.getPageSize() <= 0)
			props.setPageSize(1);
		this.noteProps.setPageSize(props.getPageSize());
		return "redirect:/notes-list";
	}
	
	/*
	 * Ниже представлены методы управления фильтром просмотра данных
	 * 
	 * */
	
	@GetMapping("/filter")
	public String switchFilter() {
		this.noteProps.setIsFiltering(!this.noteProps.getIsFiltering());
		
		Integer countNotes = this.restClientNotes.countAll(
										this.noteProps.getIsFiltering(), 
										this.noteProps.getFilteringValue());
		Integer totalPages = this.noteProps.getTotalPages(countNotes);
		if (this.noteProps.getCurPage() > totalPages) 
			this.noteProps.setCurPage(totalPages);
		
		return "redirect:/notes-list";
	}
	
	@GetMapping(value = "/query", params = "value")
	public String runFilteringQuery(@RequestParam("value") String value, Model model) {
		this.noteProps.setFilteringValueUI(value);
		this.noteProps.setFilteringValue("%" + value + "%");
		this.noteProps.setCurPage(0);
		return "redirect:/notes-list";
	}
	
	/*
	 * Ниже представлен метод экспорта данных о заметках во внешний PDF файл
	 * 
	 * */
	
	@GetMapping("/export-to-pdf")
	public void exportToPDF(HttpServletResponse response) throws 
							IOException, DocumentException {
		response.setContentType("application/pdf");
		
		String headerName = "Content-Disposition";
		String headerValue = "attachment; filename=pdf_" + 
								LocalDateTime.now().toString() + ".pdf";
		response.setHeader(headerName, headerValue);
		
		PDFGenerator generator = new PDFGenerator();
		generator.generate(response, formNotesList());
	}
	
	/*
	 * Ниже представлены методы работы с данными (CRUD) посредством REST клиента
	 * 
	 * */
	
	@GetMapping
	public String getAllNotes(Model model) {
		model.addAttribute("props", this.noteProps);
		model.addAttribute("notes", formNotesList());
		return "notes-list";
	}
	
	List<NoteDtoClient> formNotesList() {
		List<NoteDtoClient> notes = new ArrayList<>();
		
		if (!this.noteProps.getIsPaging() && !this.noteProps.getIsFiltering()) {
			notes = this.restClientNotes.getAllNotes();
		}
		else if (this.noteProps.getIsPaging() && !this.noteProps.getIsFiltering()) {
			notes = this.restClientNotes.getAllNotes(
						this.noteProps.getCurPage(), this.noteProps.getPageSize());
		}
		else if (!this.noteProps.getIsPaging() && this.noteProps.getIsFiltering()) {
			notes = this.restClientNotes.getAllNotes(
						this.noteProps.getFilteringValue());
		}
		else if (this.noteProps.getIsPaging() && this.noteProps.getIsFiltering()) {
			notes = this.restClientNotes.getAllNotes(
						this.noteProps.getCurPage(), this.noteProps.getPageSize(), 
						this.noteProps.getFilteringValue());
		}
		return notes;
	}
	
	@GetMapping("/{id}")
	public String getNoteById(@PathVariable Long id, Model model) {
		NoteDtoClient note = this.restClientNotes.getNoteById(id);
		model.addAttribute("note", note);
		return "note-card";
	}
	
	@GetMapping("/new")
	public String openCreateNoteForm(@ModelAttribute("note") NoteDtoClient note) {
		return "note-create";
	}
	
	@PostMapping
	public String postNote(@Valid @ModelAttribute("note") NoteDtoClient note, 
							BindingResult errors) {
		if (errors.hasErrors()) 
			return "note-create";
		
		this.restClientNotes.postNote(note);
		
		return "redirect:/notes-list";
	}
	
	@GetMapping("/{id}/edit")
	public String openPatchForm(@PathVariable Long id, Model model) {
		NoteDtoClient note = this.restClientNotes.getNoteById(id);
		
		model.addAttribute("oldName", note.getName());
		model.addAttribute("oldDescription", note.getDescription());
		model.addAttribute("note", new NoteDtoClient(note.getId(), null, null, 
							note.getIsDeleted(), note.getCreatedAt(), LocalDateTime.now()));
		
		return "note-edit";
	}
	
	// Эта первая версия метода patchNote сохраняет изменения в заметке (в объекте 
	// NoteDtoClient): 
	// - Для этого сначала создается промежуточный объект NoteDtoClient, полям которого 
	//   присваиваются изменения полей исходного объекта NoteDtoClient, пришедшие из формы 
	//   "note-edit". Также для сохранения даты и времени изменения в промежуточном 
	//   объекте запоминается значение для поля updatedAt. 
	// - Далее этот промежуточный объект с изменениями передается в Rest клиент с помощью 
	//   метода patchNote() Rest клиента. 
	/*
	@PatchMapping
	public String patchNote(@Valid @ModelAttribute("note") NoteDtoClient note, 
							BindingResult errors) {
		if (errors.hasErrors()) 
			return "note-edit";
		
		NoteDtoClient patch = new NoteDtoClient(null, null, null, null, null, null);
		
		if (note.getName() != null && note.getName().trim().length() > 0)
			patch.setName(note.getName().trim());
		if (note.getDescription() != null && note.getDescription().trim().length() > 0)
			patch.setDescription(note.getDescription().trim());
		patch.setUpdatedAt(note.getUpdatedAt());
		
		this.restClientNotes.patchNote(patch, note.getId());
		
		return "redirect:/notes-list";
	}*/
	
	// Эта вторая версия метода patchNote сохраняет изменения в заметке (в объекте 
	// NoteDtoClient): 
	// - Для этого сначала создается промежуточный ассоциативный массив Map, в который 
	//   добавляются пары значений с изменениями полей исходного объекта NoteDtoClient, 
	//   пришедшие из формы "note-edit". Также для сохранения даты и времени изменения в 
	//   промежуточном ассоциативном массиве Map запоминается значение для поля updatedAt 
	//   в формате текста (для корректной передачи значения LocalDateTime). 
	// - Далее этот промежуточный Map с изменениями передается в Rest клиент с помощью 
	//   метода patchNote() Rest клиента. 
	@PatchMapping
	public String patchNote(@Valid @ModelAttribute("note") NoteDtoClient note, 
							BindingResult errors) {
		if (errors.hasErrors()) 
			return "note-edit";
		
		Map<String, Object> patch = new HashMap<>();
		
		if (note.getName() != null && note.getName().trim().length() > 0)
			patch.put("name", note.getName().trim());
		if (note.getDescription() != null && note.getDescription().trim().length() > 0)
			patch.put("description", note.getDescription().trim());
		patch.put("updatedAt", note.getUpdatedAt().toString());
		
		this.restClientNotes.patchNote(patch, note.getId());
		
		return "redirect:/notes-list";
	}
	
	@GetMapping("/{id}/status")
	public String openStatusChangeForm(@PathVariable Long id, Model model) {
		NoteDtoClient note = this.restClientNotes.getNoteById(id);
		note.setUpdatedAt(LocalDateTime.now());
		model.addAttribute("note", note);
		return "note-status";
	}
	
	// Эта первая версия метода patchNoteStatus сохраняет изменения в статусе заметки 
	// (в статусе объекта NoteDtoClient): 
	// - Для этого сначала создается промежуточный объект NoteDtoClient, полю isDeleted 
	//   которого присваивается изменение, пришедшее из формы "note-status". Также 
	//   для сохранения даты и времени изменения в промежуточном объекте запоминается 
	//   значение для поля updatedAt. 
	// - Далее этот промежуточный объект с изменениями передается в Rest клиент с помощью 
	//   метода patchNote() Rest клиента. 
	/*
	@PatchMapping("/status")
	public String patchNoteStatus(NoteDtoClient note) {
		NoteDtoClient patch = new NoteDtoClient(null, null, null, null, null, null);
		
		patch.setIsDeleted(note.getIsDeleted());
		patch.setUpdatedAt(note.getUpdatedAt());
		
		this.restClientNotes.patchNote(patch, note.getId());
		
		return "redirect:/notes-list";
	}*/
	
	
	// Эта вторая версия метода patchNoteStatus сохраняет изменения в статусе заметки 
	// (в статусе объекта NoteDtoClient): 
	// - Для этого сначала создается промежуточный ассоциативный массив Map, в который 
	//   добавляется пара значений с изменением поля isDeleted, пришедшим из формы 
	//   "note-status". Также для сохранения даты и времени изменения в промежуточном 
	//   ассоциативном массиве Map запоминается значение для поля updatedAt в формате 
	//   текста (для корректной передачи значения LocalDateTime). 
	// - Далее этот промежуточный Map с изменениями передается в Rest клиент с помощью 
	//   метода patchNote() Rest клиента. 
	@PatchMapping("/status")
	public String patchNoteStatus(NoteDtoClient note) {
		Map<String, Object> patch = new HashMap<>();
		
		patch.put("isDeleted", note.getIsDeleted());
		patch.put("updatedAt", note.getUpdatedAt().toString());
		
		this.restClientNotes.patchNote(patch, note.getId());
		
		return "redirect:/notes-list";
	}
	
	@GetMapping("/{id}/delete")
	public String deleteNote(@PathVariable Long id) {
		NoteDtoClient note = this.restClientNotes.getNoteById(id);
		if (!note.getIsDeleted()) {
			return "warning";
		}
		this.restClientNotes.deleteNote(id);
		return "redirect:/notes-list";
	}
	
}
