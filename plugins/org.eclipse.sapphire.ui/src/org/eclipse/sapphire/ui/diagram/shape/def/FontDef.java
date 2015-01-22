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

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlValueBinding;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

@Label( standard = "font" )

public interface FontDef extends Element
{
	ElementType TYPE = new ElementType( FontDef.class );
	
	// *** Name ***

	@Label( standard = "font family" )
	@DefaultValue( text = "System" )
    @XmlBinding( path = "name" )
    
    ValueProperty PROP_NAME = new ValueProperty( TYPE, "Name" );
    
    Value<String> getName();
    void setName( String name );
	
	// *** Size ***
	
    @Type( base = Integer.class )
    @Label( standard = "font size" )
    @DefaultValue( text = "9" )
    @XmlBinding( path = "size" )
    
    ValueProperty PROP_SIZE = new ValueProperty( TYPE, "Size" );
    
    Value<Integer> getSize();
    void setSize( String value );
    void setSize( Integer value );
		
	// *** Bold ***
    
    @Type( base = Boolean.class )
    @XmlValueBinding( path = "bold", mapExistanceToValue = "true" )
    @DefaultValue( text = "false" )
    @Label( standard = "bold")
    
    ValueProperty PROP_BOLD = new ValueProperty(TYPE, "Bold");
    
    Value<Boolean> isBold();
    void setBold( String value );
    void setBold( Boolean value );    
    	
	// *** Italic ***
    
    @Type( base = Boolean.class )
    @XmlValueBinding( path = "italic", mapExistanceToValue = "true" )
    @DefaultValue( text = "false" )
    @Label( standard = "italic")
    
    ValueProperty PROP_ITALIC = new ValueProperty(TYPE, "Italic");
    
    Value<Boolean> isItalic();
    void setItalic( String value );
    void setItalic( Boolean value );    
    
}
