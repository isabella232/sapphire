/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 ******************************************************************************/

package org.eclipse.sapphire.ui.diagram.def;

import org.eclipse.sapphire.Element;
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

public interface IDiagramGridDef extends Element 
{
    ElementType TYPE = new ElementType( IDiagramGridDef.class);
    
    // *** Visible ***
    
    @Type( base = Boolean.class )
    @XmlBinding( path = "visible" )
    @DefaultValue( text = "false" )
    @Label( standard = "show grid")
    
    ValueProperty PROP_VISIBLE = new ValueProperty(TYPE, "Visible");
    
    Value<Boolean> isVisible();
    void setVisible( String value );
    void setVisible( Boolean value );
    
    // *** GridUnit ***
    
    @Type( base = Integer.class )
    @Label( standard = "grid unit" )
    @XmlBinding( path = "grid-unit" )
    @DefaultValue( text = "10" )
    
    ValueProperty PROP_GRID_UNIT = new ValueProperty( TYPE, "GridUnit" );
    
    Value<Integer> getGridUnit();
    void setGridUnit( String unit );
    void setGridUnit( Integer unit );    

    // *** VerticalGridUnit ***
    
    @Type( base = Integer.class )
    @Label( standard = "vertical grid unit" )
    @XmlBinding( path = "vertical-grid-unit" )
    @DefaultValue( text = "10" )
    
    ValueProperty PROP_VERTICAL_GRID_UNIT = new ValueProperty( TYPE, "VerticalGridUnit" );
    
    Value<Integer> getVerticalGridUnit();
    void setVerticalGridUnit( String unit );
    void setVerticalGridUnit( Integer unit );    

}
