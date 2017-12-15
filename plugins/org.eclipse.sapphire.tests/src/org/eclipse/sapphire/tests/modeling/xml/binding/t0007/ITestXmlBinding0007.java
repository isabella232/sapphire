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

package org.eclipse.sapphire.tests.modeling.xml.binding.t0007;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlNamespace;
import org.eclipse.sapphire.modeling.xml.annotations.XmlRootBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateImpl
@XmlRootBinding( namespace = "http://www.eclipse.org/sapphire/tests/xml/binding/0007/x", elementName = "root" )
@XmlNamespace( uri = "http://www.eclipse.org/sapphire/tests/xml/binding/0007/y", prefix = "y" )

public interface ITestXmlBinding0007 extends IModelElement
{
    ModelElementType TYPE = new ModelElementType( ITestXmlBinding0007.class );
    
    // *** TestProperty ***
    
    @XmlBinding( path = "y:child" )
    
    ValueProperty PROP_TEST_PROPERTY = new ValueProperty( TYPE, "TestProperty" );
    
    Value<String> getTestProperty();
    void setTestProperty( String value );
    
}
