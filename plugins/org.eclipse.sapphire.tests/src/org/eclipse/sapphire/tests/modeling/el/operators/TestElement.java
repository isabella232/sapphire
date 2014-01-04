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

package org.eclipse.sapphire.tests.modeling.el.operators;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementHandle;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementProperty;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Derived;
import org.eclipse.sapphire.modeling.annotations.Type;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public interface TestElement extends Element
{
    ElementType TYPE = new ElementType( TestElement.class );
    
    // *** Integer3 ***
    
    @Type( base = Integer.class )
    @Derived( text = "3" )
    
    ValueProperty PROP_INTEGER_3 = new ValueProperty( TYPE, "Integer3" );
    
    Value<Integer> getInteger3();
    
    // *** Integer5 ***
    
    @Type( base = Integer.class )
    @Derived( text = "5" )
    
    ValueProperty PROP_INTEGER_5 = new ValueProperty( TYPE, "Integer5" );
    
    Value<Integer> getInteger5();
    
    // *** BooleanTrue ***
    
    @Type( base = Boolean.class )
    @Derived( text = "true" )
    
    ValueProperty PROP_BOOLEAN_TRUE = new ValueProperty( TYPE, "BooleanTrue" );
    
    Value<Boolean> getBooleanTrue();
    
    // *** BooleanFalse ***
    
    @Type( base = Boolean.class )
    @Derived( text = "false" )
    
    ValueProperty PROP_BOOLEAN_FALSE = new ValueProperty( TYPE, "BooleanFalse" );
    
    Value<Boolean> getBooleanFalse();
    
    // *** EmptyList ***
    
    interface Entry extends Element
    {
        ElementType TYPE = new ElementType( Entry.class );
    }
    
    @Type( base = Entry.class )

    ListProperty PROP_EMPTY_LIST = new ListProperty( TYPE, "EmptyList" );
    
    ElementList<Entry> getEmptyList();
    
    // *** ChildElement ***
    
    @Type( base = Element.class )

    ElementProperty PROP_CHILD_ELEMENT = new ElementProperty( TYPE, "ChildElement" );
    
    ElementHandle<Element> getChildElement();

}
