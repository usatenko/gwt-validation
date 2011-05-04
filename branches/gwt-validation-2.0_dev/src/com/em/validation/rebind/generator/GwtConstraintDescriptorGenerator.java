package com.em.validation.rebind.generator;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.TypeOracle;

public class GwtConstraintDescriptorGenerator extends Generator {

	@Override
	public String generate(TreeLogger logger, GeneratorContext context,	String typeName) throws UnableToCompleteException {
		
		//get the type oracle
		TypeOracle typeOracle = context.getTypeOracle();
		
		//the type oracle cannot be null
		assert(typeName != null);
		
		System.out.println(typeName);
		
		
		return "";
	}

}
