package com.em.validation.rebind.generator.source;

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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.em.validation.rebind.metadata.ClassDescriptor;
import com.em.validation.rebind.metadata.ConstraintMetadata;
import com.em.validation.rebind.metadata.ConstraintPropertyMetadata;
import com.em.validation.rebind.resolve.ConstraintDescriptionResolver;
import com.em.validation.rebind.scan.ClassScanner;
import com.em.validation.rebind.template.TemplateController;

public enum AnnotationInstanceFactoryGenerator {

	INSTANCE;
	
	private final String BASE_PACKAGE = "com.em.validation.client";
	private final String TARGET_PACKAGE = this.BASE_PACKAGE + ".generated.factory";
	private final String PREFIX = "AnnotationInstanceFactory";
	
	private AnnotationInstanceFactoryGenerator() {
		
	}
	
	public ClassDescriptor getAnnotationFactoryDescriptor() {
		
		//create empty set
		ClassDescriptor outerFactoryDescriptor = new ClassDescriptor();
		
		//get all constrained classes
		Set<Class<?>> constrainedClasses = ClassScanner.INSTANCE.getConstrainedClasses();
		
		//a place to store all of the constraint metadata
		Set<ConstraintMetadata> constraintMetadataSet = new LinkedHashSet<ConstraintMetadata>();
		
		//a place to store all of the constraint metadata
		Map<Class<?>,Set<ConstraintMetadata>> annotationClassToMetadataMap = new LinkedHashMap<Class<?>, Set<ConstraintMetadata>>();	
		Map<Class<?>,Set<ConstraintPropertyMetadata>> annotationMethodMetadata = new LinkedHashMap<Class<?>, Set<ConstraintPropertyMetadata>>();
		
		//and, finally, a map of the constraint class names to the annotation instance names
		Map<String,String> factoryMap = new LinkedHashMap<String, String>();
		
		//get all of the metadata
		for(Class<?> targetClass : constrainedClasses) {
			constraintMetadataSet.addAll(ConstraintDescriptionResolver.INSTANCE.getAllMetadata(targetClass));
		}

		//recursively descend into every constraint and the composed constraints that they are made up of
		this.recursiveResolveAllMetadata(constraintMetadataSet, annotationClassToMetadataMap, annotationMethodMetadata);
		
		//at this point we have a map of all of the values that we really need to start generating annotation instances
		for(Class<?> annotationClass : annotationClassToMetadataMap.keySet()) {
			Set<ConstraintMetadata> constraints = annotationClassToMetadataMap.get(annotationClass);
			Set<ConstraintPropertyMetadata> methods = annotationMethodMetadata.get(annotationClass);

			//also a set of imports, allows other things to be imported into an annotation
			Set<String> imports = new HashSet<String>();
			
			//set up imports (see imports declaration)
			for(ConstraintPropertyMetadata method : methods) {
				if(method.getImportType() == null) continue;
				imports.add(method.getImportType());
			}
			
			//create generated name
			//uuid
			UUID uuid = UUID.randomUUID();
			String uuidString = uuid.toString();
			uuidString = uuidString.replaceAll("\\-","");
			
			//create generation target annotation name
			String generatedFactoryName = this.PREFIX + "_" + uuidString;
			String fullGeneratedFactoryName = this.TARGET_PACKAGE + ".instances." +generatedFactoryName;
			
			//target annotation stuff
			String annotationImportName = annotationClass.getName();
			String targetAnnotation = annotationClass.getSimpleName();
			
			//build the template object map
			Map<String,Object> templateMap = new HashMap<String, Object>();
			templateMap.put("constraints",constraints);
			templateMap.put("methods", methods);
			templateMap.put("imports", imports);
			templateMap.put("generatedName",generatedFactoryName);
			templateMap.put("targetPackage", this.TARGET_PACKAGE + ".instances");
			templateMap.put("annotationImportName",annotationImportName);
			templateMap.put("targetAnnotation",targetAnnotation);
			
			//use the template and create and save a new descriptor
			ClassDescriptor descriptor = new ClassDescriptor();
			descriptor.setPackageName(this.TARGET_PACKAGE + ".instances");
			descriptor.setFullClassName(fullGeneratedFactoryName);
			descriptor.setClassName(generatedFactoryName);
			descriptor.setClassContents(TemplateController.INSTANCE.processTemplate("templates/annotation/ConcreteAnnotationInstanceFactory.ftl", templateMap));
			
			//add to the factory map to generate the factory factory
			factoryMap.put(annotationImportName, descriptor.getFullClassName());
			
			//add the class descriptor to be the dependency of the outer descriptor
			outerFactoryDescriptor.getDependencies().add(descriptor);
		}
		
		//set data on the class descriptor
		outerFactoryDescriptor.setClassName("AnnotationInstanceFactory");
		outerFactoryDescriptor.setFullClassName(this.TARGET_PACKAGE + "." + outerFactoryDescriptor.getClassName());
		outerFactoryDescriptor.setPackageName(this.TARGET_PACKAGE);
		
		//create the class from the template
		Map<String,Object> factoryTemplateMap = new HashMap<String, Object>();
		factoryTemplateMap.put("factoryMap", factoryMap);
		factoryTemplateMap.put("targetPackage",this.TARGET_PACKAGE);

		//set contents of class descriptor from the template
		outerFactoryDescriptor.setClassContents(TemplateController.INSTANCE.processTemplate("templates/annotation/AnnotationInstanceFactory.ftl", factoryTemplateMap));
		
		//return set
		return outerFactoryDescriptor;
	}

	private Set<ConstraintMetadata> recursiveBlockSet = new HashSet<ConstraintMetadata>();
	
	private void recursiveResolveAllMetadata(Set<ConstraintMetadata> sourceSet, Map<Class<?>,Set<ConstraintMetadata>> annotationClassToMetadataMap, Map<Class<?>,Set<ConstraintPropertyMetadata>> annotationMethodMetadata) {
		//break down the metadata into signatures and constraints
		for(ConstraintMetadata metadata : sourceSet) {
			//continue if the metadata has already been processed
			if(this.recursiveBlockSet.contains(metadata)) {
				continue;
			}
			
			//set of constraint metadata
			Set<ConstraintMetadata> localSet = annotationClassToMetadataMap.get(metadata.getInstance().annotationType());
			//place new instance in map, if null
			if(localSet == null) {
				localSet = new LinkedHashSet<ConstraintMetadata>();
				annotationClassToMetadataMap.put(metadata.getInstance().annotationType(), localSet);
			}
			//populate local set with the metadata instance
			localSet.add(metadata);
			
			//set of method names
			Set<ConstraintPropertyMetadata> methodNames = annotationMethodMetadata.get(metadata.getInstance().annotationType());
			//place new instance in map, if null
			if(methodNames == null) {
				methodNames = new LinkedHashSet<ConstraintPropertyMetadata>();
				annotationMethodMetadata.put(metadata.getInstance().annotationType(),methodNames);
			}
			methodNames.addAll(metadata.getMethodMap().values());
			
			//add metadata as already processed to the recursive block
			this.recursiveBlockSet.add(metadata);
			
			//do the same for children
			if(metadata.getComposedOf() != null && metadata.getComposedOf().size() > 0) {
				this.recursiveResolveAllMetadata(metadata.getComposedOf(), annotationClassToMetadataMap, annotationMethodMetadata);
			}
		}
	}
	
	/**
	 * Method for clearing all metadata for a clean run
	 * 
	 */
	public void clear() {
		this.recursiveBlockSet.clear();
	}
	
}
