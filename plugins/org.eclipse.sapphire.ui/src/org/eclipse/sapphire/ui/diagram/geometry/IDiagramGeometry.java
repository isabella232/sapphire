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
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlRootBinding;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

@GenerateImpl
@XmlRootBinding( elementName = "diagram-geometry" )

public interface IDiagramGeometry extends IModelElement 
{
	ModelElementType TYPE = new ModelElementType( IDiagramGeometry.class );
	
    // *** DiagramNodeGeometries ***

    @Type( base = IDiagramNodeGeometry.class )
    
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "node", type = IDiagramNodeGeometry.class ) )
    
    ListProperty PROP_DIAGRAM_NODE_GEOMETRIES = new ListProperty( TYPE, "DiagramNodeGeometries" );
    
    ModelElementList<IDiagramNodeGeometry> getDiagramNodeGeometries();
	
    // *** DiagramConnectionGeometries ***

    @Type( base = IDiagramConnectionGeometry.class )
    
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "connection", type = IDiagramConnectionGeometry.class ) )
    
    ListProperty PROP_DIAGRAM_CONNECTION_GEOMETRIES = new ListProperty( TYPE, "DiagramConnectionGeometries" );
    
    ModelElementList<IDiagramConnectionGeometry> getDiagramConnectionGeometries();

}
