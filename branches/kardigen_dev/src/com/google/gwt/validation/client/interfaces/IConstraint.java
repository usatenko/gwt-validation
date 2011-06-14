package com.google.gwt.validation.client.interfaces;

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

import java.lang.annotation.Annotation;

/**
 * Define the logic to validate a given constraint
 * <br/>
 * (Taken from JSR-303 example implementation in specification document.)
 */
public interface IConstraint<A extends Annotation> {
	
	/**
	 * Initialize the constraint validator.
	 *
	 * @param constraintAnnotation The constraint declaration
	 */
	void initialize(A constraintAnnotation);
	
	
	/**
 	 * Evaluates the constraint against a value. This method
 	 * must be thread safe.
	 * 
	 * @param value The object to validate
	 * @return false if the value is not valid, true otherwise
	 * @exception IllegalArgumentException The value's type isn't understood
	 * by the constraint validator
	 */
	boolean isValid(Object value);	
}