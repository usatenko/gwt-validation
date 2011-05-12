package com.em.validation.rebind.resolve;

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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.validation.Constraint;
import javax.validation.OverridesAttribute;
import javax.validation.ReportAsSingleViolation;
import javax.validation.metadata.ConstraintDescriptor;

import com.em.validation.rebind.metadata.ConstraintMetadata;
import com.em.validation.rebind.metadata.ConstraintPropertyMetadata;
import com.em.validation.rebind.metadata.OverridesMetadata;
import com.em.validation.rebind.metadata.PropertyMetadata;
import com.em.validation.rebind.reflector.factory.RuntimeConstraintDescriptorFactory;


/**
 * Get all of the constraints (via constraint descriptors) of a given class.
 * 
 * @author chris
 *
 */
public enum ConstraintDescriptionResolver {

	INSTANCE;
	
	private Map<String, ConstraintMetadata> metadataCache = new HashMap<String, ConstraintMetadata>();
	
	private ConstraintDescriptionResolver() {
		
	}
	
	public Map<String,Set<ConstraintDescriptor<?>>> getConstraintDescriptors(Class<?> targetClass) {
		Map<String,Set<ConstraintDescriptor<?>>> results = new HashMap<String, Set<ConstraintDescriptor<?>>>();
		Map<String,PropertyMetadata> propertyMetadata = PropertyResolver.INSTANCE.getPropertyMetadata(targetClass);
		for(String propertyName : propertyMetadata.keySet()) {
			results.put(propertyName, this.getConstraintsForProperty(targetClass, propertyName));
		}		
		return results;
	}
	
	public Set<ConstraintMetadata> getAllMetadata(Class<?> targetClass) {
		Set<ConstraintMetadata> metadataResult = new LinkedHashSet<ConstraintMetadata>();
		Map<String,PropertyMetadata> propertyMetadata = PropertyResolver.INSTANCE.getPropertyMetadata(targetClass);
		for(String propertyName : propertyMetadata.keySet()) {
			PropertyMetadata property = propertyMetadata.get(propertyName);
			for(Annotation annotation : property.getAnnotationInstances()) {
				metadataResult.add(this.getConstraintMetadata(annotation));
			}
		}		
		return metadataResult;
	}
	
	public Set<ConstraintDescriptor<?>> getConstraintsForProperty(Class<?> targetClass, String propertyName) {
		Set<ConstraintDescriptor<?>> descriptors = new LinkedHashSet<ConstraintDescriptor<?>>();
		PropertyMetadata property = PropertyResolver.INSTANCE.getPropertyMetadata(targetClass, propertyName);
		for(Annotation annotation : property.getAnnotationInstances()) {
			ConstraintMetadata metadata = this.getConstraintMetadata(annotation);
			ConstraintDescriptor<?> descriptor = RuntimeConstraintDescriptorFactory.INSTANCE.getConstraintDescriptor(metadata);
			descriptors.add(descriptor);
		}		
		return descriptors;
	}
	
	public ConstraintMetadata getConstraintMetadata(Annotation annotation) {
		
		//create annotation metadata
		ConstraintMetadata metadata = this.metadataCache.get(annotation.toString()); 
				
		//if the cache misses, generate
		if(metadata == null) {
			metadata = new ConstraintMetadata();
		
			//create empty annotation metadata
			//annotation names
			metadata.setName(annotation.annotationType().getName());
			metadata.setSimpleName(annotation.annotationType().getSimpleName());
			metadata.setInstance(annotation);
			
			//create annotation method metadata
			for(Method method : annotation.annotationType().getDeclaredMethods()) {
				ConstraintPropertyMetadata aMeta = new ConstraintPropertyMetadata();
				String returnValue = this.createReturnValueAsString(method, annotation);
				
				//get return type
				String returnType = method.getReturnType().getSimpleName();
				if(method.getReturnType().getComponentType() != null) {
					returnType = method.getReturnType().getComponentType().getSimpleName() + "[]";
				}
				aMeta.setReturnType(returnType);
				
				//set the method name and return value
				aMeta.setMethodName(method.getName());
				aMeta.setReturnValue(returnValue);		
				
				//save metadata for use by generator
				metadata.getMethodMap().put(aMeta.getMethodName(), aMeta);
			}
			
			//constraint 
			Constraint constraint = annotation.annotationType().getAnnotation(Constraint.class);
			metadata.getValidatedBy().addAll(Arrays.asList(constraint.validatedBy()));
			
			//report as single or not
			metadata.setReportAsSingleViolation(annotation.annotationType().getAnnotation(ReportAsSingleViolation.class) != null);  
			
			//scope
			
			//target element types
			
			//put in cache
			this.metadataCache.put(annotation.toString(), metadata);
			
			//get composing constraints
			for(Annotation subAnnotation : annotation.annotationType().getAnnotations()) {
				if(subAnnotation.annotationType().getAnnotation(Constraint.class) != null){
					metadata.getComposedOf().add(this.getConstraintMetadata(subAnnotation));
				}
			}
			
			//get overrides
			if(metadata.getComposedOf() != null && metadata.getComposedOf().size() > 0) {
				//create overrides metadata for method/property
				OverridesMetadata overrides = new OverridesMetadata();
				//create annotation method metadata
				for(Method method : annotation.annotationType().getDeclaredMethods()) {
					OverridesAttribute override = method.getAnnotation(OverridesAttribute.class);
					if(override != null) {
						Object value = null;
						try {
							value = method.invoke(annotation, new Object[]{});
						} catch (Exception e) {
							//could not invoke method, value is null
						}						
						overrides.addOverride(override.constraint(), method.getName(), value, this.createReturnValueAsString(value), override.constraintIndex());
					}
					OverridesAttribute.List overrideList = method.getAnnotation(OverridesAttribute.List.class);
					if(overrideList != null && overrideList.value() != null && overrideList.value().length > 0) {
						for(OverridesAttribute listedOverride : overrideList.value()) {
							if(listedOverride != null) {
								Object value = null;
								try {
									value = method.invoke(annotation, new Object[]{});
								} catch (Exception e) {
									//could not invoke method, value is null
								}						
								overrides.addOverride(listedOverride.constraint(), method.getName(), value, this.createReturnValueAsString(value), listedOverride.constraintIndex());
							}							
						}
					}
				}
			}
		}
		
		return metadata;
	}
		
	/**
	 * Takes an annotation and a method and turns it into the string representation of what
	 * would need to be typed in to a class body to return that value;
	 * 
	 * @param method
	 * @param annotation
	 * @return
	 */
	private String createReturnValueAsString(Method method, Annotation annotation) {
		//invoke method
		Object value = null;

		try {
			value = method.invoke(annotation, new Object[]{});
		} catch (Exception e) {
		} 
		
		//use the return value function to resolve the string.
		return this.createReturnValueAsString(value);
	}
	
	/**
	 * Takes a human readable / string translatable value of the type that would be
	 * found on an annotation and turns it into a string that can be used in a code
	 * generation template.
	 * 
	 * @param method
	 * @param annotation
	 * @return
	 */
	private String createReturnValueAsString(Object value) {
		//return a null value, as a string, so that it is printed as plain null
		//this will preserve whatever the class is doing so that no "inexplicable"
		//changes are made.
		if(value == null) return "null";

		Class<?> returnType = value.getClass();
		Class<?> containedClass = returnType.getComponentType();

		
		StringBuilder output = new StringBuilder();
		if(containedClass == null) {
			if(String.class.equals(returnType)) {
				output.append("\"");
			} 
			if(value instanceof Class<?>) {
				Class<?> clazz = (Class<?>)value;
				output.append(clazz.getName() + ".class");
			} else {				
				output.append(value);
			}
			if(String.class.equals(returnType)) {
				output.append("\"");
			}
		} else {
			//get the array since the return type is of a container 
			Object[] values = (Object[])value;
			
			output.append("new ");
			output.append(containedClass.getSimpleName());
			output.append("[]{");
			int i = 0;
			for(Object v : values) {
				if(i > 0) {
					output.append(",");
				}
				i++;
				if(String.class.equals(containedClass)) {
					output.append("\"");
				} 
				if(v instanceof Class<?>) {
					Class<?> clazz = (Class<?>)v;
					output.append(clazz.getName() + ".class");
				} else {				
					output.append(v);
				}
				if(String.class.equals(containedClass)) {
					output.append("\"");
				}
			}
			output.append("}");
		}		
		
		return output.toString();
	}
	
}
