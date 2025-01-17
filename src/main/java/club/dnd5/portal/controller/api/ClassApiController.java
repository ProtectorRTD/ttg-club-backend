package club.dnd5.portal.controller.api;

import club.dnd5.portal.dto.api.FilterApi;
import club.dnd5.portal.dto.api.FilterValueApi;
import club.dnd5.portal.dto.api.classes.ClassApi;
import club.dnd5.portal.dto.api.classes.ClassDetailApi;
import club.dnd5.portal.dto.api.classes.ClassRequestApi;
import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.Dice;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.book.TypeBook;
import club.dnd5.portal.model.classes.HeroClass;
import club.dnd5.portal.model.classes.archetype.Archetype;
import club.dnd5.portal.model.image.ImageType;
import club.dnd5.portal.repository.ImageRepository;
import club.dnd5.portal.repository.classes.ArchetypeRepository;
import club.dnd5.portal.repository.classes.ClassRepository;
import club.dnd5.portal.util.SpecificationUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Tag(name = "Class", description = "The Class API")
@RestController
public class ClassApiController {
	private final ClassRepository classRepo;
	private final ArchetypeRepository archetypeRepository;
	private final ImageRepository imageRepository;

	@PostMapping(value = "/api/v1/classes", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<ClassApi> getClasses(@RequestBody ClassRequestApi request) {
		Specification<HeroClass> specification = null;
		if (request.getFilter() != null) {
			if (!CollectionUtils.isEmpty(request.getFilter().getHitdice())) {
				specification = SpecificationUtil.getAndSpecification(null,
						(root, query, cb) -> root.get("diceHp").in(request.getFilter().getHitdice()));
			}
		}
		if (request.getSearch() != null && request.getSearch().getValue() != null && !request.getSearch().getValue().isEmpty()) {
			return classRepo.findAll(specification)
					.stream()
					.map(classObject -> new ClassApi(classObject, request))
					.filter(c -> !c.getArchetypes().isEmpty())
					.collect(Collectors.toList());
		}
		return classRepo.findAll(specification)
				.stream()
				.map(classObject -> new ClassApi(classObject, request))
				.filter(classApi -> request.getFilter() != null ?
						request.getFilter().getBooks().contains(classApi.getSource().getShortName()) || (classApi.isSidekick() && request.getFilter().getBooks().contains("TCE"))
						: true)
				.collect(Collectors.toList());
	}

	@PostMapping(value = "/api/v1/classes/{englishName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ClassDetailApi> getClassInfo(@RequestBody ClassRequestApi request, @PathVariable String englishName) {
		HeroClass heroClass = classRepo.findByEnglishName(englishName.replace('_', ' ')).orElseThrow(PageNotFoundException::new);
		Collection<String> images = imageRepository.findAllByTypeAndRefId(ImageType.CLASS, heroClass.getId());
		return ResponseEntity.ok(new ClassDetailApi(heroClass, images, request));
	}

	@PostMapping(value = "/api/v1/classes/{className}/{archetypeName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ClassDetailApi> getArchetypeInfo(@RequestBody ClassRequestApi request, @PathVariable String className,
			@PathVariable String archetypeName) {
		HeroClass heroClass = classRepo.findByEnglishName(className.replace('_', ' ')).orElseThrow(PageNotFoundException::new);
		Archetype archetype = heroClass.getArchetypes().stream().filter(a -> a.getEnglishName().equalsIgnoreCase(archetypeName.replace('_', ' '))).findFirst().get();
		Collection<String> images = imageRepository.findAllByTypeAndRefId(ImageType.SUBCLASS, archetype.getId());
		return ResponseEntity.ok(new ClassDetailApi(archetype, images, request));
	}
	@PostMapping("/api/v1/filters/classes")
	public FilterApi getClassFilter() {
		return getClassFilters();
	}
	private FilterApi getClassFilters() {
		FilterApi filters = new FilterApi();
		List<FilterApi> classSources = new ArrayList<>();
		for (TypeBook typeBook : TypeBook.values()) {
			List<Book> books = Stream.concat(classRepo.findBook(typeBook).stream(), archetypeRepository.findBook(typeBook).stream()).distinct().collect(Collectors.toList());
			if (!books.isEmpty()) {
				FilterApi filter = new FilterApi(typeBook.getName(), typeBook.name());
				filter.setValues(books.stream()
						.map(book -> new FilterValueApi(book.getSource(), book.getSource(),	Boolean.TRUE, book.getName()))
						.collect(Collectors.toList()));
				classSources.add(filter);
			}
		}
		filters.setSources(classSources);

		List<FilterApi> others = new ArrayList<>();
		FilterApi hillDiceFilter = new FilterApi("Кость хитов", "hitdice");
		hillDiceFilter.setValues(
				EnumSet.of(Dice.d6, Dice.d8, Dice.d10, Dice.d12).stream()
				.map(dice -> new FilterValueApi(dice.getName(), dice.getMaxValue()))
				.collect(Collectors.toList())
		);
		others.add(hillDiceFilter);
		filters.setOther(others);
		return filters;
	}
}
