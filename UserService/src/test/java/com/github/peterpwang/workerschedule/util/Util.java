package com.github.peterpwang.workerschedule.util;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.peterpwang.workerschedule.domain.Manager;
import com.github.peterpwang.workerschedule.domain.User;

public class Util {

	public static Manager newManager(String name) {
		return new Manager(0L, name, "pass", "ROLE_MANAGER");
	}

	public static User newUser(String name, Manager manager) {
		return new User(0L, name, "pass", "pass", null, 1, 0L, manager);
	}

	public static byte[] toJson(Object object) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		return mapper.writeValueAsBytes(object);
	}
}