package com.google.gwt.validation.server;

/*
GWT-Validation Framework - Annotation based validation for the GWT Framework

Copyright (C) 2008  Christopher Ruffalo

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.google.gwt.validation.client.EmailValidator;

public class EmailServerTest {
	
	@Test
	public void testEmail() {
		
		//create email validator
		EmailValidator ev = new EmailValidator();
		
		//good emails
		assertTrue("Good Email.",ev.isValid("person@place.com"));
		assertTrue("Good Email.",ev.isValid("person.thing@place.info"));
		assertTrue("Good Email.",ev.isValid("person2_a_place@intoit.edu"));
		assertTrue("Good Email.",ev.isValid("t12h12in12g@citadel.edu"));
		assertTrue("Good Email.",ev.isValid("force_it+work@gmail.com"));
		assertTrue("Good Email.",ev.isValid("areallylongemail12345____51256.email@email.com"));
		
		//bad emails
		assertFalse("Bad email.",ev.isValid("really.com"));
		assertFalse("Bad email.",ev.isValid("not_really21@@@.come@"));
		assertFalse("Bad email.",ev.isValid("asdf@asdf"));
		assertFalse("Bad email.",ev.isValid("not@com@com"));
		assertFalse("Bad email.",ev.isValid("not_gmail.com"));
		assertFalse("Bad email.",ev.isValid("vlkajlkj@@@aldjkflsdkfj.com"));		
		
	}
	
}
