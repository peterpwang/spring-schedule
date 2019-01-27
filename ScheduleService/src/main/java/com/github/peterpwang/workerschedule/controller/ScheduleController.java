package com.github.peterpwang.workerschedule.controller;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.github.peterpwang.workerschedule.domain.Manager;
import com.github.peterpwang.workerschedule.domain.Schedule;
import com.github.peterpwang.workerschedule.domain.User;
import com.github.peterpwang.workerschedule.repository.ManagerRepository;
import com.github.peterpwang.workerschedule.service.ScheduleService;

import lombok.Data;

/**
 * Schedule controller class
 * @author Pei Wang
 *
 */
@RestController
@RequestMapping("/api")
@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
public class ScheduleController {

	@Autowired
	private ScheduleService service;

	@Autowired
	private ManagerRepository managerRepository;

	public ScheduleController(ScheduleService service) {
		this.service = service;
	}

	/**
	 * Find schedule list
	 * @param pageable
	 * @param assembler
	 * @return ResponseEntity
	 */
	@GetMapping(value = "/schedules", produces = "application/hal+json")
	public ResponseEntity<PagedResources<ScheduleResource>> findSchedule(Pageable pageable,
			PagedResourcesAssembler assembler) {
		Page<Schedule> schedules = service.findAll(pageable);

		PagedResources<ScheduleResource> resources = assembler.toResource(schedules, new ScheduleResourceAssembler());
		resources.removeLinks();
		resources.add(ControllerLinkBuilder.linkTo(ScheduleController.class).slash("/schedules").withRel("self"));

		List<Link> links = createLinks(schedules, "page", "size");
		resources.add(links);

		HttpHeaders responseHeaders = new HttpHeaders();
		return new ResponseEntity<>(resources, responseHeaders, HttpStatus.OK);
	}

	/**
	 * Find schedule list by Name
	 * @param pageable
	 * @param name
	 * @param assembler
	 * @return ResponseEntity
	 */
	@GetMapping(value = "/schedulesByName", produces = "application/hal+json")
	public ResponseEntity<PagedResources<ScheduleResource>> findSchedule(Pageable pageable, String name,
			PagedResourcesAssembler assembler) {
		Page<Schedule> schedules = service.findByNameIgnoreCaseContaining(pageable, name);

		PagedResources<ScheduleResource> resources = assembler.toResource(schedules, new ScheduleResourceAssembler());
		resources.removeLinks();
		resources.add(ControllerLinkBuilder.linkTo(ScheduleController.class).slash("/schedulesByName").withRel("self"));

		List<Link> links = createLinks(schedules, "page", "size");
		resources.add(links);

		HttpHeaders responseHeaders = new HttpHeaders();
		return new ResponseEntity<>(resources, responseHeaders, HttpStatus.OK);
	}

	/**
	 * New schedule
	 * @param newSchedule
	 * @return Schedule
	 */
	@PostMapping("/schedules")
	public Schedule newSchedule(@Valid @RequestBody Schedule newSchedule) {

		applyScheduleInformationUsingSecurityContext(newSchedule);
		return service.save(newSchedule);
	}

	/**
	 * Get schedule by id
	 * @param id
	 * @return ScheduleResource
	 */
	@GetMapping("/schedules/{id}")
	public ScheduleResource getSchedule(@PathVariable Long id) {
		Optional<Schedule> optional = service.findById(id);
		Schedule schedule = optional.isPresent() ? optional.get() : null;
		return new ScheduleResourceAssembler().toResource(schedule);
	}

	/**
	 * Update schedule
	 * @param newSchedule
	 * @param id
	 * @return Schedule
	 */
	@PutMapping("/schedules/{id}")
	public Schedule updateSchedule(@RequestBody Schedule newSchedule, @PathVariable Long id) {

		return service.findById(id).map(schedule -> {
			schedule.setName(newSchedule.getName());
			schedule.setDescription(newSchedule.getDescription());
			schedule.setDateSchedule(newSchedule.getDateSchedule());
			schedule.setTimeStart(newSchedule.getTimeStart());
			schedule.setTimeEnd(newSchedule.getTimeEnd());
			schedule.setActive(newSchedule.getActive());
			schedule.setUser(newSchedule.getUser());
			schedule.setManager(newSchedule.getManager());
			return service.save(schedule);
		}).orElseGet(() -> {
			newSchedule.setId(id);
			return service.save(newSchedule);
		});
	}

	/**
	 * Delete schedule
	 * @param id
	 */
	@DeleteMapping("/schedules/{id}")
	public void deleteSchedule(@PathVariable Long id) {
		service.deleteById(id);
	}

	/**
	 * Add manager into database where it is not present. Should be deleted in the future.
	 * @param schedule
	 */
	private void applyScheduleInformationUsingSecurityContext(Schedule schedule) {

		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		Manager manager = this.managerRepository.findByName(name);
		if (manager == null) {
			Manager newManager = new Manager(0L, name, "", "ROLE_MANAGER");
			manager = this.managerRepository.save(newManager);
		}
		schedule.setManager(manager);
	}

	/**
	 * Create paganation links
	 * @param page
	 * @param pageParam
	 * @param sizeParam
	 * @return List<Link>
	 */
	public static <T> List<Link> createLinks(Page<T> page, String pageParam, String sizeParam) {
		List<Link> links = new LinkedList<>();
		addPreviousLink(links, page, pageParam, sizeParam);
		addNextLink(links, page, pageParam, sizeParam);
		addFirstLink(links, page, pageParam, sizeParam);
		addLastLink(links, page, pageParam, sizeParam);

		return links;
	}

	/**
	 * Create previous page link
	 * @param links
	 * @param page
	 * @param pageParam
	 * @param sizeParam
	 */
	private static <T> void addPreviousLink(List<Link> links, Page<T> page, String pageParam, String sizeParam) {
		if (page.hasPrevious()) {
			Link link = buildPageLink(pageParam, page.getNumber() - 1, sizeParam, page.getSize(), Link.REL_PREVIOUS);
			links.add(link);
		}
	}

	/**
	 * Create next page link
	 * @param links
	 * @param page
	 * @param pageParam
	 * @param sizeParam
	 */
	private static <T> void addNextLink(List<Link> links, Page<T> page, String pageParam, String sizeParam) {
		if (page.hasNext()) {
			Link link = buildPageLink(pageParam, page.getNumber() + 1, sizeParam, page.getSize(), Link.REL_NEXT);
			links.add(link);
		}
	}

	/**
	 * Create first page link
	 * @param links
	 * @param page
	 * @param pageParam
	 * @param sizeParam
	 */
	private static <T> void addFirstLink(List<Link> links, Page<T> page, String pageParam, String sizeParam) {
		Link link = buildPageLink(pageParam, 0, sizeParam, page.getSize(), Link.REL_FIRST);
		links.add(link);
	}

	/**
	 * Create last page link
	 * @param links
	 * @param page
	 * @param pageParam
	 * @param sizeParam
	 */
	private static <T> void addLastLink(List<Link> links, Page<T> page, String pageParam, String sizeParam) {
		Link link = buildPageLink(pageParam, page.getTotalPages() - 1, sizeParam, page.getSize(), Link.REL_LAST);
		links.add(link);
	}

	
	/**
	 * Create a specific page link
	 * @param pageParam
	 * @param page
	 * @param sizeParam
	 * @param size
	 * @param rel
	 * @return Link
	 */
	private static Link buildPageLink(String pageParam, int page, String sizeParam, int size, String rel) {
		String path = createBuilder().queryParam(pageParam, page).queryParam(sizeParam, size).build().toUriString();
		Link link = new Link(path, rel);
		return link;
	}

	/**
	 * Create URI builder
	 * @return ServletUriComponentsBuilder
	 */
	private static ServletUriComponentsBuilder createBuilder() {
		return ServletUriComponentsBuilder.fromCurrentRequestUri();
	}
}

/**
 * Schedule resource class
 * @author Pei Wang
 *
 */
@Data
class ScheduleResource extends ResourceSupport {

	private String name;
	private String description;
	private Integer dateSchedule;
	private Integer timeStart;
	private Integer timeEnd;
	private Integer active;
	private User user;
	private Manager manager;
}

/**
 * Schedule resource assembler
 * @author Pei Wang
 *
 */
class ScheduleResourceAssembler extends ResourceAssemblerSupport<Schedule, ScheduleResource> {
	public ScheduleResourceAssembler() {
		super(ScheduleController.class, ScheduleResource.class);
	}

	/**
	 * Converter from Schedule to ScheduleResource
	 */
	@Override
	public ScheduleResource toResource(Schedule entity) {
		ScheduleResource er = super.createResourceWithId(entity.getId(), entity);

		// Add and remove links
		er.removeLinks();
		er.add(ControllerLinkBuilder.linkTo(ScheduleController.class).slash("/schedules").slash(entity.getId())
				.withRel("self"));
		er.add(ControllerLinkBuilder.linkTo(ScheduleController.class).slash("/schedules").slash(entity.getId())
				.withRel("schedule"));

		er.setName(entity.getName());
		er.setDescription(entity.getDescription());
		er.setDateSchedule(entity.getDateSchedule());
		er.setTimeStart(entity.getTimeStart());
		er.setTimeEnd(entity.getTimeEnd());
		er.setActive(entity.getActive());
		er.setUser(entity.getUser());
		er.setManager(entity.getManager());
		return er;
	}
}