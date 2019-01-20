package com.github.peterpwang.workerschedule.util;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.github.peterpwang.workerschedule.domain.Manager;
import com.github.peterpwang.workerschedule.domain.Schedule;
import com.github.peterpwang.workerschedule.domain.User;

public class Util {
	
	private static final Long MANAGER_ID = 10L;
	private static final String MANAGER_NAME = "peter";
	private static final Long USER_ID = 100L;
	private static final String USER_NAME = "user";
	
	public static Manager newManager(Long id, String name) {
		return new Manager(id, name, "pass", "ROLE_MANAGER");
	}
	
	public static User newUser(Long id, String name) {
		Manager manager = newManager(MANAGER_ID, MANAGER_NAME);
		return new User(id, name, "pass", "pass", null, 1, 0L, manager);
	}
	
	public static Schedule newSchedule(Long id, String name) {
		Manager manager = newManager(MANAGER_ID, MANAGER_NAME);
		User user = newUser(USER_ID, USER_NAME);
		return new Schedule(id, name, null, 20181010, 800, 1800, 1, 0L, user, manager);
	}

    public static byte[] toJson(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper.writeValueAsBytes(object);
    }
}