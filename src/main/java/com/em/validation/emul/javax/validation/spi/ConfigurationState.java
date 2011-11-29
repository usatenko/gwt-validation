// $Id: ConfigurationState.java 17620 2009-10-04 19:19:28Z hardy.ferentschik $
/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

/* 
GWT Validation Framework - A JSR-303 validation framework for GWT

(c) gwt-validation contributors (http://code.google.com/p/gwt-validation/)

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/
package javax.validation.spi;

import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.TraversableResolver;

/**
 * Contract between a <code>Configuration</code> and a
 * </code>ValidatorProvider</code> to create a <code>ValidatorFactory</code>.
 * The configuration artifacts defined in the XML configuration and provided to the
 * <code>Configuration</code> are merged and passed along via
 * <code>ConfigurationState</code>.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 */
public interface ConfigurationState {

	/**
	 * Returns true if Configuration.ignoreXMLConfiguration() has been called
	 * In this case, the ValidatorFactory must ignore META-INF/validation.xml
	 *
	 * @return {@code true} if META-INF/validation.xml should be ignored
	 */
	boolean isIgnoreXmlConfiguration();

	/**
	 * Returns the message interpolator of this configuration.
	 * Message interpolator is defined in the following decreasing priority:
	 * <ul>
	 * <li>set via the <code>Configuration</code> programmatic API</li>
	 * <li>defined in META-INF/validation.xml provided that ignoreXmlConfiguration
	 * is false. In this case the instance is created via its no-arg constructor.</li>
	 * <li>{@code null} if undefined.</li>
	 * </ul>
	 *
	 * @return message provider instance or null if not defined
	 */
	MessageInterpolator getMessageInterpolator();

	/**
	 * Returns a set of configuration streams.
	 * The streams are defined by:
	 * <ul>
	 * <li>mapping XML streams passed programmatically in <code>Configuration</code></li>
	 * <li>mapping XML stream located in the resources defined in</li>
	 * META-INF/validation.xml (constraint-mapping element)
	 * </ul>
	 * Streams represented in the XML configuration and opened by the
	 * <code>Configuration</code> implementation must be closed by the
	 * <code>Configuration</code> implementation after the <code>ValidatorFactory</code>
	 * creation (or if an exception occurs).
	 *
	 * @return set of input stream
	 */
	//Set<InputStream> getMappingStreams();

	/**
	 * Returns the constraint validator factory of this configuration.
	 * The {@code ConstraintValidatorFactory} implementation is defined in the following
	 * decreasing priority:
	 * <ul>
	 * <li>set via the <code>Configuration</code> programmatic API</li>
	 * <li>defined in META-INF/validation.xml provided that ignoredXmlConfiguration
	 * is false. In this case the instance is created via its no-arg constructor.</li>
	 * <li>{@code null} if undefined.</li>
	 * </ul>
	 *
	 * @return factory instance or {@code null} if not defined
	 */
	ConstraintValidatorFactory getConstraintValidatorFactory();

	/**
	 * Returns the traversable resolver for this configuration.
	 * <code>TraversableResolver</code> is defined in the following decreasing priority:
	 * <ul>
	 * <li>set via the <code>Configuration</code> programmatic API</li>
	 * <li>defined in META-INF/validation.xml provided that ignoredXmlConfiguration
	 * is false. In this case the instance is created via its no-arg constructor.</li>
	 * <li>{@code null} if undefined.</li>
	 * </ul>
	 *
	 * @return traversable provider instance or {@code null} if not defined
	 */
	TraversableResolver getTraversableResolver();

	/**
	 * Returns a map of non type-safe custom properties.
	 * Properties defined via:
	 * <ul>
	 * <li>Configuration.addProperty(String, String)</li>
	 * <li>META-INF/validation.xml provided that ignoredXmlConfiguration</li>
	 * is false.
	 * </ul>
	 * If a property is defined both programmatically and in XML,
	 * the value defined programmatically has priority
	 *
	 * @return Map whose key is the property key and the value the property value
	 */
	Map<String, String> getProperties();
}