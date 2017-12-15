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

package org.eclipse.sapphire.tests.modeling.xml.binding.t0003;

import org.eclipse.sapphire.modeling.ElementProperty;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementHandle;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlElementBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlNamespace;
import org.eclipse.sapphire.modeling.xml.annotations.XmlNamespaces;
import org.eclipse.sapphire.modeling.xml.annotations.XmlRootBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateImpl
@XmlRootBinding( namespace = "http://www.eclipse.org/sapphire/tests/xml/binding/0003/z", elementName = "a" )

@XmlNamespaces
(
    value = 
    {
        @XmlNamespace( uri = "http://www.eclipse.org/sapphire/tests/xml/binding/0003/y", prefix = "y" ),
        @XmlNamespace( uri = "http://www.eclipse.org/sapphire/tests/xml/binding/0003/x", prefix = "x" )
    }
)

public interface ITestXmlBinding0003A extends IModelElement
{
    ModelElementType TYPE = new ModelElementType( ITestXmlBinding0003A.class );
    
    // *** Aa ***
    
    @XmlBinding( path = "aa" )
    
    ValueProperty PROP_AA = new ValueProperty( TYPE, "Aa" );
    
    Value<String> getAa();
    void setAa( String value );
    
    // *** Ab ***

    @Type( base = ITestXmlBinding0003AB.class )
    @XmlElementBinding( mappings = @XmlElementBinding.Mapping( element = "y:ab", type = ITestXmlBinding0003AB.class ) )
    
    ElementProperty PROP_AB = new ElementProperty( TYPE, "Ab" );
    
    ModelElementHandle<ITestXmlBinding0003AB> getAb();
    
    // *** Ac ***

    @Type( base = ITestXmlBinding0003AC.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "x:ac", type = ITestXmlBinding0003AC.class ) )
    
    ListProperty PROP_AC = new ListProperty( TYPE, "Ac" );
    
    ModelElementList<ITestXmlBinding0003AC> getAc();
    
}
