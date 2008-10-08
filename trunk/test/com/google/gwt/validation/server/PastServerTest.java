package com.google.gwt.validation.server;

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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Test;

import com.google.gwt.validation.client.PastValidator;

public class PastServerTest {

	@Test
	public void testInFuture() {
		
		//create future validator
		PastValidator pv = new PastValidator();
		
		//create future date
		Date d = new Date((new Date()).getTime() + 10000000);
	
		//test
		assertFalse("New date is in future", pv.isValid(d));
	}
	
	@Test
	public void testInPast() {

		//create future validator
		PastValidator pv = new PastValidator();
		
		//create past date
		Date d = new Date(0);
	
		//test
		assertTrue("New date is in past", pv.isValid(d));		
	}
	
}
