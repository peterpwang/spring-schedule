package com.github.peterpwang.workerschedule.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.rest.core.RepositoryConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Repository exception handler
 * @author Pei Wang
 *
 */
@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

	@Autowired
	private ApplicationContext applicationContext;

	/**
	 * For repository constraint exception, return HTTP 400 status with error messages.
	 * @param ex
	 * @param request
	 * @return
	 */
	@ExceptionHandler({ RepositoryConstraintViolationException.class })
	public ResponseEntity<Object> handleAccessDeniedException(Exception ex, WebRequest request) {
		RepositoryConstraintViolationException nevEx = (RepositoryConstraintViolationException) ex;

		List<String> errors = new ArrayList<String>();
		for (ObjectError violation : nevEx.getErrors().getAllErrors()) {
			// Read error messages from application message file
			errors.add(
					applicationContext.getMessage(violation.getCode(), violation.getArguments(), Locale.getDefault()));
		}

		return new ResponseEntity<Object>(errors, new HttpHeaders(), HttpStatus.BAD_REQUEST);
	}
}
