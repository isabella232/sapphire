/*******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance    
 ******************************************************************************/

package org.eclipse.sapphire.tests.modeling.xml.binding.t0011h;

import org.eclipse.sapphire.modeling.ElementProperty;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementHandle;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlElementBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlNamespace;
import org.eclipse.sapphire.modeling.xml.annotations.XmlNamespaces;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateImpl

@XmlNamespaces
(
    {
        @XmlNamespace( uri = "http://www.eclipse.org/sapphire/tests/xml/binding/0011h/1", prefix = "" ),
        @XmlNamespace( uri = "http://www.eclipse.org/sapphire/tests/xml/binding/0011h/2", prefix = "ns2" ),
        @XmlNamespace( uri = "http://www.eclipse.org/sapphire/tests/xml/binding/0011h/3", prefix = "ns3" ),
        @XmlNamespace( uri = "http://www.eclipse.org/sapphire/tests/xml/binding/0011h/4", prefix = "ns4" ),
        @XmlNamespace( uri = "http://www.eclipse.org/sapphire/tests/xml/binding/0011h/5", prefix = "ns5" )
    }
)

@XmlBinding( path = "root" )

public interface TestModelRoot extends IModelElement
{
    ModelElementType TYPE = new ModelElementType( TestModelRoot.class );
    
    // *** List ***
    
    @Type( base = TestModelElementA.class, possible = { TestModelElementA1.class, TestModelElementA2.class } )
    
    @XmlListBinding
    (
        path = "x/ns2:y1/ns2:z", 
        mappings = 
        {
            @XmlListBinding.Mapping( element = "ns4:a1", type = TestModelElementA1.class ),
            @XmlListBinding.Mapping( element = "ns4:a2", type = TestModelElementA2.class )
        }
    )
    
    ListProperty PROP_LIST = new ListProperty( TYPE, "List" );
    
    ModelElementList<TestModelElementA> getList();

    // *** Element ***
    
    @Type( base = TestModelElementB.class, possible = { TestModelElementB1.class, TestModelElementB2.class } )
    
    @XmlElementBinding
    (
        path = "x/ns3:y2",
        mappings = 
        {
            @XmlElementBinding.Mapping( element = "ns5:b1", type = TestModelElementB1.class ),
            @XmlElementBinding.Mapping( element = "ns5:b2", type = TestModelElementB2.class )
        }
    )
    
    ElementProperty PROP_ELEMENT = new ElementProperty( TYPE, "Element" );
    
    ModelElementHandle<TestModelElementB> getElement();

}
