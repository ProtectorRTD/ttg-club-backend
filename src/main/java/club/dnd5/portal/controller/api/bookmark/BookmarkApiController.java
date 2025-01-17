package club.dnd5.portal.controller.api.bookmark;

import club.dnd5.portal.dto.api.bookmark.BookmarkApi;
import club.dnd5.portal.dto.api.bookmark.CategoryApi;
import club.dnd5.portal.model.BookmarkCategory;
import club.dnd5.portal.model.user.User;
import club.dnd5.portal.repository.user.UserRepository;
import club.dnd5.portal.service.BookmarkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Tag(name = "Закладки", description = "API закладок")
@RestController
@RequestMapping("/api/v1/bookmarks")
public class BookmarkApiController {
	private final BookmarkService service;
	private final UserRepository userRepository;

	@Operation(summary = "Gets all user bookmarks")
	@SecurityRequirement(name = "Bearer Authentication")
	@GetMapping
	@ResponseStatus(code = HttpStatus.OK)
	public Collection<BookmarkApi> getBookmarks(@RequestParam(required = false) Boolean parent) {
		if (parent != null) {
			return service.getRootBookmarks(getCurrentUser());
		}
		return service.getBookmarks(getCurrentUser());
	}

	@Operation(summary = "Add new bookmark")
	@SecurityRequirement(name = "Bearer Authentication")
	@PostMapping
	public ResponseEntity<BookmarkApi> createBookmark(@RequestBody BookmarkApi bookmarkApi){
		return ResponseEntity.ok(service.addBookmark(getCurrentUser(), bookmarkApi));
	}

	@Operation(summary = "Update bookmark")
	@SecurityRequirement(name = "Bearer Authentication")
	@PutMapping
	public ResponseEntity<BookmarkApi> updateBookmarks(@RequestBody BookmarkApi bookmark){
		return ResponseEntity.ok(service.updateBookmark(getCurrentUser(), bookmark));
	}

	@Operation(summary = "Delete bookmark")
	@SecurityRequirement(name = "Bearer Authentication")
	@DeleteMapping("/{uuid}")
	public ResponseEntity<?> deleteBookmark(@PathVariable String uuid){
		service.deleteBookmark(uuid);
		return ResponseEntity.ok().build();
	}

	@Operation(summary = "Get all categories")
	@GetMapping("/categories")
	public ResponseEntity<List<CategoryApi>> getBookmarkCategories() {
		return ResponseEntity.ok(
			BookmarkCategory.getCategories()
				.stream()
				.map(CategoryApi::new)
				.collect(Collectors.toList())
		);
	}

	@Operation(summary = "Get category from URL")
	@GetMapping("/category")
	public ResponseEntity<CategoryApi> getBookmarkCategory(@RequestParam(required = false) String url,
			@RequestParam(required = false) String code) {
		if (url != null) {
			try {
				url = URLDecoder.decode(url, StandardCharsets.UTF_8.toString());
				return ResponseEntity.ok(new CategoryApi(BookmarkCategory.getCategoryByURL(url)));
			}
			catch (UnsupportedEncodingException exception) {
				return ResponseEntity.ok(new CategoryApi(BookmarkCategory.getDefaultCategory()));
			}
		}
		if (code != null) {
			return ResponseEntity.ok(new CategoryApi(BookmarkCategory.getCategoryByCode(code)));
		}
		return ResponseEntity.ok(new CategoryApi(BookmarkCategory.getDefaultCategory()));
	}

	private User getCurrentUser() {
		SecurityContext context = SecurityContextHolder.getContext();
		String userName = context.getAuthentication().getName();
		return userRepository.findByEmailOrUsername(userName, userName).orElseThrow(() -> new UsernameNotFoundException(userName));
	}
}
