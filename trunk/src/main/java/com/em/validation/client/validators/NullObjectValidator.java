package com.em.validation.client.validators;

/*
GWT Validation Framework - A JSR-303 validation framework for GWT

(c) 2011 Eminent Minds, LLC
	- Chris Ruffalo

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

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.Null;

public class NullObjectValidator implements ConstraintValidator<Null,Object> {

	@Override
	public void initialize(Null constraintAnnotation) {
				
	}

	@Override
	public boolean isValid(Object value, ConstraintValidatorContext context) {
		return null == value;
	}


}