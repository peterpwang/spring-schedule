package com.github.peterpwang.workerschedule.domain;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@Entity
@Table(name = "schedules")
public class Schedule {

	private @Id @GeneratedValue Long id;
	
	@NotNull(message = "{schedule.name.notNull}")
	@Size(min = 1, max = 60, message = "{schedule.name.size}")
	private String name;
	
	@Size(max = 60, message = "{schedule.description.size}")
	private String description;

	@Positive(message = "{schedule.dateSchedule.number}")
	private Integer dateSchedule;

	@Positive(message = "{schedule.timeStart.number}")
	private Integer timeStart;

	@Positive(message = "{schedule.timeEnd.number}")
	private Integer timeEnd;

	@PositiveOrZero(message = "{schedule.active.number}")
	private Integer active;
	
	private @Version @JsonIgnore Long version;
	
	private @ManyToOne User user;
	
	private @ManyToOne Manager manager;

	private Schedule() {}

	public Schedule(Long id, String name, String description, Integer dateSchedule, Integer timeStart, Integer timeEnd, Integer active, Long version, User user, Manager manager) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.dateSchedule = dateSchedule;
		this.timeStart = timeStart;
		this.timeEnd = timeEnd;
		this.active = active;
		this.version = version;
		this.user = user;
		this.manager = manager;
	}
}