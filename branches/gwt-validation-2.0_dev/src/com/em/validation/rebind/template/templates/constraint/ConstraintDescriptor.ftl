package ${targetPackage};

//generic imports
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintValidator;
import javax.validation.Payload;
import javax.validation.metadata.ConstraintDescriptor;
import com.em.validation.client.metadata.AbstractConstraintDescriptor;
import com.em.validation.client.generated.factory.AnnotationInstanceFactory;

//the target constraint annotation
import ${annotationImportName};

public enum ${generatedName} implements ConstraintDescriptor<${targetAnnotation}> {

	INSTANCE;

	private AbstractConstraintDescriptor<${targetAnnotation}> instance = null;
	
	private ${generatedName}() {
		this.instance = new AbstractConstraintDescriptor<${targetAnnotation}>() {
			public void init() {
				//create underlying annotation
				this.annotation = AnnotationInstanceFactory.INSTANCE.getAnnotationFactory(${targetAnnotation}.class).getAnnotation("${signature}");
		
				//get property map
				this.propertyMap = AnnotationInstanceFactory.INSTANCE.getAnnotationFactory(${targetAnnotation}.class).getPropertyMap("${signature}");
			
				//save composed constraints
				<#list composedOf as composed>
				this.composedOf.add(${composed.className}.INSTANCE);
				</#list>
				
				//report as single violation
				<#if reportAsSingleViolation != "null">
				this.reportAsSingleViolation = ${reportAsSingleViolation};
				</#if>
			}
			
			public List<Class<? extends ConstraintValidator<${targetAnnotation}, ?>>> getConstraintValidatorClasses() {
				List<Class<? extends ConstraintValidator<${targetAnnotation}, ?>>> validatedBy = new ArrayList<Class<? extends ConstraintValidator<${targetAnnotation}, ?>>>();
				<#list validatedBy as validator>
				validatedBy.add(${validator}.class);
				</#list>	
				return validatedBy;
			}
		};
	} 
	
	@Override
	public Map<String, Object> getAttributes() {
		return this.instance.getAttributes();
	}
	
	@Override
	public Set<ConstraintDescriptor<?>> getComposingConstraints() {
		return this.instance.getComposingConstraints();
	}
	
	@Override
	public boolean isReportAsSingleViolation() {
		return this.instance.isReportAsSingleViolation();
	}
	
	@Override
	public Set<Class<?>> getGroups() {
		return this.instance.getGroups();
	}

	@Override
	public Set<Class<? extends Payload>> getPayload() {
		return this.instance.getPayload();
	}
	
	@Override
	public ${targetAnnotation} getAnnotation() {
		return this.instance.getAnnotation();
	}

	@Override
	public List<Class<? extends ConstraintValidator<${targetAnnotation}, ?>>> getConstraintValidatorClasses() {
		return this.instance.getConstraintValidatorClasses();
	}
	
}