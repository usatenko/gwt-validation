package com.em.validation.client.model.cascade;

/*
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


import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.em.validation.client.model.constraint.FakeConstraint;

public class Outside {

	@NotNull
	@Valid
	private Inside inside = new Inside();
	
	@FakeConstraint
	@NotNull
	private Inside noCascade = new Inside();

	public Inside getInside() {
		return inside;
	}

	public void setInside(Inside inside) {
		this.inside = inside;
	}

	public Inside getNoCascade() {
		return noCascade;
	}

	public void setNoCascade(Inside noCascade) {
		this.noCascade = noCascade;
	}
	
}