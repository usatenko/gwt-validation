package com.google.gwt.validation.rebind;

import java.io.PrintWriter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.NotFoundException;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;

/**
 * Class that creates the validator for the given input class
 *
 * @author chris
 *
 */
public class ValidatorCreator {

	private TreeLogger logger;
	private GeneratorContext context;
	private String typeName;
	private TypeOracle typeOracle;

	private ValidatorCreator() {

	}

	/**
	 * Creates the generator from the parameters passed by the GWT compiler
	 *
	 * @param logger
	 * @param context
	 * @param typeName
	 */
	public ValidatorCreator(TreeLogger logger, GeneratorContext context, String typeName) {

		//super
		this();

		//set others
		this.logger = logger;
		this.context = context;
		this.typeName = typeName;

		//get type oracle from context
		this.typeOracle = this.context.getTypeOracle();

	}


	/**
	 * Uses a sourcecode writer to write the implementation of the
	 * class's validator that was indicated at construction.  Returns
	 * the fully qualified class name of that implementation.
	 *
	 * @return
	 */
	public String createValidatorImplementation() {

		//string source
		String outputClassName = null;

		try {
			//get class type
			JClassType classType = this.typeOracle.getType(this.typeName);

			//write to string
			SourceWriter sw = this.getSourceWriter(classType);

			//if sourcewriter is null, prematurely return the classname
			if(sw == null) {
				return classType.getParameterizedQualifiedSourceName() + "Validator";
			}

			//fully qualified
			String fullClass = classType.getQualifiedSourceName();

			try {

				//get the real class for the given class name
				Class<?> inputClass = Class.forName(fullClass);

				//write the group mapping
				//protected HashMap<String, ArrayList<String>> getGroupSequenceMapping(Class<?> inputClass) {
				sw.println("protected HashMap<String, ArrayList<String>> getGroupSequenceMapping(Class<?> inputClass) {");
				sw.indent();
				sw.println("if(inputClass == null || Object.class.equals(inputClass)) return new HashMap<String, ArrayList<String>>();");
				sw.println("HashMap<String, ArrayList<String>> orderingMap = new HashMap<String, ArrayList<String>>();");
				
				//get list
				HashMap<String, ArrayList<String>> groupSequenceMap = ValidationMetadataFactory.getGroupSequenceMap(inputClass);
				
				//for each thing thing create an ArrayList<String> of the order
				for(String group : groupSequenceMap.keySet()) {
					//output (using scope trick)
					sw.println("if(true) {");
					sw.indent();

					//print line
					sw.println("ArrayList<String> groups = new ArrayList<String>();");

					//get group list
					ArrayList<String> groupOrder = groupSequenceMap.get(group);

					//for each group add to the thing
					for(String g : groupOrder) {
						//add to a group
						sw.println("groups.add(\"" + g + "\");");
					}
					
					//add groups to ordering map
					sw.println("orderingMap.put(\"" + group + "\",groups);");
					
					//close scope
					sw.outdent();
					sw.println("}");
				}

				//return
				sw.println("return orderingMap;");
				
				//close the HashMap
				sw.outdent();
				sw.println("}");
				
				//write "Set<InvalidConstraint> performValidation(T object, String propertyName, String... groups);" method
				sw.println("public Set<InvalidConstraint<" + fullClass + ">> performValidation(" + fullClass + " object, String propertyName, ArrayList<String> groups, HashSet<String> processedGroups, HashSet<String> processedObjects) {" );
				sw.indent();
				//create empty hashset of invalid constraints
				sw.println("HashSet<InvalidConstraint<" + fullClass + ">> iCSet = new HashSet<InvalidConstraint<" + fullClass + ">>();");

				//condition propertyName to be null if empty (simplify if test)
				sw.println("if(propertyName != null && propertyName.trim().isEmpty()) {");
				sw.indent();
				sw.println("propertyName = null;");
				sw.outdent();
				sw.println("}");				
				
				//groups listing
				sw.println("String grouplisting = \"\";");
				sw.println("for(String group : groups) {");
				sw.indent();
				sw.println("grouplisting += \":\" + group;");
				sw.outdent();
				sw.println("}");
				
				//get the validation package list for that class
				ArrayList<ValidationPackage> validationPackageList = ValidationMetadataFactory.getValidatorsForClass(inputClass);

				//do code output for each method
				for(ValidationPackage vPack : validationPackageList) {

					//get implementing class name
					String impl = vPack.getImplementingConstraint().getClass().getName();

					//create if string for the if block (includes propertyName and groups)
					String ifTest = "(propertyName == null || propertyName.equals(\"" + vPack.getItemName() + "\")) && (groups.size() == 0";

					//list all groups
					for(String group : vPack.getGroups()) {
						ifTest += " || groups.contains(\"" + group + "\")";
					}
					//close if test
					ifTest += ")";
					
					//processed groups
					ifTest += " && (processedGroups.size() == 0 || (";
					
					//do the same for processed groups
					for(String group : vPack.getGroups()) {
						ifTest += "!processedGroups.contains(\"" + group + "\") && ";
					}
					//close if test
					ifTest += "))";
					//replace ' && ))' with '))' because I can't think of a better
					//way to not have an && at the end of the series.
					ifTest = ifTest.replace(" && ))", "))");
					
					//create if block (used to break up scope and to test for propertyName / groups)
					sw.println("if(" + ifTest + ") {");
					sw.indent();

					//create property map output
					this.writeMapToCode(vPack.getValidationPropertyMap(), sw);

					//create validator instace from implementing validator
					sw.println(impl + " validator = new " + impl + "();");

					//initialize validator with property map
					sw.println("validator.initialize(propertyMap);");

					//do object method in if test
					sw.println("if(!validator.isValid(object." + vPack.getMethod().getName() + "())) {");
					sw.indent();

					//create invalid constraint
					sw.println("InvalidConstraint<" + fullClass + "> ivc = new InvalidConstraint<" + fullClass + ">(\"" + vPack.getItemName() + "\",\"" + vPack.getMessage() + "\");");

					//add object
					sw.println("ivc.setInvalidObject(object);");
					
					//add value
					sw.println("ivc.setValue(object." + vPack.getMethod().getName() + "());");
					
					//add to iCSet
					sw.println("iCSet.add(ivc);");

					//end if test for validator
					sw.outdent();
					sw.println("}");

					//end if block
					sw.outdent();
					sw.println("}");

				}

				//get list of packages for class level
				validationPackageList = ValidationMetadataFactory.getClassLevelValidatorsForClass(inputClass);

				//do code output for each class level validator
				for(ValidationPackage vPack : validationPackageList) {

					//get implementing class name
					String impl = vPack.getImplementingConstraint().getClass().getName();

					//create if string for the if block (includes propertyName and groups)
					String ifTest = "(propertyName == null || propertyName.equals(\"" + vPack.getItemName() + "\")) && (groups.size() == 0";

					for(String group : vPack.getGroups()) {
						ifTest += " || groups.contains(\"" + group + "\")";
					}
					//close if test
					ifTest += ")";
					
					//processed groups
					ifTest += " && (processedGroups.size() == 0 || (";
					
					//do the same for processed groups
					for(String group : vPack.getGroups()) {
						ifTest += "!processedGroups.contains(\"" + group + "\") && ";
					}
					//close if test
					ifTest += "))";
					//replace ' && ))' with '))' because I can't think of a better
					//way to not have an && at the end of the series.
					ifTest = ifTest.replace(" && ))", "))");

					//create if block (used to break up scope and to test for propertyName / groups)
					sw.println("if(" + ifTest + ") {");
					sw.indent();

					//create property map output
					this.writeMapToCode(vPack.getValidationPropertyMap(), sw);

					//create validator instace from implementing validator
					sw.println(impl + " validator = new " + impl + "();");

					//initialize validator with property map
					sw.println("validator.initialize(propertyMap);");

					//do object method in if test
					sw.println("if(!validator.isValid(object)) {");
					sw.indent();
					
					//create invalid constraint
					sw.println("InvalidConstraint<" + fullClass + "> ivc = new InvalidConstraint<" + fullClass + ">(\"" + vPack.getItemName() + "\",\"" + vPack.getMessage() + "\");");

					//add object
					sw.println("ivc.setInvalidObject(object);");
					
					//add value
					sw.println("ivc.setValue(object);");
					
					//add to iCSet
					sw.println("iCSet.add(ivc);");

					//end if test for validator
					sw.outdent();
					sw.println("}");

					//end if block
					sw.outdent();
					sw.println("}");
				}


				//get @Valid package list
				ArrayList<ValidationPackage> vpValidList = ValidationMetadataFactory.getValidAnnotedPackages(inputClass);

				//process annotations
				for(ValidationPackage vPack : vpValidList) {

					try {
						//get return thingy
						Class<?> returnType = vPack.getMethod().getReturnType();

						if(returnType != null) {

							//build if test and check that property name and object.method() are okay
							String ifTest = "(propertyName == null || propertyName.equals(\"" + vPack.getItemName() + "\")) && object." + vPack.getMethod().getName() + "() != null";
							//put if around thing
							sw.println("if("+ifTest+") {");
							sw.indent();

							boolean validated = false;

							try {
								if(returnType.asSubclass(Object[].class) != null) {

									//get component type name
									Type componentType = returnType.getComponentType();
									String typeName = componentType.toString();
									typeName = typeName.substring(typeName.indexOf(" "));

									//dont bork the whole thing on brittle failure
									sw.println("try {");
									sw.indent();

									//GWT.create() validator
									sw.println("AbstractValidator<" + typeName + "> subValidator = GWT.create(" + typeName + ".class);");

									//get the object array
									sw.println("" + typeName + "[] objectArray = object." + vPack.getMethod().getName() + "();");

									//go through the array
									sw.println("for(" + typeName + " innerObject : objectArray) {");
									sw.indent();

									//create object identifier
									sw.println("String objIdent = innerObject.hashCode() + \":\" + grouplisting.hashCode();");
																		
									//make sure it isn't part of the processed objects list
									sw.println("if(!processedObjects.contains(objIdent)) {");
									sw.indent();
									
									//add to the processed list
									sw.println("processedObjects.add(objIdent);");
									
									//validate the object
									sw.println("iCSet.addAll(this.unrollConstraintSet(object, propertyName, subValidator.performValidation(innerObject, null, groups, processedGroups, processedObjects)));");
									
									//done checking
									sw.outdent();
									sw.println("}");
									
									//end for loop
									sw.outdent();
									sw.println("}");

									sw.outdent();
									sw.println("} catch (Exception ex) {");
									sw.indent();
									sw.println("//error handling goes here");
									sw.outdent();
									sw.println("}");

									//has been validated somehow, no other method needed
									validated = true;
								}
							} catch (ClassCastException ccex) {

							}

							if(!validated) {

								try {
									Class<?> collectionClass = returnType.asSubclass(Collection.class);

									if(collectionClass != null) {

										//get the type from the method declaration
										Type typeToValidate = ((ParameterizedType)vPack.getMethod().getGenericReturnType()).getActualTypeArguments()[0];
										String typeName = typeToValidate.toString();
										typeName = typeName.substring(typeName.indexOf(" "));

										//dont bork the whole thing on brittle failure
										sw.println("try {");
										sw.indent();

										//GWT.create() validator
										sw.println("AbstractValidator<" + typeName + "> subValidator = GWT.create(" + typeName + ".class);");

										//get object instance
										sw.println("Collection<" + typeName + "> objectCollection = (Collection<" + typeName + ">) object." + vPack.getMethod().getName() + "();");

										//go through the array
										sw.println("for(" + typeName + " innerObject : objectCollection) {");
										sw.indent();
										
										//create object identifier
										sw.println("String objIdent = innerObject.hashCode() + \":\" + grouplisting.hashCode();");

										//make sure it isn't part of the processed objects list
										sw.println("if(!processedObjects.contains(objIdent)) {");
										sw.indent();
										
										//add to the processed list
										sw.println("processedObjects.add(objIdent);");
										
										//validate the object
										sw.println("iCSet.addAll(this.unrollConstraintSet(object, propertyName, subValidator.performValidation(innerObject, null, groups, processedGroups, processedObjects)));");
										
										//done checking
										sw.outdent();
										sw.println("}");
										
										//end for loop
										sw.outdent();
										sw.println("}");

										sw.outdent();
										sw.println("} catch (Exception ex) {");
										sw.indent();
										sw.println("//error handling goes here");
										sw.outdent();
										sw.println("}");

										//has been validated somehow, no other method needed
										validated = true;
									}
								} catch (ClassCastException ccex) {

								}
							}

							if(!validated) {
								try {

									Class<?> mapClass = returnType.asSubclass(Map.class);

									if(mapClass != null) {

										//get the types from the method declaration
										Type keyType = ((ParameterizedType)vPack.getMethod().getGenericReturnType()).getActualTypeArguments()[0];
										String keyTypeName = keyType.toString();
										keyTypeName = keyTypeName.substring(keyTypeName.indexOf(" "));

										//get the type from the method declaration
										Type typeToValidate = ((ParameterizedType)vPack.getMethod().getGenericReturnType()).getActualTypeArguments()[1];
										String typeName = typeToValidate.toString();
										typeName = typeName.substring(typeName.indexOf(" "));

										//dont bork the whole thing on brittle failure
										sw.println("try {");
										sw.indent();

										//GWT.create() validator
										sw.println("AbstractValidator<" + typeName + "> subValidator = GWT.create(" + typeName + ".class);");

										sw.println("Map<" + keyTypeName + "," + typeName + "> objectMap = (Map<" + keyTypeName + "," + typeName + ">) object." + vPack.getMethod().getName() + "();");

										//go through the array
										sw.println("for(" + typeName + " innerObject : objectMap.values()) {");
										sw.indent();
										
										//create object identifier
										sw.println("String objIdent = innerObject.hashCode() + \":\" + grouplisting.hashCode();");
										
										//make sure it isn't part of the processed objects list
										sw.println("if(!processedObjects.contains(objIdent)) {");
										sw.indent();
										
										//add to the processed list
										sw.println("processedObjects.add(objIdent);");
										
										//validate the object
										sw.println("iCSet.addAll(this.unrollConstraintSet(object, propertyName, subValidator.performValidation(innerObject, null, groups, processedGroups, processedObjects)));");
										
										//done checking
										sw.outdent();
										sw.println("}");
										
										//close for loop
										sw.outdent();
										sw.println("}");

										sw.outdent();
										sw.println("} catch (Exception ex) {");
										sw.indent();
										sw.println("//error handling goes here");
										sw.outdent();
										sw.println("}");

										//has been validated somehow, no other method needed
										validated = true;
									}
								} catch (ClassCastException ccex) {

								}

							}

							//if some other method has not been used...
							if(!validated) {

								//dont bork the whole thing on brittle failure
								sw.println("try {");
								sw.indent();

								//type name
								String typeName = returnType.getCanonicalName();

								//GWT.create() validator
								sw.println("AbstractValidator<" + typeName + "> subValidator = GWT.create(" + typeName + ".class);");

								//get object
								sw.println("" + typeName + " innerObject = object." + vPack.getMethod().getName() + "();");

								//create object identifier
								sw.println("String objIdent = innerObject.hashCode() + \":\" + grouplisting.hashCode();");
								
								//make sure it isn't part of the processed objects list
								sw.println("if(!processedObjects.contains(objIdent)) {");
								sw.indent();
								
								//add to the processed list
								sw.println("processedObjects.add(objIdent);");
								
								//validate
								sw.println("iCSet.addAll(this.unrollConstraintSet(object, propertyName, subValidator.performValidation(innerObject, null, groups, processedGroups, processedObjects)));");

								//close for loop
								sw.outdent();
								sw.println("}");
								
								//finish
								sw.outdent();
								sw.println("} catch (Exception ex) {");
								sw.indent();
								sw.println("//error handling goes here");
								sw.outdent();
								sw.println("}");
							}

							//close if
							sw.outdent();
							sw.println("}");
						}

					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					}
				}

			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

			//return thing
			sw.println("return iCSet;");
			//done with method
			sw.outdent();
			//close validate method
			sw.println("}");

			//commit to tree logger
			sw.commit(this.logger);

			//output class name
			outputClassName = classType.getParameterizedQualifiedSourceName() + "Validator";

		} catch (NotFoundException e) {
			this.logger.log(TreeLogger.ERROR, "Type " + this.typeName + " not found.");
		}

		//output class name
		return outputClassName;
	}

	/**
	 * Create a source writer for classType + "Validator".
	 *
	 * @param classType
	 * @return
	 */
	private SourceWriter getSourceWriter(JClassType classType) {

		//get package
		String packageName = classType.getPackage().getName();
		String simpleName = classType.getSimpleSourceName() + "Validator";

		//get full name for package + validator
		//String fullName = packageName + "." + simpleName;

		//create validator that implements ivalidator for given typename
		ClassSourceFileComposerFactory composer = new ClassSourceFileComposerFactory(packageName, simpleName);

		//add the abstract validator method
		composer.setSuperclass("com.google.gwt.validation.client.AbstractValidator<" + classType.getSimpleSourceName() + ">");

		//add imports (other classes will be referenced by FULL class name)
		composer.addImport("java.util.HashSet");
		composer.addImport("java.util.HashMap");
		composer.addImport("java.util.Set");
		composer.addImport("java.util.Map");
		composer.addImport("java.util.Collection");
		composer.addImport("java.util.ArrayList");
		composer.addImport("com.google.gwt.validation.client.AbstractValidator");
		composer.addImport("com.google.gwt.validation.client.InvalidConstraint");
		composer.addImport("com.google.gwt.validation.client.interfaces.IValidator");
		composer.addImport("com.google.gwt.core.client.GWT");

		//create print writer for the given source, if the print writer is null it either indicates
		//a problem with the filesystem or a that the class had already been created and does not
		//need to be created again.  in either case a null sourcewriter would be created and the
		//creation method would return just the name that should have been used so that it can
		//be loaded by the classloader.
		PrintWriter printWriter = this.context.tryCreate(this.logger, packageName, simpleName);

		//set sourcewriter
		SourceWriter sw = null;

		//ensure a valid print writer
		if(printWriter != null) {
			//create sourcewriter
			sw = composer.createSourceWriter(this.context, printWriter);
			//this.logger.log(TreeLogger.INFO, "SourceWriter intialized from a non-null printwriter.");
		} else {
			//this.logger.log(TreeLogger.INFO, "PrintWriter was null so the validator class was most likely initialized already.");
		}

		//logging
		//this.logger.log(TreeLogger.INFO, "Validator dynamically created for " + fullName);

		//return sourcewriter
		return sw;
	}

	/**
	 * A helper method that writes a hashmap of properties for use in constructing
	 * the constraint for use in validation.
	 *
	 * @param propertyMap
	 * @param sw
	 */
	private void writeMapToCode(Map<String, String> propertyMap, SourceWriter sw) {

		//create empty object, regardless
		sw.println("HashMap<String,String> propertyMap = new HashMap<String,String>();");

		if(propertyMap == null || propertyMap.size() == 0) return;

		for(String key : propertyMap.keySet()) {
			//value
			String value = propertyMap.get(key);

			//put out propertykey
			//propertyMap.put(key, value);
			sw.println("propertyMap.put(\"" + key + "\",\"" + value + "\");");
		}


	}

}
