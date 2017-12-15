/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.samples.gallery;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.ClearOnDisable;
import org.eclipse.sapphire.modeling.annotations.Enablement;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateImpl

public interface ConditionalGallery extends IModelElement
{
    ModelElementType TYPE = new ModelElementType( ConditionalGallery.class );
    
    // *** ShapeType ***
    
    @Type( base = ShapeType.class )
    @Label( standard = "shape type" )
    @XmlBinding( path = "shape-type" )
    
    ValueProperty PROP_SHAPE_TYPE = new ValueProperty( TYPE, "ShapeType" );
    
    Value<ShapeType> getShapeType();
    void setShapeType( String value );
    void setShapeType( ShapeType value );
    
    // *** Radius ***
    
    @Type( base = Integer.class )
    @Label( standard = "radius" )
    @Enablement( expr = "${ ShapeType IN List( 'CIRCLE', 'PENTAGON', 'HEXAGON', 'HEPTAGON', 'OCTAGON' ) }" )
    @ClearOnDisable
    @Required
    @XmlBinding( path = "radius" )
    
    ValueProperty PROP_RADIUS = new ValueProperty( TYPE, "Radius" );
    
    Value<Integer> getRadius();
    void setRadius( String value );
    void setRadius( Integer value );
    
    // *** EdgeLength1 ***
    
    @Type( base = Integer.class )
    @Label( standard = "edge length 1" )
    @Enablement( expr = "${ ShapeType IN List( 'TRIANGLE', 'RECTANGLE', 'SQUARE' ) }" )
    @ClearOnDisable
    @Required
    @XmlBinding( path = "edge-length-1" )
    
    ValueProperty PROP_EDGE_LENGTH_1 = new ValueProperty( TYPE, "EdgeLength1" );
    
    Value<Integer> getEdgeLength1();
    void setEdgeLength1( String value );
    void setEdgeLength1( Integer value );
    
    // *** EdgeLength2 ***
    
    @Type( base = Integer.class )
    @Label( standard = "edge length 2" )
    @Enablement( expr = "${ ShapeType IN List( 'TRIANGLE', 'RECTANGLE' ) }" )
    @ClearOnDisable
    @Required
    @XmlBinding( path = "edge-length-2" )
    
    ValueProperty PROP_EDGE_LENGTH_2 = new ValueProperty( TYPE, "EdgeLength2" );
    
    Value<Integer> getEdgeLength2();
    void setEdgeLength2( String value );
    void setEdgeLength2( Integer value );
    
    // *** EdgeLength3 ***
    
    @Type( base = Integer.class )
    @Label( standard = "edge length 3" )
    @Enablement( expr = "${ ShapeType == 'TRIANGLE' }" )
    @ClearOnDisable
    @Required
    @XmlBinding( path = "edge-length-3" )
    
    ValueProperty PROP_EDGE_LENGTH_3 = new ValueProperty( TYPE, "EdgeLength3" );
    
    Value<Integer> getEdgeLength3();
    void setEdgeLength3( String value );
    void setEdgeLength3( Integer value );
    
}
