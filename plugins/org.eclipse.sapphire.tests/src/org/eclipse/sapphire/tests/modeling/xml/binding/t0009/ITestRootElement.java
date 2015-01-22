/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.tests.modeling.xml.binding.t0009;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementHandle;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementProperty;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Type;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public interface ITestRootElement extends Element
{
    ElementType TYPE = new ElementType( ITestRootElement.class );
    
    // *** ValuePropertyA ***
    
    ValueProperty PROP_VALUE_PROPERTY_A = new ValueProperty( TYPE, "ValuePropertyA" );

    Value<String> getValuePropertyA();
    void setValuePropertyA( String value );

    // *** ValuePropertyB ***
    
    ValueProperty PROP_VALUE_PROPERTY_B = new ValueProperty( TYPE, "ValuePropertyB" );

    Value<String> getValuePropertyB();
    void setValuePropertyB( String value );
    
    // *** ListPropertyA ***
    
    @Type( base = ITestChildElement.class, possible = { ITestChildElementA.class, ITestChildElementB.class } )
    
    ListProperty PROP_LIST_PROPERTY_A = new ListProperty( TYPE, "ListPropertyA" );
    
    ElementList<ITestChildElement> getListPropertyA();
    
    // *** ElementPropertyA ***
    
    @Type( base = ITestChildElement.class, possible = { ITestChildElementA.class, ITestChildElementB.class } )
    
    ElementProperty PROP_ELEMENT_PROPERTY_A = new ElementProperty( TYPE, "ElementPropertyA" );
    
    ElementHandle<ITestChildElement> getElementPropertyA();

}
