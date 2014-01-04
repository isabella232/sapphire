/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.diagram.shape.def;

import org.eclipse.sapphire.ElementHandle;
import org.eclipse.sapphire.ElementProperty;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ImpliedElementProperty;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.Image;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.NoDuplicates;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlElementBinding;
import org.eclipse.sapphire.ui.def.PartDef;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@Label( standard = "case" )
@Image( path = "ShapeFactoryCaseDef.png" )

public interface ShapeFactoryCaseDef extends PartDef 
{
	ElementType TYPE = new ElementType( ShapeFactoryCaseDef.class );
	
    // *** ElementType ***
    
    @DefaultValue( text = "org.eclipse.sapphire.Element" )
    @NoDuplicates
    
    ValueProperty PROP_ELEMENT_TYPE = new ValueProperty( TYPE, PartDef.PROP_ELEMENT_TYPE );

    // *** Shape ***
    
	@Type
	(
		base = ShapeDef.class, 
		possible =
		{
			TextDef.class, 
			ImageDef.class, 
			ValidationMarkerDef.class, 
		    RectangleDef.class,
		    LineShapeDef.class,
		    ShapeFactoryDef.class,
		    SpacerDef.class
		}
	)
	
	@Label( standard = "shape" )
	
	@XmlElementBinding
	( 
	    mappings = 
	    {
	    	@XmlElementBinding.Mapping( element = "text", type = TextDef.class ),
	        @XmlElementBinding.Mapping( element = "image", type = ImageDef.class ),
	        @XmlElementBinding.Mapping( element = "validation-marker", type = ValidationMarkerDef.class ),
	        @XmlElementBinding.Mapping( element = "rectangle", type = RectangleDef.class ),
	        @XmlElementBinding.Mapping( element = "line", type = LineShapeDef.class ),
	        @XmlElementBinding.Mapping( element = "shape-factory", type = ShapeFactoryDef.class ),
	        @XmlElementBinding.Mapping( element = "spacer", type = SpacerDef.class )
	    }
	)	
	
    ElementProperty PROP_SHAPE = new ElementProperty( TYPE, "Shape" );
    
    ElementHandle<ShapeDef> getShape();
    
    // *** SelectionPresentation ***
    
    @Type( base = SelectionPresentation.class )
    @Label( standard = "selection presentation" )
    @XmlBinding( path = "selection-presentation" )

    ImpliedElementProperty PROP_SELECTION_PRESENTATION = new ImpliedElementProperty( TYPE, "SelectionPresentation" );
    
    SelectionPresentation getSelectionPresentation();

}
