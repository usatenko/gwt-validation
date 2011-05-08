package com.em.validation.client.reflector;

import java.util.Set;

import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.ConstraintDescriptor;

import com.em.validation.client.GwtValidationBaseTestCase;
import com.em.validation.client.metadata.factory.DescriptorFactory;
import com.em.validation.client.model.composed.ComposedConstraint;
import com.em.validation.client.model.composed.ComposedSingleViolationConstraint;
import com.em.validation.client.model.composed.ComposedTestClass;
import com.em.validation.client.model.generic.TestClass;

public class ReflectorGenerationTest extends GwtValidationBaseTestCase {
	
	public void testConstraintGeneration() {
		//get the factory instance
		IReflectorFactory factory = ReflectorFactory.INSTANCE;

		//assert that we got a usable factory
		assertNotNull(factory);
		
		//test class
		TestClass testClassInstance = new TestClass();
		IReflector<TestClass> testClassReflector = factory.getReflector(TestClass.class);
		
		assertEquals(0, testClassReflector.getValue("testInt", testClassInstance));
		//set new value
		testClassInstance.setTestInt(430);
		assertEquals(430, testClassReflector.getValue("testInt", testClassInstance));	
		
	}
	
	public void testComposedConstraints() {
		//test composed constraint
		BeanDescriptor beanDesc = DescriptorFactory.INSTANCE.getBeanDescriptor(ComposedTestClass.class);
		
		Set<ConstraintDescriptor<?>> constraints = beanDesc.getConstraintDescriptors();
		
		assertEquals(2, constraints.size());
		
		for(ConstraintDescriptor<?> descriptor : constraints) {
			if(ComposedConstraint.class.equals(descriptor.getAnnotation().annotationType())){
				assertEquals(2, descriptor.getComposingConstraints().size());
			} else if(ComposedSingleViolationConstraint.class.equals(descriptor.getAnnotation().annotationType())){
				assertTrue(descriptor.isReportAsSingleViolation());
				assertEquals(3, descriptor.getComposingConstraints().size());
			}
		}
		
	}

}
