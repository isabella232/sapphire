/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 *    Ling Hao - [383924]  Flexible diagram node shapes
 ******************************************************************************/

package org.eclipse.sapphire.ui.diagram.shape.def;

import org.eclipse.sapphire.ElementHandle;
import org.eclipse.sapphire.ElementProperty;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.NumericRange;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlElementBinding;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

@Label( standard = "selection presentation" )

public interface SelectionPresentation extends LinePresentation 
{
	ElementType TYPE = new ElementType( SelectionPresentation.class );
	
	// *** Color ***
    
    @DefaultValue( text = "#FFA500" ) 
    
    ValueProperty PROP_COLOR = new ValueProperty( TYPE, LinePresentation.PROP_COLOR );

	// *** Weight ***
    
    @DefaultValue( text = "1" )
    
    ValueProperty PROP_WEIGHT = new ValueProperty( TYPE, LinePresentation.PROP_WEIGHT );

	// *** Style ***
    
    @DefaultValue( text = "dash" )
    
    ValueProperty PROP_STYLE = new ValueProperty( TYPE, LinePresentation.PROP_STYLE );
    
    // *** Offset ***
    
    @Type( base = Integer.class )
    @Label( standard = "offset" )
    @DefaultValue( text = "0" )
    @NumericRange( max = "0" )
    @XmlBinding( path = "offset" )
    
    ValueProperty PROP_OFFSET = new ValueProperty( TYPE, "Offset" );
    
    Value<Integer> getOffset();
    void setOffset( String value );
    void setOffset( Integer value );        
    
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

}
