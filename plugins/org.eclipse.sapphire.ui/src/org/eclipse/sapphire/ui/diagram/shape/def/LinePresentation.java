/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.diagram.shape.def;

import org.eclipse.sapphire.Color;
import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.NumericRange;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.ui.LineStyle;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@Label( standard = "line presentation" )

public interface LinePresentation extends Element 
{
	ElementType TYPE = new ElementType( LinePresentation.class );
	
	// *** Color ***
    
    @Type( base = Color.class )
    @Label( standard = "color")
    @DefaultValue( text = "black" )
    @XmlBinding( path = "color")
    
    ValueProperty PROP_COLOR = new ValueProperty( TYPE, "Color" );
    
    Value<Color> getColor();
    void setColor( String value );
    void setColor( Color value );    

	// *** Weight ***
    
    @Type( base = Integer.class )
    @Label( standard = "weight" )
    @DefaultValue( text = "1" )
    @NumericRange( min = "0" )
    @XmlBinding( path = "weight" )
    
    ValueProperty PROP_WEIGHT = new ValueProperty( TYPE, "Weight" );
    
    Value<Integer> getWeight();
    void setWeight( String value );
    void setWeight( Integer value );        
		
    // *** Style ***
    
    @Type( base = LineStyle.class )
    @Label( standard = "style")
    @DefaultValue( text = "solid" )
    @XmlBinding( path = "style" )
    
    ValueProperty PROP_STYLE = new ValueProperty( TYPE, "Style" );
    
    Value<LineStyle> getStyle();
    void setStyle( String value );
    void setStyle( LineStyle value ) ;

}
