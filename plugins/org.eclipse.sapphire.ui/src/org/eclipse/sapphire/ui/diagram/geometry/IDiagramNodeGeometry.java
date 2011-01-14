/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.diagram.geometry;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.NonNullValue;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.CustomXmlValueBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;
import org.eclipse.sapphire.ui.diagram.geometry.internal.GeometryAttributeBinding;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

@GenerateImpl

public interface IDiagramNodeGeometry extends IModelElement
{
	ModelElementType TYPE = new ModelElementType( IDiagramNodeGeometry.class );
	
	// *** NodeId ***
	
	@XmlBinding( path = "id")
	@NonNullValue

	ValueProperty PROP_NODE_ID = new ValueProperty( TYPE, "NodeId" );

    Value<String> getNodeId();
    void setNodeId( String name );

    // *** X ***
    
    @Type( base = Integer.class )
    @CustomXmlValueBinding( impl = GeometryAttributeBinding.class, params = {"gemometry", "x"})
    
    ValueProperty PROP_X = new ValueProperty( TYPE, "X");
    
    Value<Integer> getX();
    void setX(Integer value);
	void setX(String value);

    // *** Y ***
    
    @Type( base = Integer.class )
    @CustomXmlValueBinding( impl = GeometryAttributeBinding.class, params = {"gemometry", "y"})

    ValueProperty PROP_Y = new ValueProperty( TYPE, "Y");
    
    Value<Integer> getY();
    void setY(Integer value);
	void setY(String value);
    
    // *** Width ***
    
    @Type( base = Integer.class )
    @CustomXmlValueBinding( impl = GeometryAttributeBinding.class, params = {"gemometry", "width"})
    
    ValueProperty PROP_WIDTH = new ValueProperty( TYPE, "Width");
    
    Value<Integer> getWidth();
    void setWidth(Integer value);
	void setWidth(String value);
	
    // *** Height ***
    
    @Type( base = Integer.class )
    @CustomXmlValueBinding( impl = GeometryAttributeBinding.class, params = {"gemometry", "height"})
    
    ValueProperty PROP_HEIGHT = new ValueProperty( TYPE, "Height");
    
    Value<Integer> getHeight();
    void setHeight(Integer value);
	void setHeight(String value);
	
    // *** EmbeddedConnectionGeometries ***

    @Type( base = IDiagramConnectionGeometry.class )
    
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "connection", type = IDiagramConnectionGeometry.class ) )
    
    ListProperty PROP_EMBEDDED_CONNECTION_GEOMETRIES = new ListProperty( TYPE, "EmbeddedConnectionGeometries" );
    
    ModelElementList<IDiagramConnectionGeometry> getEmbeddedConnectionGeometries();	
	
}
