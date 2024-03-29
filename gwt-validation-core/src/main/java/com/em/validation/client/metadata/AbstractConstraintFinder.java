package com.em.validation.client.metadata;

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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.validation.metadata.ConstraintDescriptor;
import javax.validation.metadata.ElementDescriptor.ConstraintFinder;
import javax.validation.metadata.Scope;

import com.em.validation.client.reflector.IReflector;
import com.em.validation.client.reflector.ReflectorFactory;

/**
 * Base class for implementing different types of constraint finders.
 * 
 * @author chris
 *
 */
public abstract class AbstractConstraintFinder implements ConstraintFinder {

	
	/**
	 * Set for caching the results of a search.  By default, the set is empty.
	 */
	protected Set<ConstraintDescriptor<?>> cachedResults = new HashSet<ConstraintDescriptor<?>>();
	
	/**
	 * Used for scope searches
	 * 
	 */
	protected IReflector backingReflector = null;

	/**
	 * When the state of the search values has changed, mark the cache as dirty so that the search will be re-run. 
	 */
	private boolean searchChanged = true; 

	/**
	 * The scope of the search
	 * 
	 * @see Scope
	 */
	private Scope scope = Scope.HIERARCHY;
	
	/**
	 * List of types that the annotation can be declared on
	 */
	private Set<ElementType> declaredOnTypes = new HashSet<ElementType>();
	
	/**
	 * List of groups that are being queried on
	 */
	private Set<Class<?>> matchingGroups = new HashSet<Class<?>>();
	
	public abstract Set<ConstraintDescriptor<?>> findConstraints(Scope scope, Set<ElementType> declaredOnTypes, Set<Class<?>> matchingGroups);
	
	@Override
	public Set<ConstraintDescriptor<?>> getConstraintDescriptors() {
		//if the state of the class has changed, re-search the constraints
		if(this.searchChanged) {
			this.cachedResults = this.findConstraints(this.scope,this.declaredOnTypes,this.matchingGroups);
			this.searchChanged = false;
		}		
		return this.cachedResults;
	}

	@Override
	public boolean hasConstraints() {
		return !this.getConstraintDescriptors().isEmpty();
	}

	@Override
	public ConstraintFinder lookingAt(Scope scope) {
		this.scope = scope;
		//mark the cache as dirty
		this.searchChanged = true;
		return this;
	}
	@Override
	public ConstraintFinder unorderedAndMatchingGroups(Class<?>... groups) {
		this.matchingGroups.clear();
		this.matchingGroups.addAll(Arrays.asList(groups));
		//mark the cache as dirty
		this.searchChanged = true;
		return this;
	}
	
	@Override
	public ConstraintFinder declaredOn(ElementType... types) {
		this.declaredOnTypes.addAll(Arrays.asList(types));
		//mark the cache as dirty
		this.searchChanged = true;
		return this;
	}
	
	protected Boolean foundIn(Set<Class<?>> groupSet, Class<?> mGroup) {
		boolean result = false;
		
		if(groupSet.contains(mGroup)) {
			result = true;
		} else {
			//create temp set
			Set<Class<?>> tempSet = new HashSet<Class<?>>(groupSet);
			
			//create deeper set and check again
			for(Class<?> group : groupSet) {
				IReflector gReflector = ReflectorFactory.INSTANCE.getReflector(group);
				
				if(gReflector != null) {
					if(gReflector.getParentReflector() != null) {
						tempSet.add(gReflector.getParentReflector().getTargetClass());
					}
					
					if(gReflector.getInterfaceReflectors() != null) {
						for(IReflector ireflector : gReflector.getInterfaceReflectors()) {
							if(ireflector != null) {
								tempSet.add(ireflector.getTargetClass());
							}
						}
					}
				}
			}
			
			if(groupSet.size() != tempSet.size()) {
				//call with temp set				
				result = this.foundIn(tempSet, mGroup);
			}
			//else the value is unchanged, drop from loop, and return
		}		
		
		return result;
	}	
}
