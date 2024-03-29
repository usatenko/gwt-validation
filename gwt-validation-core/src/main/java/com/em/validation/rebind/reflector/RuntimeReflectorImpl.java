package com.em.validation.rebind.reflector;

/*
 GWT Validation Framework - A JSR-303 validation framework for GWT

 (c) 2008 gwt-validation contributors (http://code.google.com/p/gwt-validation/) 

 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.
*/

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.GroupSequence;
import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.metadata.ConstraintDescriptor;
import javax.validation.metadata.Scope;

import com.em.validation.client.reflector.IReflector;
import com.em.validation.client.reflector.Reflector;
import com.em.validation.rebind.metadata.PropertyMetadata;
import com.em.validation.rebind.resolve.PropertyResolver;

public class RuntimeReflectorImpl extends Reflector {

	Map<String,PropertyMetadata> metadataMap = new HashMap<String, PropertyMetadata>();
	
	private RuntimeReflectorImpl() {
		
	}
	
	public RuntimeReflectorImpl(Class<?> targetClass) {
		this();
		this.targetClass = targetClass;
		this.metadataMap = PropertyResolver.INSTANCE.getPropertyMetadata(targetClass);
		for(String prop : this.metadataMap.keySet()) {
			//don't include class level annotations in the property set
			if(targetClass.getName().equals(prop)) continue;
			//add properties
			this.properties.add(prop);
		}
		
		//set group sequence from metadata
		GroupSequence sequence = targetClass.getAnnotation(GroupSequence.class);
		if(sequence != null) {
			this.groupSequence = sequence.value();
			if(this.groupSequence == null) {
				this.groupSequence = new Class<?>[0];
			}
		}
	}
	
	@Override
	public Object getValue(String name, Object target) {
		//get property metadata
		PropertyMetadata metadata = this.metadataMap.get(name); 
		
		Object value = null;
		
		if(metadata != null && metadata.getAccessor() != null) {
			String accessor = metadata.getAccessor();
			if(metadata.isField()) {
					try {
						Field field = this.targetClass.getDeclaredField(accessor);
						field.setAccessible(true);
						value = field.get(target);
					} catch (SecurityException e) {
						throw new ValidationException("A security exception occurred during validation.  Please inspect your class definition and security configuration.",e);
					} catch (NoSuchFieldException e) {
						throw new ValidationException("The field \"" + accessor + "\" does not exist.  Please inspect your class definition.",e);
					} catch (IllegalArgumentException e) {
						throw new ValidationException("Illegal arguments were provided to a property during validation.",e);
					} catch (IllegalAccessException e) {
						throw new ValidationException("Illegal access exception caught during validation.  Only accessible properties can be validated.",e);
					} catch (Exception e) {
						throw new ValidationException("An exception was thrown during validation: " + e.getMessage(),e);
					}
			} else {
				try {
					accessor = accessor.substring(0, accessor.lastIndexOf('('));
					Method method = this.targetClass.getDeclaredMethod(accessor, new Class<?>[]{});
					method.setAccessible(true);
					value = method.invoke(target, new Object[]{});
				} catch (IllegalArgumentException e) {
					throw new ValidationException("Illegal arguments were provided to a method during validation.  Only no-argument properties are accepted.",e);
				} catch (IllegalAccessException e) {
					throw new ValidationException("Illegal access exception caught during validation.  Only accessible properties can be validated.",e);
				} catch (InvocationTargetException e) {
					throw new ValidationException("Invocation target exception caught during validation.  Only accessible classes can be validated.",e);
				} catch (SecurityException e) {
					throw new ValidationException("A security exception occurred during validation.  Please inspect your class definition and security configuration.",e);
				} catch (NoSuchMethodException e) {
					throw new ValidationException("The method \"" + accessor + "\" does not exist.  Please inspect your class definition.",e);
				} catch (Exception e) {
					throw new ValidationException("An exception was thrown during validation: " + e.getMessage(),e);
				}
			}
		}
		
		if(value == null) {
			value = this.getSuperValues(name, target);
		}
		
		return value;
	}
	
	public void setConstraintDescriptorMap(Map<String, Set<ConstraintDescriptor<?>>> constraintDescriptors) {
		this.constraintDescriptors = constraintDescriptors;
	}

	@Override
	public boolean isCascaded(String propertyName) {
		//default to non-cascaded
		boolean result = false;
		
		//get property metadata
		PropertyMetadata metadata = this.metadataMap.get(propertyName);
		String accessor = null;
		if(metadata != null) {
			accessor = metadata.getAccessor();
		}
	
		//check method names for property that is cascaded
		Method method = null;//part of fix for issue #69, starts as null
		
		try {
			if(method == null) {
				method = this.targetClass.getDeclaredMethod(propertyName, new Class<?>[]{});
			}
		} catch (Exception ex) {
			//result will still be false
		}
		
		try {
			if(method == null && accessor != null) {
				method = this.targetClass.getDeclaredMethod(accessor, new Class<?>[]{});
			}			
		} catch (Exception ex) {
			//result will still be false			
		}

		try {
			if(method == null) {
				method = this.targetClass.getDeclaredMethod("get" + propertyName.substring(0,1).toUpperCase() + propertyName.substring(1), new Class<?>[]{});
			}
		} catch (Exception ex) {
			//result will still be false			
		}

		try {
			if(method != null) {
				result = method.getAnnotation(Valid.class) != null;
			}
		} catch (Exception ex) {
			//result will still be false
		}
		
		//check fields for property that is cascaded (only if still false)
		if(result == false) {
			try {
				Field field = this.targetClass.getDeclaredField(propertyName);
				if(field != null) {
					result = field.getAnnotation(Valid.class) != null;
				}
			} catch (Exception ex) {
				//result will still be false
			}
		}
		
		//if still false after checking property and field, continue to check other values
		if(result == false) {
			if(this.superReflector != null) {
				result = this.superReflector.isCascaded(propertyName);
			}
			if(result == false) {
				for(IReflector iface : this.reflectorInterfaces) {
					result = iface.isCascaded(propertyName);
					if(result) break;
				}
			}			
		}
		
		return result;
	}

	@Override
	public Class<?> getPropertyType(String name) {
		Class<?> result = null;
		
		//check method names for property that is cascaded
		try {
			Method method = this.targetClass.getDeclaredMethod(name, new Class<?>[]{});
			if(method != null) {
				result = method.getReturnType();
			}
		} catch (Exception ex) {
			//result will still be false			
		}
		
		//check fields for property that is cascaded (only if still false)
		if(result == null) {
			try {
				Field field = this.targetClass.getDeclaredField(name);
				if(field != null) {
					result = field.getType();
				}
			} catch (Exception ex) {
				//result will still be false
			}
		}
		
		//if still false after checking property and field, continue to check other values
		if(result == null) {
			if(this.superReflector != null) {
				result = this.superReflector.getPropertyType(name);
			}
			if(result == null) {
				for(IReflector iface : this.reflectorInterfaces) {
					result = iface.getPropertyType(name);
					if(result != null) break;
				}
			}			
		}
		return result;
	}

	@Override
	public Set<ElementType> declaredOn(Scope scope, String property, ConstraintDescriptor<?> descriptor) {
		Set<ElementType> results = new LinkedHashSet<ElementType>();

		//return if the property string is null or the descriptor is null
		if(property == null || descriptor == null) return results;
		
		//get property descriptor from introspection
		BeanInfo targetInfo = null;
		try {
			targetInfo = Introspector.getBeanInfo(targetClass);
		} catch (IntrospectionException e) {
			//do nothing
		}
		
		//get annotation instance
		Annotation annotation = descriptor.getAnnotation();
		
		//find the property descriptor by name
		PropertyDescriptor prop = null;
		if(targetInfo != null) {
			for(PropertyDescriptor check : targetInfo.getPropertyDescriptors()) {
				if(property.equals(check.getName())) {
					prop = check;
					break;
				}
			}
		}
		
		//check the property and field for the declared annotation
		if(prop != null) {
			
			try {
				Field field = this.targetClass.getDeclaredField(property);
				List<Annotation> annotationList = PropertyResolver.INSTANCE.getContstraintAnnotations(Arrays.asList(field.getAnnotations()));
				for(Annotation checking : annotationList) {
					if(checking.toString().equals(annotation.toString())) {
						results.add(ElementType.FIELD);
					}
				}
			} catch (Exception ex) {
				
			}
			
			try {
				Method method = this.targetClass.getDeclaredMethod(prop.getReadMethod().getName(),new Class<?>[]{});
				List<Annotation> annotationList = PropertyResolver.INSTANCE.getContstraintAnnotations(Arrays.asList(method.getAnnotations()));
				for(Annotation checking : annotationList) {
					if(checking.toString().equals(annotation.toString())) {
						results.add(ElementType.METHOD);
					}
				}
			} catch (Exception ex) {
				
			}
		}
		
		if(Scope.HIERARCHY.equals(scope)) {
			if(this.superReflector != null) {
				results.addAll(this.superReflector.declaredOn(scope, property, descriptor));
			}
			for(IReflector iface : this.reflectorInterfaces) {
				if(iface != null){
					results.addAll(iface.declaredOn(scope, property, descriptor));
				}
			}
		}
		
		return results;
	}	
}
