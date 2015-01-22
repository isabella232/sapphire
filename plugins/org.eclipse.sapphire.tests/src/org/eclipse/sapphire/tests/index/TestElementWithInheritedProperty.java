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

package org.eclipse.sapphire.tests.index;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Type;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public interface TestElementWithInheritedProperty extends Element
{
    ElementType TYPE = new ElementType( TestElementWithInheritedProperty.class );
    
    // *** List ***
    
    interface ListEntryBase extends Element
    {
        ElementType TYPE = new ElementType( ListEntryBase.class );
        
        // *** Value ***
        
        ValueProperty PROP_VALUE = new ValueProperty( TYPE, "Value" );
        
        Value<String> getValue();
        void setValue( String value );
    }

    interface ListEntry extends ListEntryBase
    {
        ElementType TYPE = new ElementType( ListEntry.class );
    }
    
    @Type( base = ListEntry.class )

    ListProperty PROP_LIST = new ListProperty( TYPE, "List" );
    
    ElementList<ListEntry> getList();
    
}
