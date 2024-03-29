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

import java.lang.annotation.ElementType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.validation.metadata.ConstraintDescriptor;
import javax.validation.metadata.Scope;

import com.em.validation.client.reflector.IReflector;
import com.em.validation.client.reflector.ReflectorFactory;
import com.em.validation.rebind.metadata.ClassDescriptor;
import com.em.validation.rebind.metadata.PropertyMetadata;
import com.em.validation.rebind.resolve.PropertyResolver;
import com.em.validation.rebind.template.TemplateController;

public enum ReflectorGenerator {

	INSTANCE;
	
	private final String BASE_PACKAGE = "com.em.validation.client";
	private final String TARGET_PACKAGE = this.BASE_PACKAGE + ".generated.reflector";
	
	public ClassDescriptor getReflectorDescirptions(Class<?> targetClass) {
		//get the runtime reflector (usful for some metadata)
		IReflector runtimeReflector = ReflectorFactory.INSTANCE.getReflector(targetClass);
		
		//target of generation
		String targetPackage = this.TARGET_PACKAGE;

		UUID uuid = UUID.randomUUID();
		String uuidString = uuid.toString();
		uuidString = uuidString.replaceAll("\\-","");
		
		//get the basic name of the target class and generate the reflector class name, add uuid to avoid colisions with 
		//classes from different trees (and groups) that may have otherwise stomped on each other
		String concreteClassName = targetClass.getSimpleName() + "Reflector_" + uuidString;
		
		//the source listings
		ClassDescriptor reflectorDescriptor = new ClassDescriptor();
	
		//set up imports
		List<String> imports = new ArrayList<String>();
		String targetClassString = targetClass.getName();
		if(targetClassString != null && !targetClassString.isEmpty()) {
			targetClassString = targetClassString.replaceAll("\\$", ".");
		}		
		imports.add(targetClassString);

		//group sequence
		Class<?>[] groupSequenceArray = runtimeReflector.getGroupSequence();
		List<String> groupSequences = new ArrayList<String>();
		for(Class<?> group : groupSequenceArray) {
			groupSequences.add(group.getName() + ".class");
		}
		
		//the list of properties
		Map<String,PropertyMetadata> metadataMap = PropertyResolver.INSTANCE.getPropertyMetadata(targetClass);
		
		//list of cascaded properties
		Set<String> cascadedProperties = new LinkedHashSet<String>();
		
		//where properties are declared (for finder)
		Set<String> declaredOnMethod = new LinkedHashSet<String>();
		Set<String> declaredOnField = new LinkedHashSet<String>(); 

		//generate class descriptors for each metadata on each annotation
		for(PropertyMetadata propertyMetadata : metadataMap.values()) {

			for(ConstraintDescriptor<?> constraint : runtimeReflector.getConstraintDescriptors(propertyMetadata.getName(), Scope.LOCAL_ELEMENT)) {
				//generate the class descriptor (the actual ascii data that makes up the class) from the annotation and property metadata
				ClassDescriptor descriptor = ConstraintDescriptionGenerator.INSTANCE.generateConstraintDescriptor(constraint.getAnnotation(),propertyMetadata.getReturnType());

				//set up the dependencies in the property metadata and in the class descriptor
				propertyMetadata.getConstraintDescriptorClasses().add(descriptor.getClassName());
				reflectorDescriptor.getDependencies().add(descriptor);
				
				//get declared on
				Set<ElementType> declaredOn = runtimeReflector.declaredOn(Scope.LOCAL_ELEMENT, propertyMetadata.getName(), constraint);
				
				//if the return list contains "field" then it has been declared on a field and we pass this info to the template
				if(declaredOn.contains(ElementType.FIELD)) {
					declaredOnField.add(descriptor.getClassName());
				} 
				
				//if the return list contains "method" then it has been declared on a method and we pass this info to the template
				if(declaredOn.contains(ElementType.METHOD)) {
					declaredOnMethod.add(descriptor.getClassName());					
				} 
			}
			
			//save cascaded property
			if(runtimeReflector.isCascaded(propertyMetadata.getName())) {
				cascadedProperties.add(propertyMetadata.getName());
			}
		}
		
		Set<String> classLevelConstraints = new HashSet<String>();
		for(ConstraintDescriptor<?> classLevel : runtimeReflector.getClassConstraintDescriptors(Scope.LOCAL_ELEMENT)) {
			//generate the class descriptor (the actual ascii data that makes up the class) from the annotation and property metadata
			ClassDescriptor descriptor = ConstraintDescriptionGenerator.INSTANCE.generateConstraintDescriptor(classLevel.getAnnotation(),targetClass);
			reflectorDescriptor.getDependencies().add(descriptor);
			classLevelConstraints.add(descriptor.getClassName());			
		}
		
		//create data model
		Map<String,Object> templateDataModel = new HashMap<String, Object>();
		templateDataModel.put("concreteClassName", concreteClassName);
		templateDataModel.put("importList", imports);
		templateDataModel.put("properties", metadataMap.values());
		templateDataModel.put("reflectionTargetName", targetClass.getSimpleName());
		templateDataModel.put("targetPackage", targetPackage);
		templateDataModel.put("generatedConstraintPackage",ConstraintDescriptionGenerator.INSTANCE.getTargetPackage());
		templateDataModel.put("cascades",cascadedProperties);
		templateDataModel.put("declaredOnMethod", declaredOnMethod);
		templateDataModel.put("declaredOnField", declaredOnField);
		templateDataModel.put("groupSequence",groupSequences);
		templateDataModel.put("classLevelConstraints",classLevelConstraints);

		//create class descriptor from generated code
		reflectorDescriptor.setClassContents(TemplateController.INSTANCE.processTemplate("templates/reflector/ReflectorImpl.ftl", templateDataModel));
		reflectorDescriptor.setClassName(concreteClassName);
		reflectorDescriptor.setFullClassName(targetPackage + "." + concreteClassName);
		reflectorDescriptor.setPackageName(this.TARGET_PACKAGE);
		
		return reflectorDescriptor;
	}
}
