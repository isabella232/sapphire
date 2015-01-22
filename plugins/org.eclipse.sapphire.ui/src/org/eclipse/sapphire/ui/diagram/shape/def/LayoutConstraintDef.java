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

package org.eclipse.sapphire.ui.diagram.shape.def;

import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.Image;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.ui.def.HorizontalAlignment;
import org.eclipse.sapphire.ui.def.MarginPresentation;
import org.eclipse.sapphire.ui.def.VerticalAlignment;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

@Label( standard = "layout constraint" )
@Image( path = "LayoutConstraintDef.png" )

public interface LayoutConstraintDef extends MarginPresentation 
{
	ElementType TYPE = new ElementType( LayoutConstraintDef.class );
	
    // *** Width ***
    
    @Type( base = Integer.class )
    @Label( standard = "width" )
    @XmlBinding( path = "width" )
    
    ValueProperty PROP_WIDTH = new ValueProperty( TYPE, "Width" );
    
    Value<Integer> getWidth();
    void setWidth( String value );
    void setWidth( Integer value );
	
    // *** Height ***
    
    @Type( base = Integer.class )
    @Label( standard = "height" )
    @XmlBinding( path = "height" )
    
    ValueProperty PROP_HEIGHT = new ValueProperty( TYPE, "Height" );
    
    Value<Integer> getHeight();
    void setHeight( String value );
    void setHeight( Integer value );
	
	// *** HorizontalAlignment ***
    
    @Type( base = HorizontalAlignment.class )
    @Label( standard = "horizontal alignment")
    @XmlBinding( path = "horizontal-alignment" )
    @DefaultValue( text = "left" )
    
    ValueProperty PROP_HORIZONTAL_ALIGNMENT = new ValueProperty( TYPE, "HorizontalAlignment" );
    
    Value<HorizontalAlignment> getHorizontalAlignment();
    void setHorizontalAlignment( String value );
    void setHorizontalAlignment( HorizontalAlignment value );
    
	// *** VerticalAlignment ***
    
    @Type( base = VerticalAlignment.class )
    @Label( standard = "vertical alignment")
    @XmlBinding( path = "vertical-alignment" )
    @DefaultValue( text = "center" )
    
    ValueProperty PROP_VERTICAL_ALIGNMENT = new ValueProperty( TYPE, "VerticalAlignment" );
    
    Value<VerticalAlignment> getVerticalAlignment();
    void setVerticalAlignment( String value );
    void setVerticalAlignment( VerticalAlignment value );
    
}
