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

package org.eclipse.sapphire.tests.modeling.xml;

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

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateImpl

public interface IDefaultXmlBindingTestModel

    extends IModelElement
    
{
    ModelElementType TYPE = new ModelElementType( IDefaultXmlBindingTestModel.class );
    
    // *** ValuePropertyA ***
    
    ValueProperty PROP_VALUE_PROPERTY_A = new ValueProperty( TYPE, "ValuePropertyA" );

    Value<String> getValuePropertyA();
    void setValuePropertyA( String value );

    // *** ValuePropertyB ***
    
    ValueProperty PROP_VALUE_PROPERTY_B = new ValueProperty( TYPE, "ValuePropertyB" );

    Value<String> getValuePropertyB();
    void setValuePropertyB( String value );
    
    // *** ListPropertyA ***
    
    @Type( base = IDefaultXmlBindingTestModelChild.class, possible = { IDefaultXmlBindingTestModelChildA.class, IDefaultXmlBindingTestModelChildB.class } )
    
    ListProperty PROP_LIST_PROPERTY_A = new ListProperty( TYPE, "ListPropertyA" );
    
    ModelElementList<IDefaultXmlBindingTestModelChild> getListPropertyA();
    
    // *** ElementPropertyA ***
    
    @Type( base = IDefaultXmlBindingTestModelChild.class, possible = { IDefaultXmlBindingTestModelChildA.class, IDefaultXmlBindingTestModelChildB.class } )
    
    ElementProperty PROP_ELEMENT_PROPERTY_A = new ElementProperty( TYPE, "ElementPropertyA" );
    
    ModelElementHandle<IDefaultXmlBindingTestModelChild> getElementPropertyA();

}
