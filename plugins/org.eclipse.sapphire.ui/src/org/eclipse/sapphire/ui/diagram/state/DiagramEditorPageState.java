/******************************************************************************
 * Copyright (c) 2013 Liferay and Oracle
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

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ImpliedElementProperty;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

/**
 * @author <a href="mailto:gregory.amerson@liferay.com">Gregory Amerson</a>
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@XmlBinding( path = "sapphire-diagram-editor-page-state" )
@GenerateImpl

public interface DiagramEditorPageState extends IModelElement
{
    ModelElementType TYPE = new ModelElementType( DiagramEditorPageState.class );    
    
    // *** ZoomLevel ***
    
    @Type( base = Integer.class )
    @DefaultValue( text = "100" )
    @XmlBinding( path = "zoom-level" )

    ValueProperty PROP_ZOOM_LEVEL = new ValueProperty( TYPE, "ZoomLevel" );
    
    Value<Integer> getZoomLevel();
    void setZoomLevel( String value );
    void setZoomLevel( Integer value );

    // *** PalettePreferences ***
    
    @Type( base = PalettePreferences.class )
    @XmlBinding( path = "palette-preferences")
    
    ImpliedElementProperty PROP_PALETTE_PREFERENCES = new ImpliedElementProperty( TYPE, "PalettePreferences" );

    PalettePreferences getPalettePreferences();    
}
