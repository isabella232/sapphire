/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.diagram.shape.def;

import org.eclipse.sapphire.ElementHandle;
import org.eclipse.sapphire.ElementProperty;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ImpliedElementProperty;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.Image;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlElementBinding;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@Label( standard = "rectangle" )
@Image( path = "RectangleDef.png" )

public interface RectangleDef extends ContainerShapeDef 
{
	ElementType TYPE = new ElementType( RectangleDef.class );
	
    // *** CornerRadius ***
    
    @Type( base = Integer.class )
    @Label( standard = "corner radius" )
    @XmlBinding( path = "corner-radius" )
    @DefaultValue( text = "0" )
    
    ValueProperty PROP_CORNER_RADIUS = new ValueProperty( TYPE, "CornerRadius" );
    
    Value<Integer> getCornerRadius();
    void setCornerRadius( String value );
    void setCornerRadius( Integer value );
	
    // *** Border ***
    
    @Type( base = Border.class )
    @XmlBinding( path = "border" )

    ImpliedElementProperty PROP_BORDER = new ImpliedElementProperty( TYPE, "Border" );
    
    Border getBorder();
    
    // *** TopBorder ***
    
    @Type( base = BorderComponent.class )
    @XmlBinding( path = "top-border" )

    ImpliedElementProperty PROP_TOP_BORDER = new ImpliedElementProperty( TYPE, "TopBorder" );
    
    BorderComponent getTopBorder();
    
    // *** BottomBorder ***
    
    @Type( base = BorderComponent.class )
    @XmlBinding( path = "bottom-border" )

    ImpliedElementProperty PROP_BOTTOM_BORDER = new ImpliedElementProperty( TYPE, "BottomBorder" );
    
    BorderComponent getBottomBorder();
    
    // *** LeftBorder ***
    
    @Type( base = BorderComponent.class )
    @XmlBinding( path = "left-border" )

    ImpliedElementProperty PROP_LEFT_BORDER = new ImpliedElementProperty( TYPE, "LeftBorder" );
    
    BorderComponent getLeftBorder();
    
    // *** RightBorder ***
    
    @Type( base = BorderComponent.class )
    @XmlBinding( path = "right-border" )

    ImpliedElementProperty PROP_RIGHT_BORDER = new ImpliedElementProperty( TYPE, "RightBorder" );
    
    BorderComponent getRightBorder();
        
    // *** Background ***
    
    @Type
    ( 
        base = BackgroundDef.class, 
        possible = 
        { 
            SolidBackgroundDef.class, 
            GradientBackgroundDef.class
        }
    )    
    @Label( standard = "background" )
    @XmlElementBinding
    ( 
    	path = "background",
        mappings = 
        {
            @XmlElementBinding.Mapping( element = "color", type = SolidBackgroundDef.class ),
            @XmlElementBinding.Mapping( element = "gradient", type = GradientBackgroundDef.class )
        }
    )
    
    ElementProperty PROP_BACKGROUND = new ElementProperty( TYPE, "Background" );
    
    ElementHandle<BackgroundDef> getBackground();
    
    // ** ScrollHorizontally ***
    
    @Type( base = Boolean.class )
    @XmlBinding( path = "scroll-horizontally" )
    @DefaultValue( text = "false" )
    @Label( standard = "scroll horizontally")
    
    ValueProperty PROP_SCROLL_HORIZONTALLY = new ValueProperty(TYPE, "ScrollHorizontally");
    
    Value<Boolean> isScrollHorizontally();
    void setScrollHorizontally( String value );
    void setScrollHorizontally( Boolean value );    
    
    // ** ScrollVertically ***
        
    @Type( base = Boolean.class )
    @XmlBinding( path = "scroll-vertically" )
    @DefaultValue( text = "false" )
    @Label( standard = "scroll vertically")
    
    ValueProperty PROP_SCROLL_VERTICALLY = new ValueProperty(TYPE, "ScrollVertically");
    
    Value<Boolean> isScrollVertically();
    void setScrollVertically( String value );
    void setScrollVertically( Boolean value );    
    
}
