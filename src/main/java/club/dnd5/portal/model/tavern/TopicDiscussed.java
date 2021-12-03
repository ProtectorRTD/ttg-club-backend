package club.dnd5.portal.model.tavern;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "taverna_topics")
@Getter
@Setter
public class TopicDiscussed {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@Column(columnDefinition = "TEXT")
	private String name;
	private int visitors;
}
