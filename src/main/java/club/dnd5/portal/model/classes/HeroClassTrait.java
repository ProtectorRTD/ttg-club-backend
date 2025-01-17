package club.dnd5.portal.model.classes;

import club.dnd5.portal.model.book.Book;
import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "hero_class_traits")
@Data
public class HeroClassTrait {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String name;
	private byte level;
	@Column(columnDefinition = "TEXT")
	private String description;

	@ManyToOne(targetEntity = HeroClass.class)
	private HeroClass heroClass;

	private boolean architype;
	private String child;
	private int optional;

	@ManyToOne
	@JoinColumn(name = "source")
	private Book book;
}
