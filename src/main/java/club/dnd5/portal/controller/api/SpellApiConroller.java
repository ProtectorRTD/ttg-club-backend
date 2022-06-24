package club.dnd5.portal.controller.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.datatables.mapping.Column;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.Search;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import club.dnd5.portal.dto.api.spell.SpellApi;
import club.dnd5.portal.dto.api.FilterApi;
import club.dnd5.portal.dto.api.FilterValueApi;
import club.dnd5.portal.dto.api.spell.ReferenceClassApi;
import club.dnd5.portal.dto.api.spell.SpellDetailApi;
import club.dnd5.portal.dto.api.spell.SpellRequesApi;
import club.dnd5.portal.dto.api.spells.SpellFvtt;
import club.dnd5.portal.dto.api.spells.SpellsFvtt;
import club.dnd5.portal.model.DamageType;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.book.TypeBook;
import club.dnd5.portal.model.classes.HeroClass;
import club.dnd5.portal.model.classes.archetype.Archetype;
import club.dnd5.portal.model.races.Race;
import club.dnd5.portal.model.splells.MagicSchool;
import club.dnd5.portal.model.splells.Spell;
import club.dnd5.portal.repository.classes.ArchetypeSpellRepository;
import club.dnd5.portal.repository.classes.ClassRepository;
import club.dnd5.portal.repository.datatable.SpellDatatableRepository;
import club.dnd5.portal.util.SpecificationUtil;

@RestController
public class SpellApiConroller {
	private static final String[][] classesMap = { { "1", "Бард" }, { "2", "Волшебник" }, { "3", "Друид" },
			{ "4", "Жрец" }, { "5", "Колдун" }, { "6", "Паладин" }, { "7", "Следопыт" }, { "8", "Чародей" },
			{ "14", "Изобретатель" } };
	
	@Autowired
	private SpellDatatableRepository spellRepo;
	@Autowired
	private ClassRepository classRepository;
	@Autowired
	private ArchetypeSpellRepository archetypeSpellRepository;
	
	@PostMapping(value = "/api/v1/spells", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<SpellApi> getSpells(@RequestBody SpellRequesApi request) {
		Specification<Spell> specification = null;

		DataTablesInput input = new DataTablesInput();
		List<Column> columns = new ArrayList<Column>(3);
		Column column = new Column();
		column.setData("name");
		column.setName("name");
		column.setSearchable(Boolean.TRUE);
		column.setOrderable(Boolean.TRUE);
		column.setSearch(new Search("", Boolean.FALSE));
		columns.add(column);
		
		column = new Column();
		column.setData("englishName");
		column.setName("englishName");
		column.setSearch(new Search("", Boolean.FALSE));
		column.setSearchable(Boolean.TRUE);
		column.setOrderable(Boolean.TRUE);
		columns.add(column);
		
		column = new Column();
		column.setData("altName");
		column.setName("altName");
		column.setSearchable(Boolean.TRUE);
		column.setOrderable(Boolean.FALSE);

		columns.add(column);
		if (request.getOrders()!=null && !request.getOrders().isEmpty()) {
			
			specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
				List<Order> orders = request.getOrders().stream()
						.map(
							order -> "asc".equals(order.getDirection()) ? cb.asc(root.get(order.getField())) : cb.desc(root.get(order.getField()))
						)
						.collect(Collectors.toList());
				query.orderBy(orders);
				return cb.and();
			});
		}
		input.setColumns(columns);
		input.setLength(request.getLimit() != null ? request.getLimit() : -1);
		if (request.getPage() != null && request.getLimit()!=null) {
			input.setStart(request.getPage() * request.getLimit());	
		}
		if (request.getSearch() != null) {
			if (request.getSearch().getValue() != null && !request.getSearch().getValue().isEmpty()) {
				if (request.getSearch().getExact() != null && request.getSearch().getExact()) {
					specification = (root, query, cb) -> cb.equal(root.get("name"), request.getSearch().getValue().trim().toUpperCase());
				} else {
					input.getSearch().setValue(request.getSearch().getValue());
					input.getSearch().setRegex(Boolean.FALSE);
				}
			}
		}
		if (request.getFilter() != null) {
			if (!request.getFilter().getLevels().isEmpty()) {
				specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> root.get("level").in(request.getFilter().getLevels()));
			}
			if (!request.getFilter().getMyclass().isEmpty()) {
				specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
					Join<HeroClass, Spell> join = root.join("heroClass", JoinType.LEFT);
					query.distinct(true);
					return cb.and(join.get("id").in(request.getFilter().getMyclass()));
				});
			}
			if (!request.getFilter().getBooks().isEmpty()) {
				specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
					Join<Book, Spell> join = root.join("book", JoinType.INNER);
					return join.get("source").in(request.getFilter().getBooks());
				});
			}
		}
		return spellRepo.findAll(input, specification, specification, SpellApi::new).getData();
	}
	
	@PostMapping(value = "/api/v1/spells/{englishName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public SpellDetailApi getSpell(@PathVariable String englishName) {
		Spell spell = spellRepo.findByEnglishName(englishName.replace('_', ' '));
		SpellDetailApi spellApi = new SpellDetailApi(spell);
		List<Archetype> archetypes = archetypeSpellRepository.findAllBySpell(spell.getId());
		if (!archetypes.isEmpty()) {
			spellApi.setSubclasses(archetypes.stream().map(ReferenceClassApi::new).collect(Collectors.toList()));
		}
		List<Race> races = spellRepo.findAllRaceBySpell(spell.getId());
		if (!races.isEmpty()) {
			spellApi.setRaces(races.stream().map(ReferenceClassApi::new).collect(Collectors.toList()));
		}
		return spellApi;
	}
	
	@CrossOrigin
	@GetMapping(value = "/api/fvtt/v1/spells", produces = MediaType.APPLICATION_JSON_VALUE)
	public SpellsFvtt getSpells(String search, String exact){
		DataTablesInput input = new DataTablesInput();
		List<Column> columns = new ArrayList<Column>(3);
		Column column = new Column();
		column.setData("name");
		column.setName("name");
		column.setSearchable(Boolean.TRUE);
		column.setOrderable(Boolean.TRUE);
		column.setSearch(new Search("", Boolean.FALSE));
		columns.add(column);
		
		column = new Column();
		column.setData("englishName");
		column.setName("englishName");
		column.setSearch(new Search("", Boolean.FALSE));
		column.setSearchable(Boolean.TRUE);
		column.setOrderable(Boolean.TRUE);
		columns.add(column);
		
		column = new Column();
		column.setData("level");
		column.setName("level");
		column.setSearch(new Search("", Boolean.FALSE));
		column.setSearchable(Boolean.FALSE);
		column.setOrderable(Boolean.TRUE);
		columns.add(column);
		input.setColumns(columns);
		input.setLength(-1);
		Specification<Spell> specification = null;
		if (search != null) {
			if (exact != null) {
				specification = (root, query, cb) -> cb.equal(root.get("name"), search.trim().toUpperCase());
			} else {
				input.getSearch().setValue(search);
				input.getSearch().setRegex(Boolean.FALSE);
			}
		}
		return new SpellsFvtt(spellRepo.findAll(input, specification, specification, SpellFvtt::new).getData());
	}
	
	@PostMapping("/api/v1/filters/spells")
	public FilterApi getFilter() {
		FilterApi filters = new FilterApi();
		List<FilterApi> sources = new ArrayList<>();
		FilterApi spellMainFilter = new FilterApi("main");
		spellMainFilter.setValues(
				spellRepo.findBook(TypeBook.OFFICAL).stream()
				.map(book -> new FilterValueApi(book.getSource(), book.getSource(),	Boolean.TRUE, book.getName()))
				.collect(Collectors.toList()));
		sources.add(spellMainFilter);
		
		FilterApi settingFilter = new FilterApi("Сеттинги", "settings");
		settingFilter.setValues(
				spellRepo.findBook(TypeBook.SETTING).stream()
				.map(book -> new FilterValueApi(book.getSource(), book.getSource(),	Boolean.TRUE, book.getName()))
				.collect(Collectors.toList()));
		sources.add(settingFilter);
		
		FilterApi adventureFilter = new FilterApi("Приключения", "adventures");
		adventureFilter.setValues(
				spellRepo.findBook(TypeBook.MODULE).stream()
				.map(book -> new FilterValueApi(book.getSource(), book.getSource(),	Boolean.TRUE, book.getName()))
				.collect(Collectors.toList()));
		sources.add(adventureFilter);
		
		FilterApi homebrewFilter = new FilterApi("Homebrew", "homebrew");
		homebrewFilter.setValues(
				spellRepo.findBook(TypeBook.CUSTOM).stream()
				.map(book -> new FilterValueApi(book.getSource(), book.getSource(),	Boolean.TRUE, book.getName()))
				.collect(Collectors.toList()));
		sources.add(homebrewFilter);
		filters.setSources(sources);
		
		List<FilterApi> otherFilters = new ArrayList<>();
		
		otherFilters.add(getLevelsFilter(9));

		FilterApi spellClassFilter = new FilterApi("Классы", "class");
		spellClassFilter.setValues(IntStream.range(0, classesMap.length)
				 .mapToObj(indexSpellClass -> new FilterValueApi(classesMap[indexSpellClass][1], classesMap[indexSpellClass][0], Boolean.TRUE))
				 .collect(Collectors.toList()));
		otherFilters.add(spellClassFilter);
		
		FilterApi schoolSpellFilter = new FilterApi("Школа", "school");
		schoolSpellFilter.setValues(
				Arrays.stream(MagicSchool.values())
				 .map(school -> new FilterValueApi(school.getName(), school.name(), Boolean.TRUE))
				 .collect(Collectors.toList()));
		otherFilters.add(schoolSpellFilter);

		FilterApi ritualFilter = new FilterApi("Ритуал", "ritual");
		List<FilterValueApi> values = new ArrayList<>(2);
		values.add(new FilterValueApi("да", "yes", Boolean.TRUE));
		values.add(new FilterValueApi("нет", "no", Boolean.TRUE));
		ritualFilter.setValues(values);
		otherFilters.add(ritualFilter);

		FilterApi concentrationFilter = new FilterApi("Концентрация", "concentration");
		values = new ArrayList<>(2);
		values.add(new FilterValueApi("требуется", "yes", Boolean.TRUE));
		values.add(new FilterValueApi("не требуется", "no", Boolean.TRUE));
		concentrationFilter.setValues(values);
		otherFilters.add(concentrationFilter);

		FilterApi damageTypeFilter = new FilterApi("Тип урона", "damageType");
		damageTypeFilter.setValues(
				DamageType.getSpellDamage().stream()
				 .map(value -> new FilterValueApi(value.getCyrilicName(), value.name(), Boolean.TRUE))
				 .collect(Collectors.toList()));
		otherFilters.add(damageTypeFilter);
		
		FilterApi timecastFilter = new FilterApi("Время накладывания", "timecast");
		values = new ArrayList<>();
		values.add(new FilterValueApi("бонусное действие", "1 BONUS", Boolean.TRUE));
		values.add(new FilterValueApi("реакция", "1 REACTION", Boolean.TRUE));
		values.add(new FilterValueApi("действие", "1 ACTION", Boolean.TRUE));
		values.add(new FilterValueApi("ход", "1 ROUND", Boolean.TRUE));
		values.add(new FilterValueApi("1 минута", "1 MINUTE", Boolean.TRUE));
		values.add(new FilterValueApi("10 минут", "10 MINUTE", Boolean.TRUE));
		values.add(new FilterValueApi("1 час", "10 HOUR", Boolean.TRUE));
		values.add(new FilterValueApi("8 час", "8 HOUR", Boolean.TRUE));
		values.add(new FilterValueApi("12 час", "12 HOUR", Boolean.TRUE));
		values.add(new FilterValueApi("24 час", "24 HOUR", Boolean.TRUE));
		timecastFilter.setValues(values);
		otherFilters.add(timecastFilter);
		
		FilterApi distanceFilter = new FilterApi("Дистанция", "distance");
		values = new ArrayList<>();
		values.add(new FilterValueApi("на себя", "self", Boolean.TRUE));
		values.add(new FilterValueApi("касание", "tuch", Boolean.TRUE));
		values.add(new FilterValueApi("5 футов", "5 fit", Boolean.TRUE));
		values.add(new FilterValueApi("10 футов", "10 fit", Boolean.TRUE));
		values.add(new FilterValueApi("25 футов", "25 fit", Boolean.TRUE));
		values.add(new FilterValueApi("30 футов", "30 fit", Boolean.TRUE));
		values.add(new FilterValueApi("40 футов", "40 fit", Boolean.TRUE));
		values.add(new FilterValueApi("50 футов", "50 fit", Boolean.TRUE));
		values.add(new FilterValueApi("60 футов", "60 fit", Boolean.TRUE));
		values.add(new FilterValueApi("90 футов", "90 fit", Boolean.TRUE));
		values.add(new FilterValueApi("100 футов", "100 fit", Boolean.TRUE));
		values.add(new FilterValueApi("150 футов", "150 fit", Boolean.TRUE));
		values.add(new FilterValueApi("300 футов", "300 fit", Boolean.TRUE));
		values.add(new FilterValueApi("400 футов", "400 fit", Boolean.TRUE));
		values.add(new FilterValueApi("1 миля", "1 mile", Boolean.TRUE));
		values.add(new FilterValueApi("500 миль", "500 mile", Boolean.TRUE));
		values.add(new FilterValueApi("1000 миль", "1000 mile", Boolean.TRUE));
		distanceFilter.setValues(values);
		otherFilters.add(distanceFilter);
		
		FilterApi durationFilter = new FilterApi("Длительность", "duration");
		values = new ArrayList<>();
		values.add(new FilterValueApi("Мгновенная", "instant", Boolean.TRUE));
		values.add(new FilterValueApi("1 раунд", "round", Boolean.TRUE));
		values.add(new FilterValueApi("1 минута", "1 minute", Boolean.TRUE));
		values.add(new FilterValueApi("10 минут", "10 minute", Boolean.TRUE));
		values.add(new FilterValueApi("1 час", "1 hour", Boolean.TRUE));
		values.add(new FilterValueApi("8 часов", "8 hour", Boolean.TRUE));
		values.add(new FilterValueApi("12 часов", "12 hour", Boolean.TRUE));
		values.add(new FilterValueApi("24 часа", "24 hour", Boolean.TRUE));
		values.add(new FilterValueApi("1 день", "1 day", Boolean.TRUE));
		values.add(new FilterValueApi("7 дней", "7 day", Boolean.TRUE));
		values.add(new FilterValueApi("10 дней", "10 day", Boolean.TRUE));
		values.add(new FilterValueApi("1 год", "1 year", Boolean.TRUE));
		durationFilter.setValues(values);
		otherFilters.add(durationFilter);
		
		otherFilters.add(getCompomemtsFilter());
	
		filters.setOther(otherFilters);
		return filters;
	}
	
	@PostMapping("/api/v1/filters/spells/{englishClassName}")
	public FilterApi getByClassFilter(@PathVariable String englishClassName) {
		FilterApi filters = new FilterApi();

		HeroClass heroClass = classRepository.findByEnglishName(englishClassName.replace('_', ' '));
		List<FilterApi> otherFilters = new ArrayList<>();
		otherFilters.add(getLevelsFilter(heroClass.getSpellcasterType().getMaxSpellLevel()));
		otherFilters.add(getCompomemtsFilter());
		filters.setOther(otherFilters);
		
		List<FilterApi> customFilters = new ArrayList<>();
		FilterApi customFilter = new FilterApi();
		customFilter.setName("Классы");
		customFilter.setKey("class");

		FilterValueApi customValue = new FilterValueApi();
		customValue.setLabel(heroClass.getCapitalazeName());
		customValue.setDefaultValue(Boolean.TRUE);
		customValue.setKey(String.valueOf(heroClass.getId()));
		customFilter.setValues(Collections.singletonList(customValue));
		customFilters.add(customFilter);
		filters.setCustom(customFilters);
		return filters;
	}
	
	@PostMapping("/api/v1/filters/spells/{englishClassName}/{englishArchetypeName}")
	public FilterApi getByClassFilter(@PathVariable String englishClassName, @PathVariable String englishArchetypeName) {
		FilterApi filters = new FilterApi();

		HeroClass heroClass = classRepository.findByEnglishName(englishClassName.replace('_', ' '));
		List<FilterApi> otherFilters = new ArrayList<>();
		otherFilters.add(getLevelsFilter(heroClass.getSpellcasterType().getMaxSpellLevel()));
		otherFilters.add(getCompomemtsFilter());
		filters.setOther(otherFilters);
		
		List<FilterApi> customFilters = new ArrayList<>();
		FilterApi customFilter = new FilterApi();
		customFilter.setName("Классы");
		customFilter.setKey("class");

		FilterValueApi customValue = new FilterValueApi();
		customValue.setLabel(heroClass.getCapitalazeName());
		customValue.setDefaultValue(Boolean.TRUE);
		customValue.setKey(String.valueOf(heroClass.getId()));
		customFilter.setValues(Collections.singletonList(customValue));
		customFilters.add(customFilter);
		filters.setCustom(customFilters);
		return filters;
	}
	
	private FilterApi getLevelsFilter(int maxLevel) {
		FilterApi levelFilter = new FilterApi("Уровень", "level");
		levelFilter.setValues(IntStream.rangeClosed(0, maxLevel)
				 .mapToObj(level -> new FilterValueApi(level == 0 ? "заговор" : String.valueOf(level),  String.valueOf(level), Boolean.TRUE))
				 .collect(Collectors.toList()));
		return levelFilter;
	}
	
	private FilterApi getCompomemtsFilter() {
		FilterApi componentsSpellFilter = new FilterApi("Компоненты", "components");
		List<FilterValueApi> componentValues = new ArrayList<>();
		componentValues.add(new FilterValueApi("вербальный", "1", Boolean.TRUE));
		componentValues.add(new FilterValueApi("соматический", "2", Boolean.TRUE));
		componentValues.add(new FilterValueApi("материальный", "3", Boolean.TRUE));
		componentValues.add(new FilterValueApi("расходуемый", "4", Boolean.TRUE));
		componentValues.add(new FilterValueApi("не расходуемый", "5", Boolean.TRUE));
		
		componentsSpellFilter.setValues(componentValues);
		return componentsSpellFilter;
	}
}