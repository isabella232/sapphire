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
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

@Label( standard = "sequence layout constraint" )

public interface SequenceLayoutConstraintDef extends LayoutConstraintDef 
{
	ElementType TYPE = new ElementType( SequenceLayoutConstraintDef.class );
	
    // *** MinWidth ***
    
    @Type( base = Integer.class )
    @Label( standard = "minimum width" )
    @XmlBinding( path = "min-width" )
    
    ValueProperty PROP_MIN_WIDTH = new ValueProperty( TYPE, "MinWidth" );
    
    Value<Integer> getMinWidth();
    void setMinWidth( String value );
    void setMinWidth( Integer value );
	
    // *** MinHeight ***
    
    @Type( base = Integer.class )
    @Label( standard = "minimum height" )
    @XmlBinding( path = "min-height" )
    
    ValueProperty PROP_MIN_HEIGHT = new ValueProperty( TYPE, "MinHeight" );
    
    Value<Integer> getMinHeight();
    void setMinHeight( String value );
    void setMinHeight( Integer value );
	
    // *** MaxWidth ***
    
    @Type( base = Integer.class )
    @Label( standard = "maximum width" )
    @XmlBinding( path = "max-width" )
    
    ValueProperty PROP_MAX_WIDTH = new ValueProperty( TYPE, "MaxWidth" );
    
    Value<Integer> getMaxWidth();
    void setMaxWidth( String value );
    void setMaxWidth( Integer value );
	
    // *** MaxHeight ***
    
    @Type( base = Integer.class )
    @Label( standard = "maximum height" )
    @XmlBinding( path = "max-height" )
    
    ValueProperty PROP_MAX_HEIGHT = new ValueProperty( TYPE, "MaxHeight" );
    
    Value<Integer> getMaxHeight();
    void setMaxHeight( String value );
    void setMaxHeight( Integer value );
    
    // *** Expand ***
    
    @Type( base = Boolean.class )
    @XmlBinding( path = "expand" )
    @DefaultValue( text = "false" )
    @Label( standard = "expand")
    
    ValueProperty PROP_EXPAND = new ValueProperty(TYPE, "Expand");
    
    Value<Boolean> isExpand();
    void setExpand( String value );
    void setExpand( Boolean value );

    // *** ExpandHorizontally ***
    
    @Type( base = Boolean.class )
    @XmlBinding( path = "expand-horizontally" )
    @DefaultValue( text = "${ Expand }" )
    @Label( standard = "expand horizontally")
    
    ValueProperty PROP_EXPAND_HORIZONTALLY = new ValueProperty(TYPE, "ExpandHorizontally");
    
    Value<Boolean> isExpandHorizontally();
    void setExpandHorizontally( String value );
    void setExpandHorizontally( Boolean value );
    
    // *** ExpandVertically ***
    
    @Type( base = Boolean.class )
    @XmlBinding( path = "expand-vertically" )
    @DefaultValue( text = "${ Expand }" )
    @Label( standard = "expand vertically")
    
    ValueProperty PROP_EXPAND_VERTICALLY = new ValueProperty(TYPE, "ExpandVertically");
    
    Value<Boolean> isExpandVertically();
    void setExpandVertically( String value );
    void setExpandVertically( Boolean value );
        
}
