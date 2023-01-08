package club.dnd5.portal.controller.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import club.dnd5.portal.dto.api.RequestApi;
import club.dnd5.portal.repository.SearchRepository;
import club.dnd5.portal.repository.datatable.BackgroundDatatableRepository;
import club.dnd5.portal.repository.datatable.OptionDatatableRepository;
import club.dnd5.portal.repository.datatable.SpellDatatableRepository;
import club.dnd5.portal.repository.datatable.TraitDatatableRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import club.dnd5.portal.dto.api.SearchApi;
import club.dnd5.portal.repository.classes.ClassRepository;
import club.dnd5.portal.repository.classes.RaceRepository;

@Tag(name = "Full search", description = "The search API")
@RestController
@RequestMapping("/api/v1/search")
public class FullSearchController {
	@Autowired
	private SearchRepository repository;

	@Operation(summary = "Gets search result", tags = "Full search")
	@PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public List search(@RequestBody RequestApi request){
		return repository.search(request.getSearch().getValue(), request.getPage(), request.getLimit());
	}
}