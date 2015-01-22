/******************************************************************************
 * Copyright (c) 2015 Liferay and Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Gregory Amerson - initial implementation
 *    Konstantin Komissarchik - initial implementation review and related changes
 ******************************************************************************/

package org.eclipse.sapphire.ui.diagram.state;

import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ImpliedElementProperty;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.ui.EditorPageState;

/**
 * @author <a href="mailto:gregory.amerson@liferay.com">Gregory Amerson</a>
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public interface DiagramEditorPageState extends EditorPageState
{
    ElementType TYPE = new ElementType( DiagramEditorPageState.class );    
    
    // *** ZoomLevel ***
    
    @Type( base = Integer.class )
    @DefaultValue( text = "100" )

    ValueProperty PROP_ZOOM_LEVEL = new ValueProperty( TYPE, "ZoomLevel" );
    
    Value<Integer> getZoomLevel();
    void setZoomLevel( String value );
    void setZoomLevel( Integer value );

    // *** PalettePreferences ***
    
    @Type( base = PalettePreferences.class )
    
    ImpliedElementProperty PROP_PALETTE_PREFERENCES = new ImpliedElementProperty( TYPE, "PalettePreferences" );

    PalettePreferences getPalettePreferences();    
}
