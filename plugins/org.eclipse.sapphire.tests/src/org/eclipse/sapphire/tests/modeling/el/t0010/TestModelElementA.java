/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.tests.modeling.el.t0010;

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

public interface TestModelElementA extends Element
{
    ElementType TYPE = new ElementType( TestModelElementA.class );
    
    // *** Element1 ***
    
    @Type( base = TestModelElementA.class )
    
    ElementProperty PROP_ELEMENT_1 = new ElementProperty( TYPE, "Element1" );
    
    ElementHandle<TestModelElementA> getElement1();
    
    // *** List1 ***
    
    @Type( base = TestModelElementA.class )
    
    ListProperty PROP_LIST_1 = new ListProperty( TYPE, "List1" );
    
    ElementList<TestModelElementA> getList1();
    
    // *** Value1 ***
    
    ValueProperty PROP_VALUE_1 = new ValueProperty( TYPE, "Value1" );
    
    Value<String> getValue1();
    void setValue1( String value );
    
    // *** Value2 ***
    
    @Type( base = Integer.class )

    ValueProperty PROP_VALUE_2 = new ValueProperty( TYPE, "Value2" );
    
    Value<Integer> getValue2();
    void setValue2( String value );
    void setValue2( Integer value );
    
}
