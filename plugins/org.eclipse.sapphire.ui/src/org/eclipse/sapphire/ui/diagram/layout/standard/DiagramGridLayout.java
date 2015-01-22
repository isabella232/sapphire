/******************************************************************************
 * Copyright (c) 2015 Oracle and Liferay
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 *    Gregory Amerson - [377388] IDiagram{Guides/Grids}Def visible property does not affect StandardDiagramLayout persistence
 ******************************************************************************/

package org.eclipse.sapphire.ui.diagram.layout.standard;

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
 * @author <a href="mailto:gregory.amerson@liferay.com">Gregory Amerson</a>
 */

public interface DiagramGridLayout extends Element 
{
	ElementType TYPE = new ElementType( DiagramGridLayout.class);

	// *** Visible ***
	
	@Type( base = Boolean.class )
	@XmlBinding( path = "visible" )
	@Label( standard = "show grid")
	
	ValueProperty PROP_VISIBLE = new ValueProperty(TYPE, "Visible");
	
	Value<Boolean> isVisible();
	void setVisible( String value );
	void setVisible( Boolean value );
	
	// *** GridUnit ***
	
	@Type( base = Integer.class )
	@Label( standard = "grid unit" )
	@XmlBinding( path = "grid-unit" )
	@DefaultValue( text = "-1" )
	
	ValueProperty PROP_GRID_UNIT = new ValueProperty( TYPE, "GridUnit" );
	
	Value<Integer> getGridUnit();
	void setGridUnit( String unit );
	void setGridUnit( Integer unit );    
	
	// *** VerticalGridUnit ***
	
	@Type( base = Integer.class )
	@Label( standard = "vertical grid unit" )
	@XmlBinding( path = "vertical-grid-unit" )
	@DefaultValue( text = "-1" )
	
	ValueProperty PROP_VERTICAL_GRID_UNIT = new ValueProperty( TYPE, "VerticalGridUnit" );
	
	Value<Integer> getVerticalGridUnit();
	void setVerticalGridUnit( String unit );
	void setVerticalGridUnit( Integer unit );    

}

