/******************************************************************************
 * Copyright (c) 2015 Oracle
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

import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ImpliedElementProperty;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.modeling.annotations.CountConstraint;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@Label( standard = "container shape" )

public interface ContainerShapeDef extends ShapeDef
{
	ElementType TYPE = new ElementType( ContainerShapeDef.class );	
    
    // *** Layout ***
    
    @Type( base = SequenceLayoutDef.class )
    @Label( standard = "sequence layout" )
    @XmlBinding( path = "sequence-layout" )
    @Required

    ImpliedElementProperty PROP_LAYOUT = new ImpliedElementProperty( TYPE, "Layout" );
    
    SequenceLayoutDef getLayout();
        
    // *** Content ***
    
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
	
	@CountConstraint( min = 1 )
	@Label( standard = "content" )
	
	@XmlListBinding
	( 
	    path = "content",
	    mappings = 
	    {
	        @XmlListBinding.Mapping( element = "text", type = TextDef.class ),
	        @XmlListBinding.Mapping( element = "image", type = ImageDef.class ),
	        @XmlListBinding.Mapping( element = "validation-marker", type = ValidationMarkerDef.class ),
	        @XmlListBinding.Mapping( element = "rectangle", type = RectangleDef.class ),
	        @XmlListBinding.Mapping( element = "line", type = LineShapeDef.class ),
	        @XmlListBinding.Mapping( element = "shape-factory", type = ShapeFactoryDef.class ),
	        @XmlListBinding.Mapping( element = "spacer", type = SpacerDef.class )
	    }
	)	
	
	ListProperty PROP_CONTENT = new ListProperty( TYPE, "Content");
	
	ElementList<ShapeDef> getContent();    
	
}
