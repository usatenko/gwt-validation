package com.google.gwt.validation.client;

/*
GWT-Validation Framework - Annotation based validation for the GWT Framework

Copyright (C) 2008  Christopher Ruffalo

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Asserts that the field or method matches the given regex pattern
 * when validated.  This regex pattern should be written so that it
 * has the same functionality in Java and JavaScript.
 * 
 * @author chris
 *
 */
@Documented
@Target({METHOD, FIELD})
@ConstraintValidator(PatternValidator.class)
@Retention(RUNTIME)
public @interface Pattern {
	
	/**
	 * Message that is returned on validation failure.
	 * 
	 * @return
	 */
	public String message() default "{constraint.pattern}";
	
	
	/**
	 * Groups that the constraint belongs to
	 * 
	 * @return
	 */
	public String[] groups() default {};
	

	/**
	 * String value of the regex pattern to match against.  This should work
	 * the same in Java and JavaScript or validations may not occur properly.
	 * <br/>
	 * By default it matches anything. 
	 * 
	 * @return
	 */
	public String pattern() default ".*";
	
}