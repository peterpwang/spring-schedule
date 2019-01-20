package com.github.peterpwang.workerschedule.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.github.peterpwang.workerschedule.domain.User;

@Component("beforeCreateUserValidator")
public class UserValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return User.class.equals(clazz);
	}

	@Override
	public void validate(Object obj, Errors errors) {
		User user = (User) obj;

		if (Checker.checkEmpty(user.getName())) {
			errors.rejectValue("name", "user.name.notNull");
		}
		if (Checker.checkSize(user.getName(), 1, 60)) {
			errors.rejectValue("name", "user.name.size");
		}

		if (Checker.checkEmpty(user.getPassword())) {
			errors.rejectValue("password", "user.password.notNull");
		}
		if (Checker.checkSize(user.getPassword(), 1, 60)) {
			errors.rejectValue("password", "user.password.size");
		}

		if (user.getPassword() != null
				&& (user.getPasswordRepeat() == null || user.getPasswordRepeat().compareTo(user.getPassword()) != 0)) {
			errors.rejectValue("password", "user.password.same");
		}

		if (Checker.checkSize(user.getDescription(), 0, 60)) {
			errors.rejectValue("description", "user.description.size");
		}

		if (Checker.checkEmpty(user.getActive())) {
			errors.rejectValue("active", "user.active.notNull");
		}
		if (Checker.checkZeroOrPositive(user.getActive())) {
			errors.rejectValue("active", "user.active.number");
		}
	}
}