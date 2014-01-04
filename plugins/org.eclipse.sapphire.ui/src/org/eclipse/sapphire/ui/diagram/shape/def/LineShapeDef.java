/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.diagram.shape.def;

import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ImpliedElementProperty;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.Image;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.ui.def.Orientation;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@Label( standard = "line" )
@Image( path = "LineShapeHorizontal.png" )

public interface LineShapeDef extends ShapeDef
{
	ElementType TYPE = new ElementType( LineShapeDef.class );
	
	// *** Orientation ***

	@Type( base = Orientation.class )
	@Label( standard = "orientation" )
	@DefaultValue( text = "horizontal" )
	@XmlBinding( path = "orientation" )
	
	ValueProperty PROP_ORIENTATION = new ValueProperty( TYPE, "Orientation" );

	Value<Orientation> getOrientation();
	void setOrientation( String value );
	void setOrientation( Orientation value );
	
	// *** Presentation ***

	@Type( base = LinePresentation.class )
	@Label( standard = "presentation" )
	@XmlBinding( path = "" )
	
	ImpliedElementProperty PROP_PRESENTATION = new ImpliedElementProperty( TYPE, "Presentation" );

	LinePresentation getPresentation();
    
}
