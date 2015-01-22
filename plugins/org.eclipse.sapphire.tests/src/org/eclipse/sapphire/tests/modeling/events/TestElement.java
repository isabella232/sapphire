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

package org.eclipse.sapphire.tests.modeling.events;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.Enablement;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.Type;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public interface TestElement extends Element
{
    ElementType TYPE = new ElementType( TestElement.class );
    
    // *** Enablement ***

    @Type( base = Boolean.class )
    @DefaultValue( text = "true" )

    ValueProperty PROP_ENABLEMENT = new ValueProperty( TYPE, "Enablement" );

    Value<Boolean> getEnablement();
    void setEnablement( String value );
    void setEnablement( Boolean value );
    
    // *** ValuePlain ***
    
    ValueProperty PROP_VALUE_PLAIN = new ValueProperty( TYPE, "ValuePlain" );
    
    Value<String> getValuePlain();
    void setValuePlain( String value );
    
    // *** ValueConstrained ***
    
    @Required
    @Enablement( expr = "${ Enablement }" )
    
    ValueProperty PROP_VALUE_CONSTRAINED = new ValueProperty( TYPE, "ValueConstrained" );
    
    Value<String> getValueConstrained();
    void setValueConstrained( String value );
    
    // *** List ***
    
    interface ListEntry extends Element
    {
        ElementType TYPE = new ElementType( ListEntry.class );
        
        // *** Value ***
        
        ValueProperty PROP_VALUE = new ValueProperty( TYPE, "Value" );
        
        Value<String> getValue();
        void setValue( String value );
        
        // *** Children ***
        
        @Type( base = ListEntry.class )
        
        ListProperty PROP_CHILDREN = new ListProperty( TYPE, "Children" );
        
        ElementList<ListEntry> getChildren();
     }
    
    @Type( base = ListEntry.class )

    ListProperty PROP_LIST = new ListProperty( TYPE, "List" );
    
    ElementList<ListEntry> getList();
    
}