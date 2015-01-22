/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 *    Ling Hao - [383924] Extend Sapphire Diagram Framework to support SQL Schema diagram like editors
 ******************************************************************************/

package org.eclipse.sapphire.ui.diagram.shape.def;

import org.eclipse.sapphire.Color;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ImpliedElementProperty;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.Image;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

@Label( standard = "text" )
@Image( path = "TextDef.png" )

public interface TextDef extends ShapeDef 
{
	ElementType TYPE = new ElementType( TextDef.class );
	
	// *** Content ***
	
    @Type( base = Function.class )
    @Label( standard = "content" )
    @Required
    @XmlBinding( path = "content" )
    
    ValueProperty PROP_CONTENT = new ValueProperty( TYPE, "Content" );
    
    Value<Function> getContent();
    void setContent( String value );
    void setContent( Function value );
    
	// *** Color ***
    
    @Type( base = Color.class )
    @Label( standard = "color")
    @DefaultValue( text = "black" )
    @XmlBinding( path = "color")
    
    ValueProperty PROP_COLOR = new ValueProperty( TYPE, "Color" );
    
    Value<Color> getColor();
    void setColor( String value );
    void setColor( Color value );
    
    // *** Font ***
    
    @Type( base = FontDef.class )
    @Label( standard = "font" )
    @XmlBinding( path = "font" )
    
    ImpliedElementProperty PROP_FONT = new ImpliedElementProperty( TYPE, "Font" );
    
    FontDef getFont();
	
    // *** Truncatable ***
    
    @Type( base = Boolean.class )
    @XmlBinding( path = "truncatable" )
    @DefaultValue( text = "true" )
    @Label( standard = "truncatable")
    
    ValueProperty PROP_TRUNCATABLE = new ValueProperty(TYPE, "Truncatable");
    
    Value<Boolean> isTruncatable();
    void setTruncatable( String value );
    void setTruncatable( Boolean value );
    
}
