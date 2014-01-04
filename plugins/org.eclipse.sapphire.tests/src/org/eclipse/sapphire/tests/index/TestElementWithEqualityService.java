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

package org.eclipse.sapphire.tests.index;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Service;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.services.EqualityService;
import org.eclipse.sapphire.util.EqualsFactory;
import org.eclipse.sapphire.util.HashCodeFactory;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public interface TestElementWithEqualityService extends Element
{
    ElementType TYPE = new ElementType( TestElementWithEqualityService.class );
    
    // *** List ***
    
    class ListEntryEqualityService extends EqualityService
    {
        @Override
        public boolean doEquals( final Object obj )
        {
            if( obj instanceof ListEntry )
            {
                return EqualsFactory.start().add( context( ListEntry.class ).getValue().content(), ( (ListEntry) obj ).getValue().content() ).result();
            }
            
            return false;
        }

        @Override
        public int doHashCode()
        {
            return HashCodeFactory.start().add( context( ListEntry.class ).getValue().content() ).result();
        }
    }
    
    @Service( impl = ListEntryEqualityService.class )
    
    interface ListEntry extends Element
    {
        ElementType TYPE = new ElementType( ListEntry.class );
        
        // *** Value ***
        
        ValueProperty PROP_VALUE = new ValueProperty( TYPE, "Value" );
        
        Value<String> getValue();
        void setValue( String value );
    }
    
    @Type( base = ListEntry.class )

    ListProperty PROP_LIST = new ListProperty( TYPE, "List" );
    
    ElementList<ListEntry> getList();
    
}
