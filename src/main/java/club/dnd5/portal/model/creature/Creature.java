package club.dnd5.portal.model.creature;

import club.dnd5.portal.model.*;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.util.ChallengeRating;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Существо
 */
@Getter
@Setter

@Entity
@Table(name = "creatures")
public class Creature {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@Column(nullable = false)
	private String name;
	private String altName;
	private String englishName;

	@Enumerated(EnumType.ORDINAL)
	private CreatureSize size;

	@Enumerated(EnumType.ORDINAL)
	private CreatureType type;

	private Integer raceId;
	private String raceName;

	@Enumerated(EnumType.ORDINAL)
	private Alignment alignment;

	@Column(nullable = false)
	private byte AC;
	private String bonusAC;

	@ElementCollection
	@Enumerated(EnumType.STRING)
	private List<ArmorType> armorTypes;

	private short averageHp;
	private Short countDiceHp;

	@Enumerated(EnumType.ORDINAL)
	private Dice diceHp;

	private Short bonusHP;

	private String suffixHP;

	private byte speed = 30;

	private Short flySpeed;

	private Short hover;

	private Short swimmingSpped;

	private Short climbingSpeed;

	private Short diggingSpeed;

	private String speedText;

	// Абилки
	@Column(nullable = false)
	private byte strength = 10;
	@Column(nullable = false)
	private byte dexterity = 10;
	@Column(nullable = false)
	private byte constitution = 10;
	@Column(nullable = false)
	private byte intellect = 10;
	@Column(nullable = false)
	private byte wizdom = 10;
	@Column(nullable = false)
	private byte charisma = 10;

	@ElementCollection
    @CollectionTable(name = "creature_immunity_states", joinColumns = @JoinColumn(name = "creature_id"))
	@Enumerated(EnumType.ORDINAL)
	private List<Condition> immunityStates;

	@ElementCollection
	@Enumerated(EnumType.ORDINAL)
	private List<DamageType> immunityDamages;

	@ElementCollection
	@Enumerated(EnumType.ORDINAL)
	private List<DamageType> resistanceDamages;

	@ElementCollection
	@Enumerated(EnumType.ORDINAL)
	private List<DamageType> vulnerabilityDamages;

	private Integer darkvision;
	private Integer trysight;
	private Integer vibration;
	private Integer blindsight;
	private Integer blindsightRadius;

	private byte passivePerception;
	private String passivePerceptionBonus;

	// опыт
	private int exp;
	// уровень опасности
	private String challengeRating;

	private String proficiencyBonus;

	// спаброски
	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "creature_id")
	private List<SavingThrow> savingThrows;

	// навыки
	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "creature_id")
	private List<Skill> skills;

	@ManyToMany
	private List<Language> languages;

	@OneToMany(cascade = CascadeType.ALL)
	private List<CreatureFeat> feats;

	@ManyToMany(cascade = CascadeType.ALL)
	private List<Action> actions;

	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name = "creature_id")
	private List<CreatureSpell> spells;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(columnDefinition = "TEXT")
	private String legendary;

	@Column(columnDefinition = "TEXT")
	private String reaction;

	@ManyToMany
	private List<CreatureRace> races;

	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name = "creature_id")
	private List<Spellcater> spellcasters;

	@ElementCollection
	@Enumerated(EnumType.STRING)
	private List<HabitatType> habitates;

	@OneToOne
	@JoinColumn(name = "lair_id")
	private Lair lair;

	@ManyToOne
	@JoinColumn(name = "source")
	private Book book;
	private Short page;

	private String img;

	public void addFeat(CreatureFeat feat) {
		feats.add(feat);
	}

	public String getSizeName() {
		return size.getSizeName(type);
	}

	public String getAligment() {
		return alignment.getName(type);
	}

	public Alignment getAligmentRaw(){
		return alignment;
	}

	public String strengthText() {
		return getFormatAbility(strength);
	}

	public String dexterityText() {
		return getFormatAbility(dexterity);
	}

	public String constitutionText() {
		return getFormatAbility(constitution);
	}

	public String intellectText() {
		return getFormatAbility(intellect);
	}

	public String wizdomText() {
		return getFormatAbility(wizdom);
	}

	public String charismaText() {
		return getFormatAbility(charisma);
	}

	private String getFormatAbility(byte ability) {
		return String.format("%d (%+d)", ability, (ability - 10) < 0 ? (ability - 11) / 2 : (ability - 10) / 2);
	}

	public String getArmorTypeString() {
		return armorTypes.stream().map(ArmorType::getCyrillicName).collect(Collectors.joining(", "));
	}

	public String getHp() {
		if (bonusHP == null && diceHp == null && suffixHP == null) {
			return String.format("%d", averageHp);
		}
		if (bonusHP == null && diceHp == null && suffixHP != null) {
			return String.format("%d %s", averageHp, suffixHP);
		}
		if (bonusHP == null) {
			return String.format("%d (%d%s)", averageHp, countDiceHp, diceHp.getName());
		}
		return String.format("%d (%d%s %s %d)", averageHp, countDiceHp, diceHp.getName(), bonusHP >=0 ? "+" : "-", Math.abs(bonusHP));
	}

	public String getHpFormula() {
		if (bonusHP == null && diceHp == null && suffixHP == null) {
			return String.format("%d", averageHp);
		}
		if (bonusHP == null && diceHp == null && suffixHP != null) {
			return String.format("%d %s", averageHp, suffixHP);
		}
		if (bonusHP == null) {
			return String.format("%d%s", countDiceHp, diceHp.name());
		}
		return String.format("%d%s%s%d", countDiceHp, diceHp.name(),  bonusHP>=0 ? "+" : "-", Math.abs(bonusHP));
	}

	public String getSense() {
		List<String> sense = new ArrayList<>(5);
		if (blindsight != null) {
			String blind = String.format("слепое зрение %d фт.", blindsight);
			if (blindsightRadius != null) {
				blind += " (слеп за пределами этого радиуса)";
			}
			sense.add(blind);
		}
		if (darkvision != null) {
			String darkvis = String.format("тёмное зрение %d фт.", darkvision);
			if (blindsightRadius != null) {
				darkvis += " (слеп за пределами этого радиуса)";
			}
			sense.add(darkvis);
		}
		if (trysight != null) {
			sense.add(String.format("истинное зрение %d фт.", trysight));
		}
		if (vibration != null) {
			sense.add(String.format("чувство вибрации %d фт.", vibration));
		}
		return String.join(", ", sense);
	}

	public String getAllSpeed() {
		return String.format("%d фт.", speed) + (flySpeed == null ? "" : String.format(", летая %d фт.", flySpeed))
				+ (hover != null && hover == 1 ? " (парит)" : "")
				+ (swimmingSpped == null ? "" : String.format(", плавая %d фт.", swimmingSpped))
				+ (diggingSpeed == null ? "" : String.format(", копая %d фт.", diggingSpeed))
				+ (climbingSpeed == null ? "" : String.format(", лазая %d фт.", climbingSpeed));
	}

	public String getAllSpeedEnglish() {
		return String.format("%d ft.", speed) + (flySpeed == null ? "" : String.format(", fly %d ft.", flySpeed))
				+ (swimmingSpped == null ? "" : String.format(", swim %d ft.", swimmingSpped))
				+ (diggingSpeed == null ? "" : String.format(", burrow %d ft.", diggingSpeed))
				+ (climbingSpeed == null ? "" : String.format(", climb %d ft.", climbingSpeed));
	}

	public List<Action> getActions(ActionType type){
		return actions.stream()
			.filter(a -> a.getActionType() == type)
			.collect(Collectors.toList());
	}

	public List<Action> getActions(){
 		return actions;
	}

	public List<Action> getReactions(){
		return actions.stream().filter(a -> a.getActionType() == ActionType.REACTION).collect(Collectors.toList());
	}

	public List<Action> getBonusActions(){
		return actions.stream().filter(a -> a.getActionType() == ActionType.BONUS).collect(Collectors.toList());
	}

	public List<Action> getLegendaries(){
		return actions.stream().filter(a -> a.getActionType() == ActionType.LEGENDARY).collect(Collectors.toList());
	}

	public Byte getSavingThrow(AbilityType abilityType) {
		return savingThrows.stream().filter(st-> st.getAbility() == abilityType).map(SavingThrow::getBonus).findFirst().orElse(null);
	}

	public Byte getSkillBonus(SkillType skillType) {
		return skills.stream().filter(st-> st.getType() == skillType).map(Skill::getBonus).findFirst().orElse(null);
	}

	public int getBonusHpAbs() {
		return bonusHP == null ? 0 : Math.abs(bonusHP);
	}

	public String getUrlName() {
		return englishName.toLowerCase().replace(' ', '_');
	}

	public String getProficiencyBonus() {
		if (Objects.isNull(proficiencyBonus)) {
			return ChallengeRating.getProficiencyBonus(challengeRating);
		}
		return proficiencyBonus;
	}
}
