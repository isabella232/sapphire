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

package org.eclipse.sapphire.ui.forms;

import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.ui.AttributesContainer;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public interface MasterDetailsNodeState extends AttributesContainer
{
    ElementType TYPE = new ElementType( MasterDetailsNodeState.class );
    
    // *** Label ***
    
    ValueProperty PROP_LABEL = new ValueProperty( TYPE, "Label" );
    
    Value<String> getLabel();
    void setLabel( String value );
    
    // *** Expanded ***
    
    @Type( base = Boolean.class )
    @DefaultValue( text = "false" )
    
    ValueProperty PROP_EXPANDED = new ValueProperty( TYPE, "Expanded" );
    
    Value<Boolean> getExpanded();
    void setExpanded( String value );
    void setExpanded( Boolean value );
    
    // *** Selected ***
    
    @Type( base = Boolean.class )
    @DefaultValue( text = "false" )
    
    ValueProperty PROP_SELECTED = new ValueProperty( TYPE, "Selected" );
    
    Value<Boolean> getSelected();
    void setSelected( String value );
    void setSelected( Boolean value );
    
    // *** Children ***
    
    @Type( base = MasterDetailsNodeState.class )
    
    ListProperty PROP_CHILDREN = new ListProperty( TYPE, "Children" );
    
    ElementList<MasterDetailsNodeState> getChildren();
    
}
