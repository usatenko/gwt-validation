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

import com.google.gwt.validation.client.interfaces.IInvalidConstraint;

/**
 * This class is for denoting instances where a particular object 
 * has failed the validation.  These are generated by the validator
 * as part of the validation process.
 * 
 * @author chris
 *
 */
public class InvalidConstraint<T> implements IInvalidConstraint<T> {

	private String itemName;
	private String message;
	
	//objects
	private T rootObject;
	private Object value;
	
	//property path
	private String propertyPath;
	
	public InvalidConstraint(String itemName, String message) {
		
		this.itemName = itemName;
		this.message = message;
		
		this.propertyPath = this.itemName;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setInvalidObject(T rootObject) {
		this.rootObject = rootObject;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public void setPropertyPath(String propertyPath) {
		this.propertyPath = propertyPath;
	}

	@Override
	public String toString() {
		return this.propertyPath + " : " + this.message;
	}

	public T getInvalidObject() {
		return this.rootObject;
	}

	public String getPropertyPath() {
		if(this.propertyPath == null || this.propertyPath.trim().length() == 0) return this.itemName;
		
		return this.propertyPath;
	}

	public Object getValue() {
		return this.value;
	}
	
}
