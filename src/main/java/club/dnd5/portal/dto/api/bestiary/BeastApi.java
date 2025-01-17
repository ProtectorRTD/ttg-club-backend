package club.dnd5.portal.dto.api.bestiary;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import club.dnd5.portal.dto.api.SourceApi;
import club.dnd5.portal.dto.api.classes.NameApi;
import club.dnd5.portal.model.creature.Creature;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonInclude(Include.NON_NULL)

@NoArgsConstructor
@Getter
@Setter
public class BeastApi {
	private NameApi name;
	protected Object type;
	private String challengeRating;
	protected String url;
	protected SourceApi source;

	public BeastApi(Creature beast) {
		name = new NameApi(beast.getName(), beast.getEnglishName());
		type = beast.getType().getCyrillicName();
		challengeRating = beast.getChallengeRating();
		url = String.format("/bestiary/%s", beast.getUrlName());
		source = new SourceApi(beast.getBook());
	}
}
