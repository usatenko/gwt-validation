package com.em.validation.client.regex;

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

import java.util.regex.Pattern;

import javax.validation.constraints.Pattern.Flag;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

public enum RegexProvider {

	INSTANCE;
	
	private RegexProvider(){
		
	}
	
	public boolean matches(String regex, String check, Flag[] flags) {
		RegExp exp = null;
		if(flags != null && flags.length > 0) {
			String flagString = "";
			for(Flag f : flags) {
				if(Flag.CASE_INSENSITIVE.equals(f)) {
					flagString += "i";
				} else if(Flag.MULTILINE.equals(f)) {
					flagString += "m";
				}
			}		
			exp = RegExp.compile(regex);
		} else {
			exp = RegExp.compile(regex);
		}
		
		
		return exp.test(check);
	}
	
}
