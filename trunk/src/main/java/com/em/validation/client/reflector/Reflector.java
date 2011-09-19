package com.em.validation.client.reflector;

/*
GWT Validation Framework - A JSR-303 validation framework for GWT

(c) gwt-validation contributors (http://code.google.com/p/gwt-validation/)

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


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.validation.metadata.ConstraintDescriptor;
import javax.validation.metadata.Scope;

public abstract class Reflector implements IReflector {

	/**
	 * Set of unique property names
	 */
	protected Set<String> properties = new HashSet<String>();
	
	/**
	 * Store the property return types
	 */
	protected Map<String,Class<?>> propertyTypes = new HashMap<String, Class<?>>();
	
	protected IReflector superReflector = null;
	protected Set<IReflector> reflectorInterfaces = new HashSet<IReflector>();
	
	/**
	 * Sequence of groups to use for default validation
	 * 
	 */
	protected Class<?>[] groupSequence = new Class<?>[]{};
	
	/**
	 * The target class of the reflector
	 * 
	 */
	protected Class<?> targetClass = null;
	
	public Class<?> getTargetClass() {
		return this.targetClass;
	}
	
	/**
	 * Get the bean accessible name (short name) of all of the publicly accessible methods contained in the given concrete class.
	 * 
	 * @return
	 */
	public Set<String> getPropertyNames() {
		Set<String> results = new HashSet<String>();
		
		results.addAll(this.getDeclaredPropertyNames());
		
		if(this.superReflector != null) {
			results.addAll(this.superReflector.getPropertyNames());
		}
		
		if(this.reflectorInterfaces != null && this.reflectorInterfaces.size() > 0) {
			for(IReflector iface : this.reflectorInterfaces) {
				if(iface != null) {
					results.addAll(iface.getPropertyNames());
				}
			}
		}
		
		return results;
	}
	
	public Set<String> getDeclaredPropertyNames() {
		return this.properties;
	}
	
	/**
	 * Direct access to the constraint descriptors, from which we can get constraints
	 * 
	 */
	protected Map<String, Set<ConstraintDescriptor<?>>> constraintDescriptors = new HashMap<String, Set<ConstraintDescriptor<?>>>();
	
	@Override
	public Set<ConstraintDescriptor<?>> getConstraintDescriptors(Scope scope) {
		Set<ConstraintDescriptor<?>> outputSet = new HashSet<ConstraintDescriptor<?>>();
		for(Set<ConstraintDescriptor<?>> partialSet : this.constraintDescriptors.values()) {
			outputSet.addAll(partialSet);
		}
		if(Scope.HIERARCHY.equals(scope)) {
			if(this.superReflector != null) {
				outputSet.addAll(this.superReflector.getConstraintDescriptors());
			}
			for(IReflector iface : this.reflectorInterfaces) {
				if(iface != null) {
					outputSet.addAll(iface.getConstraintDescriptors());
				}
			}
		}
		return outputSet;
	}
	
	@Override
	public Set<ConstraintDescriptor<?>> getConstraintDescriptors() {
		return this.getConstraintDescriptors(Scope.HIERARCHY);
	}
	
	@Override
	public Set<ConstraintDescriptor<?>> getClassConstraintDescriptors(Scope scope) {
		Set<ConstraintDescriptor<?>> classDescriptors = this.constraintDescriptors.get(this.targetClass.getName());
		if(classDescriptors == null) {
			classDescriptors = new HashSet<ConstraintDescriptor<?>>();
			this.constraintDescriptors.put(this.targetClass.getName(), classDescriptors);
		}
		if(Scope.HIERARCHY.equals(scope)) {
			if(this.superReflector != null) {
				classDescriptors.addAll(this.superReflector.getClassConstraintDescriptors());
			}
			for(IReflector iface : this.reflectorInterfaces) {
				classDescriptors.addAll(iface.getClassConstraintDescriptors());
			}
		}
		return classDescriptors;
	}
	
	@Override
	public Set<ConstraintDescriptor<?>> getClassConstraintDescriptors() {
		return this.getClassConstraintDescriptors(Scope.HIERARCHY);
	}

	@Override
	public Set<ConstraintDescriptor<?>> getConstraintDescriptors(String name, Scope scope) {
		Set<ConstraintDescriptor<?>> descriptors = this.constraintDescriptors.get(name);
		if(descriptors == null) {
			descriptors = new HashSet<ConstraintDescriptor<?>>();
			this.constraintDescriptors.put(name, descriptors);
		}
		if(Scope.HIERARCHY.equals(scope)) {
			if(this.superReflector != null) {
				descriptors.addAll(this.superReflector.getConstraintDescriptors(name));	
			}
			for(IReflector iface : this.reflectorInterfaces) {
				descriptors.addAll(iface.getConstraintDescriptors(name));
			}
		}		
		return this.constraintDescriptors.get(name);
	}

	@Override
	public Set<ConstraintDescriptor<?>> getConstraintDescriptors(String name){
		return this.getConstraintDescriptors(name, Scope.HIERARCHY);
	}

	protected Object getSuperValues(String name, Object target) {
		//check super classes
		Object value = null;
		if(this.superReflector != null && this.superReflector instanceof Reflector) {
			value = ((Reflector)this.superReflector).getValue(name, target);
		}		
		//if the value is still null, check interfaces
		if(value == null) {
			for(IReflector iface : this.reflectorInterfaces) {
				if(iface != null && iface instanceof Reflector) {
					value = ((Reflector)iface).getValue(name, target);
				}
				if(value != null) break;
			}
		}	
		
		return value;
	}	
	
	
	public void setSuperReflector(IReflector superReflector) {
		this.superReflector = superReflector;
	}
	
	public void addReflectorInterface(IReflector reflectorInterface) {
		this.reflectorInterfaces.add(reflectorInterface);
	}

	@Override
	public IReflector getParentReflector() {
		return this.superReflector;
	}

	@Override
	public Set<IReflector> getInterfaceReflectors() {
		return this.reflectorInterfaces;
	}	
	
	@Override
	public Class<?>[] getGroupSequence() {
		Class<?>[] copy = new Class<?>[this.groupSequence.length];
		int i = 0;
		for(Class<?> c : this.groupSequence) {
			copy[i++] = c;
		}
		return copy;
	}
	
	@Override
	public boolean hasGroupSequence() {
		return this.groupSequence.length > 0;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result	+ ((targetClass == null) ? 0 : targetClass.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Reflector other = (Reflector) obj;
		if (targetClass == null) {
			if (other.targetClass != null)
				return false;
		} else if (!targetClass.equals(other.targetClass))
			return false;
		return true;
	}	
	
}
