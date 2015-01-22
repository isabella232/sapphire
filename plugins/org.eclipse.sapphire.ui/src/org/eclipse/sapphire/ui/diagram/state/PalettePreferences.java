/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.diagram.state;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.Type;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public interface PalettePreferences extends Element 
{
	ElementType TYPE = new ElementType( PalettePreferences.class );

    // *** DockLocation ***

    @Type( base = Integer.class )
    @DefaultValue( text = "0" )

    ValueProperty PROP_DOCK_LOCATION = new ValueProperty( TYPE, "DockLocation" );
    
    Value<Integer> getDockLocation();
    void setDockLocation( String value );
    void setDockLocation( Integer value );
    
    // *** PaletteState ***
    
    @Type( base = Integer.class )
    @DefaultValue( text = "4" )

    ValueProperty PROP_PALETTE_STATE = new ValueProperty( TYPE, "PaletteState" );
    
    Value<Integer> getPaletteState();
    void setPaletteState( String value );
    void setPaletteState( Integer value );
	
    // *** PaletteWidth ***
    
    @Type( base = Integer.class )
    @DefaultValue( text = "0" )

    ValueProperty PROP_PALETTE_WIDTH = new ValueProperty( TYPE, "PaletteWidth" );
    
    Value<Integer> getPaletteWidth();
    void setPaletteWidth( String value );
    void setPaletteWidth( Integer value );
        
}
