package club.dnd5.portal.model;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DamageType {
	FAIR("огонь"), //0
	COLD("холод"), //1
	LIGHTNING("электричество"), //2
	POISON("яд"),//3
	ACID("кислота"), //4
	SOUND("звук"),//5
	NECTOTIC("некротическая энергия"),//6
	PSYCHIC("психическая энергия"),//7

	BLUDGEONING("дробящий"), //8
	PIERCING ("колющий"),//9
	SLASHING ("рубящий"),//10
	PHYSICAL("дробящий, колющий и рубящий урон от немагических атак"),//11

	NO_NOSILVER("дробящий, колющий и рубящий урон от немагических атак, а также от немагического оружия, которое при этом не посеребрено"), //12
	NO_DAMAGE("без урона"), //13
	RADIANT("излучение"), //14
	NO_ADMANTIT("дробящий, колющий и рубящий урон от немагических атак, а также от немагического оружия, которое при этом не изготовлено из адамантина"), //15
	PHYSICAL_MAGIC("дробящий, колющий и рубящий урон от магического оружия"), //16
	PIERCING_GOOD("колющий от магического оружия, используемого добрыми существами"), //17
	MAGIC("урон от заклинаний"), //18
	DARK("дробящий, колющий и рубящий, пока находится в области тусклого света или тьмы"), //19
	FORCE("силовое поле"), //20
	METAL_WEAPON("дробящий, колющий и рубящий урон от оружия из металла"), //21
	VORPAL_SWORD("рубящий удар мечом головорубом"); //22

	private String cyrilicName;

	public static DamageType parse(String damageTypeString) {
		for (DamageType damageType : values()) {
			if (damageType.cyrilicName.equals(damageTypeString)) {
				return damageType;
			}
		}
		return null;
	}

	public static Set<DamageType> getVulnerability()
	{
		return EnumSet.of(BLUDGEONING, PIERCING, SLASHING, FAIR, COLD, LIGHTNING, POISON, ACID, SOUND, NECTOTIC, PSYCHIC, RADIANT, PIERCING_GOOD);
	}

	public static Set<DamageType> getResistance()
	{
		return EnumSet.of(BLUDGEONING, PIERCING, SLASHING, FAIR, COLD, LIGHTNING, POISON, ACID, SOUND, NECTOTIC,
				RADIANT, PHYSICAL, NO_ADMANTIT, PHYSICAL_MAGIC, NO_NOSILVER, PSYCHIC, MAGIC, DARK, FORCE);
	}

	public static Set<DamageType> getImmunity()
	{
		return EnumSet.of(BLUDGEONING, PIERCING, SLASHING, FAIR, COLD, LIGHTNING, POISON, ACID, SOUND, NECTOTIC,
				RADIANT, PHYSICAL, NO_ADMANTIT, FORCE, PSYCHIC);
	}

	public static Set<DamageType> getSpecial()
	{
		return EnumSet.of(PHYSICAL, NO_NOSILVER, NO_ADMANTIT, PHYSICAL_MAGIC);
	}

	public static List<DamageType> getSpellDamage(){
		return Arrays.asList(NO_DAMAGE, BLUDGEONING, PIERCING, SLASHING, FAIR, COLD, LIGHTNING, POISON, ACID, SOUND, NECTOTIC, RADIANT, FORCE, PSYCHIC);
	}

	public static Set<DamageType> getWeaponDamage() {
		return EnumSet.of(BLUDGEONING, PIERCING, SLASHING, NO_DAMAGE);
	}
}
