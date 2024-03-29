package com.em.validation.client.messages;

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

import java.util.Map;

/**
 * Used to fulfill the getMessage method of IMessageResolver in the client and runtime code without copy pasting
 * 
 * @author chris
 *
 */
public class MessageUtil {

	/**
	 * Do not allow utility class to be constructed
	 */
	private MessageUtil() {
		
	}
	
	public static String getMessage(String messageTemplate, Map<String, Object> properties) {
		String output = messageTemplate;
		
		for(String key : properties.keySet()) {
			Object value = properties.get(key);
			if(value != null) {
				String replace = "\\{" + key + "\\}";
				output = output.replaceAll(replace, value.toString());
			}
		}	
		
		//replace literals with actuals, and remark at how crazy the java regex stuff can be
		//we have to escape the escape to get the final literal "\{"
		output = output.replaceAll("\\\\\\{", "{");
		output = output.replaceAll("\\\\\\}", "}");
		
		return output;
	}
	
}
