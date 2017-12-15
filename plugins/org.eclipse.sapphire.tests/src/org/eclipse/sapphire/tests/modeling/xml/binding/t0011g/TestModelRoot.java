/*******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance    
 ******************************************************************************/

package org.eclipse.sapphire.tests.modeling.xml.binding.t0011g;

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

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateImpl
@XmlBinding( path = "root" )

public interface TestModelRoot extends IModelElement
{
    ModelElementType TYPE = new ModelElementType( TestModelRoot.class );
    
    // *** List ***
    
    @Type( base = TestModelElementA.class, possible = { TestModelElementA1.class, TestModelElementA2.class } )
    
    @XmlListBinding
    (
        path = "x/y1/z", 
        mappings = 
        {
            @XmlListBinding.Mapping( element = "a1", type = TestModelElementA1.class ),
            @XmlListBinding.Mapping( element = "a2", type = TestModelElementA2.class )
        }
    )
    
    ListProperty PROP_LIST = new ListProperty( TYPE, "List" );
    
    ModelElementList<TestModelElementA> getList();

    // *** Element ***
    
    @Type( base = TestModelElementB.class, possible = { TestModelElementB1.class, TestModelElementB2.class } )
    
    @XmlElementBinding
    (
        path = "x/y2",
        mappings = 
        {
            @XmlElementBinding.Mapping( element = "b1", type = TestModelElementB1.class ),
            @XmlElementBinding.Mapping( element = "b2", type = TestModelElementB2.class )
        }
    )
    
    ElementProperty PROP_ELEMENT = new ElementProperty( TYPE, "Element" );
    
    ModelElementHandle<TestModelElementB> getElement();

}
