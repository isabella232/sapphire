/******************************************************************************
 * Copyright (c) 2015 Oracle
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

import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.CountConstraint;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

@Label( standard = "gradient background" )

public interface GradientBackgroundDef extends BackgroundDef 
{
	ElementType TYPE = new ElementType( GradientBackgroundDef.class );
	
	// *** GradientSegments ***
	
    @Type( base = GradientSegmentDef.class )
    @CountConstraint( max = 2 )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "gradient-segment", type = GradientSegmentDef.class ) )
                             
    ListProperty PROP_GRADIENT_SEGMENTS = new ListProperty( TYPE, "GradientSegments" );
    
    ElementList<GradientSegmentDef> getGradientSegments();
    
    // ** Vertical ***
    
    @Type( base = Boolean.class )
    @XmlBinding( path = "vertical" )
    @DefaultValue( text = "true" )
    @Label( standard = "vertical")
    
    ValueProperty PROP_VERTICAL = new ValueProperty(TYPE, "Vertical");
    
    Value<Boolean> isVertical();
    void setVertical( String value );
    void setVertical( Boolean value );    
    
}
