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

package org.eclipse.sapphire.tests.unique;

import org.eclipse.sapphire.Collation;
import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.NoDuplicates;
import org.eclipse.sapphire.modeling.annotations.Type;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public interface TestElementCustomCollation extends Element
{
    ElementType TYPE = new ElementType( TestElementCustomCollation.class );
    
    // *** IgnoreCaseDifferences ***
    
    @Type( base = Boolean.class )
    @DefaultValue( text = "false" )
    
    ValueProperty PROP_IGNORE_CASE_DIFFERENCES = new ValueProperty( TYPE, "IgnoreCaseDifferences" );
    
    Value<Boolean> getIgnoreCaseDifferences();
    void setIgnoreCaseDifferences( String value );
    void setIgnoreCaseDifferences( Boolean value );
    
    // *** List ***
    
    interface ListEntry extends Element
    {
        ElementType TYPE = new ElementType( ListEntry.class );
        
        // *** Value ***
        
        @NoDuplicates
        @Collation( ignoreCaseDifferences = "${ Parent().IgnoreCaseDifferences }" )
        
        ValueProperty PROP_VALUE = new ValueProperty( TYPE, "Value" );
        
        Value<String> getValue();
        void setValue( String value );
    }
    
    @Type( base = ListEntry.class )

    ListProperty PROP_LIST = new ListProperty( TYPE, "List" );
    
    ElementList<ListEntry> getList();
    
}
