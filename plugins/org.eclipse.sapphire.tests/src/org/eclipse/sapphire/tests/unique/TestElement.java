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

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.NoDuplicates;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@XmlBinding( path = "root" )

public interface TestElement extends Element
{
    ElementType TYPE = new ElementType( TestElement.class );
    
    // *** List ***
    
    @XmlBinding( path = "entry" )
    
    interface ListEntry extends Element
    {
        ElementType TYPE = new ElementType( ListEntry.class );
        
        // *** Value ***
        
        @NoDuplicates
        @XmlBinding( path = "" )
        
        ValueProperty PROP_VALUE = new ValueProperty( TYPE, "Value" );
        
        Value<String> getValue();
        void setValue( String value );
    }
    
    @Type( base = ListEntry.class )
    @XmlListBinding( path = "" )

    ListProperty PROP_LIST = new ListProperty( TYPE, "List" );
    
    ElementList<ListEntry> getList();
    
}
