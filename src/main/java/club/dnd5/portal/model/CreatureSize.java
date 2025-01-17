package club.dnd5.portal.model;

import lombok.Getter;

import java.util.EnumSet;
import java.util.Set;

@Getter
public enum CreatureSize {
	TINY("Крошечный","Крошечная", "Крошечное"), // 0
	SMALL("Маленький", "Маленькая", "Маленькое"), // 1
	MEDIUM("Средний", "Средняя", "Среднее"), // 2
	LARGE("Большой", "Большая", "Большое"), // 3
	HUGE("Огромный", "Огромная", "Огромное"), // 4
	GARGANTUAN("Громадный", "Громадная", "Громадное"), //5
	SMALL_MEDIUM("Средний или Маленький");

	private final String [] names;
	CreatureSize(String... names){
		this.names = names;
	}

	public static CreatureSize parse(String size) {
		for (CreatureSize creatureSize : values()) {
			for (String sizeName : creatureSize.names) {
				if (sizeName.equalsIgnoreCase(size)) {
					return creatureSize;
				}
			}
		}
		return null;
	}
	public static Set<CreatureSize> getFilterSizes(){
		return EnumSet.of(TINY, SMALL, MEDIUM, LARGE, HUGE, GARGANTUAN);
	}

	public String getSizeName(CreatureType type) {
		switch (type) {
		case ABERRATION:
		case FEY:
		case OOZE:
		case UNDEAD:
		case SLIME:
		case SMALL_BEAST:
			return names[1];
		case FIEND:
		case PLANT:
			return names[2];
		default:
			return names[0];
		}
	}

	public String getCyrillicName() {
		return names[0];
	}

	public String getCell() {
		switch (this) {
		case TINY: return "1/4 клетки";
		case SMALL: return "1 клетка";
		case MEDIUM: return "1 клетка";
		case LARGE: return "2x2 клетки";
		case HUGE: return "3x3 клетки";
		case GARGANTUAN: return "4х4 клетки или больше";
		default: return "-";
		}
	}
}
