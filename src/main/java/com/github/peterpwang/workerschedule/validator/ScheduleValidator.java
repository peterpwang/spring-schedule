package com.github.peterpwang.workerschedule.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.github.peterpwang.workerschedule.domain.Schedule;

/**
 * Schedule validator
 * @author Pei Wang
 */
@Component("beforeCreateScheduleValidator")
public class ScheduleValidator implements Validator {
 
    @Override
    public boolean supports(Class<?> clazz) {
        return Schedule.class.equals(clazz);
    }
 
    @Override
    public void validate(Object obj, Errors errors) {
		Schedule schedule = (Schedule) obj;

        if (Checker.checkEmpty(schedule.getName())) {
            errors.rejectValue("name", "schedule.name.notNull");
        }
        if (Checker.checkSize(schedule.getName(), 1, 60)) {
            errors.rejectValue("name", "schedule.name.size");
        }

        if (Checker.checkSize(schedule.getDescription(), 0, 60)) {
            errors.rejectValue("description", "schedule.description.size");
        }
    
        if (Checker.checkEmpty(schedule.getActive())) {
            errors.rejectValue("active", "schedule.active.notNull");
        }
        if (Checker.checkZeroOrPositive(schedule.getActive())) {
            errors.rejectValue("active", "schedule.active.number");
        }

		if (schedule.getUser() == null)
		{
            errors.rejectValue("user", "schedule.user.notNull");
		}
    }
 }