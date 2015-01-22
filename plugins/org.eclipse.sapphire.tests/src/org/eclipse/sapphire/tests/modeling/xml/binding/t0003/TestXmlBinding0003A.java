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

package org.eclipse.sapphire.tests.modeling.xml.binding.t0003;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementHandle;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementProperty;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlElementBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlNamespace;
import org.eclipse.sapphire.modeling.xml.annotations.XmlNamespaces;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 * @author <a href="mailto:kamesh.sampath@accenture.com">Kamesh Sampath</a> 
 */

@XmlNamespaces
(
    value = 
    {
        @XmlNamespace( uri = "http://www.eclipse.org/sapphire/tests/xml/binding/0003/x", prefix = "x" ),
        @XmlNamespace( uri = "http://www.eclipse.org/sapphire/tests/xml/binding/0003/y", prefix = "y" ),
        @XmlNamespace( uri = "http://www.eclipse.org/sapphire/tests/xml/binding/0003/z", prefix = "" )
    }
)

@XmlBinding( path = "a" )

public interface TestXmlBinding0003A extends Element
{
    ElementType TYPE = new ElementType( TestXmlBinding0003A.class );
    
    // *** Aa ***
    
    @XmlBinding( path = "aa" )
    
    ValueProperty PROP_AA = new ValueProperty( TYPE, "Aa" );
    
    Value<String> getAa();
    void setAa( String value );
    
    // *** Ab ***

    @Type( base = TestXmlBinding0003AB.class )
    @XmlElementBinding( mappings = @XmlElementBinding.Mapping( element = "y:ab", type = TestXmlBinding0003AB.class ) )
    
    ElementProperty PROP_AB = new ElementProperty( TYPE, "Ab" );
    
    ElementHandle<TestXmlBinding0003AB> getAb();
    
    // *** Ac ***

    @Type( base = TestXmlBinding0003AC.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "x:ac", type = TestXmlBinding0003AC.class ) )
    
    ListProperty PROP_AC = new ListProperty( TYPE, "Ac" );
    
    ElementList<TestXmlBinding0003AC> getAc();
    
}
