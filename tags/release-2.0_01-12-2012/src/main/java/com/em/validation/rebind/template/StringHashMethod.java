package com.em.validation.rebind.template;

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

import java.util.List;

import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModelException;

public class StringHashMethod implements TemplateMethodModel {

	@Override
	public Object exec(@SuppressWarnings("rawtypes") List args) throws TemplateModelException {

		if(args.size() != 1) {
			throw new TemplateModelException("Hash requires 1 argument");
		}
		
		//get string
		String toHash = (String)args.get(0);
		//get hash code as string
		return String.valueOf(toHash.hashCode());
	}

}
