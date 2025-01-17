package club.dnd5.portal.dto.api.spells;

import club.dnd5.portal.model.AbilityType;
import club.dnd5.portal.model.DamageType;
import club.dnd5.portal.model.splells.Spell;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@JsonInclude(Include.NON_NULL)

@JsonPropertyOrder({ "name", "source", "page", "level" ,"school","time", "range", "components", "duration", "meta", "entries" })

@Getter
@Setter
public class SpellFvtt implements Serializable {
	private static final long serialVersionUID = 6266015163866595679L;
	private String id;
	private String name;
    private String englishName;
    private String altName;
    private byte level;
    private String school;
    @JsonProperty("time")
    private List<Timecast> timecast;
    private Range range;
    private Components components;
    private List<Duration> duration;
    private Classes classes;
    private String source;
    private List<String> entries;
    private Short page;
    private List<String> damageInflict;
    private List<String> savingThrow;
    private EntriesHigherLevel entriesHigherLevel;
    private Meta meta;

	public SpellFvtt(Spell spell) {
		this.id = spell.getEnglishName().toLowerCase().replace(' ', '_');
		this.name = spell.getName();
		this.englishName = spell.getEnglishName();
		this.level = spell.getLevel();
		this.school = spell.getSchool().name().toLowerCase();
		this.source = spell.getBook().getSource();
		if (spell.getPage() != null) {
			this.page = spell.getPage();
		}
		if (!spell.getDamageType().isEmpty()) {
			this.damageInflict = spell.getDamageType()
					.stream()
					.map(DamageType::name)
					.map(String::toLowerCase)
					.collect(Collectors.toList());
		}
		components = new Components();
		if (spell.getVerbalComponent()) {
			components.setV(true);
		}
		if (spell.getSomaticComponent()) {
			components.setS(true);
		}
		if (Objects.nonNull(spell.getAdditionalMaterialComponent())) {
			components.setM(spell.getAdditionalMaterialComponent());
		}
		this.range = new Range(spell);
		String durationText = spell.getDuration();
		duration = Arrays.stream(durationText.split(" или ")).map(Duration::new).collect(Collectors.toList());
		this.timecast = spell.getTimes()
			.stream()
			.map(Timecast::new)
			.collect(Collectors.toList());
		this.entries = new ArrayList<>(2);
		this.entries.addAll(Arrays
			.stream(spell.getDescription().replace("<p>", "").split("</p>"))
			.map(t->t.replace("\\\"", ""))
			.map(t->t.replace("href=\"", "href=\"https://ttg.club"))
			.filter(t -> !t.isEmpty())
			.collect(Collectors.toList()));
		this.classes = new Classes(spell.getHeroClass());
		if (spell.getRitual()) {
			meta = new Meta();
			meta.setRitual(true);
		}
		if (spell.getUpperLevel() != null) {
			this.entriesHigherLevel = new EntriesHigherLevel(spell.getUpperLevel());
		}
	    Matcher matcher = Pattern.compile("(<span class=\"saving_throw\">\\W+<\\/span>)+").matcher(spell.getDescription());
	    if(matcher.matches()) {
	    	this.savingThrow = new ArrayList<>(matcher.groupCount());
	    	while (matcher.find()) {
				String ability = AbilityType.parse(matcher.group()).getCapitalizeName().toLowerCase();
				savingThrow.add(ability);
			}
	    }
	}
}
