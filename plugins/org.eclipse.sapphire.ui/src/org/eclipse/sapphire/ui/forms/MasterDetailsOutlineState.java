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

import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ImpliedElementProperty;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.ui.AttributesContainer;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public interface MasterDetailsOutlineState extends AttributesContainer
{
    ElementType TYPE = new ElementType( MasterDetailsOutlineState.class );
    
    // *** Visible ***
    
    @Type( base = Boolean.class )
    @DefaultValue( text = "true" )
    
    ValueProperty PROP_VISIBLE = new ValueProperty( TYPE, "Visible" );
    
    Value<Boolean> getVisible();
    void setVisible( String value );
    void setVisible( Boolean value );
    
    // *** Ratio ***
    
    @Type( base = Double.class )
    @DefaultValue( text = "0.3" )

    ValueProperty PROP_RATIO = new ValueProperty( TYPE, "Ratio" );
    
    Value<Double> getRatio();
    void setRatio( String value );
    void setRatio( Double value );
    
    // *** Root ***
    
    @Type( base = MasterDetailsNodeState.class )

    ImpliedElementProperty PROP_ROOT = new ImpliedElementProperty( TYPE, "Root" );
    
    MasterDetailsNodeState getRoot();
    
}
