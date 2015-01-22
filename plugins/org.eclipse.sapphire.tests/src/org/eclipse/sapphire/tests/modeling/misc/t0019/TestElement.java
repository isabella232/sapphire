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

package org.eclipse.sapphire.tests.modeling.misc.t0019;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementHandle;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementProperty;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ImpliedElementProperty;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.Transient;
import org.eclipse.sapphire.TransientProperty;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Type;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public interface TestElement extends Element
{
    ElementType TYPE = new ElementType( TestElement.class );
    
    interface Child extends Element
    {
        ElementType TYPE = new ElementType( TestElement.Child.class );
        
        // *** ValueProperty ***
        
        ValueProperty PROP_VALUE_PROPERTY = new ValueProperty( TYPE, "ValueProperty" );
        
        Value<String> getValueProperty();
        void setValueProperty( String value );
    }
    
    // *** StringValueProperty ***
    
    ValueProperty PROP_STRING_VALUE_PROPERTY = new ValueProperty( TYPE, "StringValueProperty" );
    
    Value<String> getStringValueProperty();
    void setStringValueProperty( String value );
    
    // *** IntegerValueProperty ***
    
    @Type( base = Integer.class )

    ValueProperty PROP_INTEGER_VALUE_PROPERTY = new ValueProperty( TYPE, "IntegerValueProperty" );
    
    Value<Integer> getIntegerValueProperty();
    void setIntegerValueProperty( String value );
    void setIntegerValueProperty( Integer value );
    
    // *** TransientProperty ***
    
    @Type( base = Object.class )

    TransientProperty PROP_TRANSIENT_PROPERTY = new TransientProperty( TYPE, "TransientProperty" );
    
    Transient<Object> getTransientProperty();
    void setTransientProperty( Object value );
    
    // *** ElementProperty ***
    
    @Type( base = Child.class )
    
    ElementProperty PROP_ELEMENT_PROPERTY = new ElementProperty( TYPE, "ElementProperty" );
    
    ElementHandle<Child> getElementProperty();
    
    // *** ImpliedElementProperty ***
    
    @Type( base = Child.class )

    ImpliedElementProperty PROP_IMPLIED_ELEMENT_PROPERTY = new ImpliedElementProperty( TYPE, "ImpliedElementProperty" );
    
    Child getImpliedElementProperty();
    
    // *** ListProperty ***
    
    @Type( base = Child.class )
    
    ListProperty PROP_LIST_PROPERTY = new ListProperty( TYPE, "ListProperty" );
    
    ElementList<Child> getListProperty();

}