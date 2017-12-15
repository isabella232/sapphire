/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.modeling.annotations.processor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.sapphire.modeling.annotations.GenerateStub;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.NamedValues;
import org.eclipse.sapphire.modeling.xml.annotations.GenerateXmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.GenerateXmlBindingModelImpl;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class APFactory implements AnnotationProcessorFactory {

	private static final String[] SUPPORTED_ANNOTATIONS = 
	{
		GenerateXmlBinding.class.getName(),
		GenerateXmlBindingModelImpl.class.getName(),
		GenerateStub.class.getName(),
        Label.class.getName(),
        NamedValues.class.getName()
	};

	public AnnotationProcessor getProcessorFor( final Set<AnnotationTypeDeclaration> atds,
			                                    final AnnotationProcessorEnvironment env) 
	{
		return new Processor(atds, env);
	}

	public Collection<String> supportedAnnotationTypes() 
	{
		Set<String> supportedAnnotations = new HashSet<String>(SUPPORTED_ANNOTATIONS.length);
		for (String annotation : SUPPORTED_ANNOTATIONS) {
			supportedAnnotations.add(annotation);
		}
		return supportedAnnotations;
	}

	public Collection<String> supportedOptions() 
	{
		return null;
	}

}
