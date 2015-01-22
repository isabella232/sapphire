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

package org.eclipse.sapphire.ui;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DelegateImplementation;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.Type;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public interface AttributesContainer extends Element
{
    ElementType TYPE = new ElementType( AttributesContainer.class );
    
    // *** Attributes ***
    
    interface Attribute extends Element
    {
        ElementType TYPE = new ElementType( AttributesContainer.Attribute.class );
        
        // *** Name ***
        
        @Required
        
        ValueProperty PROP_NAME = new ValueProperty( TYPE, "Name" );
        
        Value<String> getName();
        void setName( String value );
        
        // *** Value ***
        
        @Required
        
        ValueProperty PROP_VALUE = new ValueProperty( TYPE, "Value" );
        
        Value<String> getValue();
        void setValue( String value );
    }

    @Type( base = Attribute.class )

    ListProperty PROP_ATTRIBUTES = new ListProperty( TYPE, "Attributes" );
    
    ElementList<Attribute> getAttributes();
    
    // *** Method: getAttribute ***
    
    @DelegateImplementation( AttributesContainerMethods.class )
    
    String getAttribute( String name );
    
    @DelegateImplementation( AttributesContainerMethods.class )
    
    <T> T getAttribute( String name, T def );
    
    // *** Method: setAttribute ***
    
    @DelegateImplementation( AttributesContainerMethods.class )
    
    void setAttribute( String name, Object value );
    
}
