package com.em.validation.client.reflector;

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

import com.em.validation.rebind.reflector.factory.RuntimeReflectorFactory;

public enum ReflectorFactory implements IReflectorFactory {
	
	INSTANCE;
	
	private ReflectorFactory(){
	
	}

	@Override
	public <T> IReflector<T> getReflector(Class<? extends T> targetClass) {
		//at runtime, delegate to a class that uses introspection and reflection
		//to create (and cache) reflectors
		return RuntimeReflectorFactory.INSTANCE.getReflector(targetClass);
	}

}
