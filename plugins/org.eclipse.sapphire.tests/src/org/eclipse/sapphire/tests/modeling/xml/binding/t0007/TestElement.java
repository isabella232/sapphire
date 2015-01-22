/******************************************************************************
 * Copyright (c) 2015 Oracle and Accenture
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation
 *    Kamesh Sampath - [355751] General improvement of XML root binding API    
 ******************************************************************************/

package org.eclipse.sapphire.tests.modeling.xml.binding.t0007;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlNamespace;
import org.eclipse.sapphire.modeling.xml.annotations.XmlNamespaces;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 * @author <a href="mailto:kamesh.sampath@accenture.com">Kamesh Sampath</a> 
 */

@XmlNamespaces
(
    {
        @XmlNamespace( uri = "http://www.eclipse.org/sapphire/tests/xml/binding/0007/x", prefix = "y" ),
        @XmlNamespace( uri = "http://www.eclipse.org/sapphire/tests/xml/binding/0007/y", prefix = "y1" )
    }
)

@XmlBinding( path = "y:root" )

public interface TestElement extends Element
{
    ElementType TYPE = new ElementType( TestElement.class );
    
    // *** TestProperty ***
    
    @XmlBinding( path = "y1:child" )
    
    ValueProperty PROP_TEST_PROPERTY = new ValueProperty( TYPE, "TestProperty" );
    
    Value<String> getTestProperty();
    void setTestProperty( String value );
    
}
