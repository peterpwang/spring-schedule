package com.github.peterpwang.workerschedule.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleAfterDelete;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.hateoas.EntityLinks;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.github.peterpwang.workerschedule.configuration.WebSocketConfiguration;
import com.github.peterpwang.workerschedule.domain.Manager;
import com.github.peterpwang.workerschedule.domain.User;
import com.github.peterpwang.workerschedule.repository.ManagerRepository;

/**
 * User repository event handler
 * @author Pei Wang
 *
 */
@Component
@RepositoryEventHandler(User.class)
public class UserRestEventHandler {

	private final SimpMessagingTemplate websocket;

	private final EntityLinks entityLinks;

	private final ManagerRepository managerRepository;

	@Autowired
	public UserRestEventHandler(SimpMessagingTemplate websocket, EntityLinks entityLinks,
			ManagerRepository managerRepository) {
		this.websocket = websocket;
		this.entityLinks = entityLinks;
		this.managerRepository = managerRepository;
	}

	/**
	 * Add manager information to user object before create and save
	 * @param user
	 */
	@HandleBeforeCreate
	@HandleBeforeSave
	public void applyUserInformationUsingSecurityContext(User user) {

		String name = (String)SecurityContextHolder.getContext().getAuthentication().getName();
		Manager manager = this.managerRepository.findByName(name);
		if (manager == null) {
			Manager newManager = new Manager(0L, name, "", "ROLE_MANAGER");
			manager = this.managerRepository.save(newManager);
		}
		user.setManager(manager);
	}

	/**
	 * Notify clients after creation
	 * @param user
	 */
	@HandleAfterCreate
	public void newUser(User user) {
		this.websocket.convertAndSend(WebSocketConfiguration.MESSAGE_PREFIX + "/newUser", getUserPath(user));
	}

	/**
	 * Notify clients after delete
	 * @param user
	 */
	@HandleAfterDelete
	public void deleteUser(User user) {
		this.websocket.convertAndSend(WebSocketConfiguration.MESSAGE_PREFIX + "/deleteUser", getUserPath(user));
	}

	/**
	 * Notify clients after update
	 * @param user
	 */
	@HandleAfterSave
	public void updateUser(User user) {
		this.websocket.convertAndSend(WebSocketConfiguration.MESSAGE_PREFIX + "/updateUser", getUserPath(user));
	}

	/**
	 * Take an {@link User} and get the URI using Spring Data REST's
	 * {@link EntityLinks}.
	 *
	 * @param user
	 */
	private String getUserPath(User user) {
		return this.entityLinks.linkForSingleResource(user.getClass(), user.getId()).toUri().getPath();
	}
}
