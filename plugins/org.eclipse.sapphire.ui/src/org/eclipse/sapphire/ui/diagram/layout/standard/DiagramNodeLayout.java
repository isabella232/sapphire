/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 ******************************************************************************/
package org.eclipse.sapphire.ui.diagram.layout.standard;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public interface DiagramNodeLayout extends Element
{
    ElementType TYPE = new ElementType( DiagramNodeLayout.class );
    
    // *** NodeId ***
    
    @XmlBinding( path = "id")
    @Required

    ValueProperty PROP_NODE_ID = new ValueProperty( TYPE, "NodeId" );

    Value<String> getNodeId();
    void setNodeId( String name );
    
    // *** X ***
    
    @Type( base = Integer.class )
    @XmlBinding( path = "bounds/@x" )
    @DefaultValue( text = "-1" )
    
    ValueProperty PROP_X = new ValueProperty( TYPE, "X");
    
    Value<Integer> getX();
    void setX(Integer value);
    void setX(String value);

    // *** Y ***
    
    @Type( base = Integer.class )
    @XmlBinding( path = "bounds/@y" )
    @DefaultValue( text = "-1" )

    ValueProperty PROP_Y = new ValueProperty( TYPE, "Y");
    
    Value<Integer> getY();
    void setY(Integer value);
    void setY(String value);
    
    // *** Width ***
    
    @Type( base = Integer.class )
    @XmlBinding( path = "bounds/@width" )
    @DefaultValue( text = "-1" )
    
    ValueProperty PROP_WIDTH = new ValueProperty( TYPE, "Width");
    
    Value<Integer> getWidth();
    void setWidth(Integer value);
    void setWidth(String value);
    
    // *** Height ***
    
    @Type( base = Integer.class )
    @XmlBinding( path = "bounds/@height" )
    @DefaultValue( text = "-1" )
    
    ValueProperty PROP_HEIGHT = new ValueProperty( TYPE, "Height");
    
    Value<Integer> getHeight();
    void setHeight(Integer value);
    void setHeight(String value);
            
    // *** EmbeddedConnectionsLayout ***

    @Type( base = DiagramConnectionLayout.class )
    
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "connection", type = DiagramConnectionLayout.class ) )
    
    ListProperty PROP_EMBEDDED_CONNECTIONS_LAYOUT = new ListProperty( TYPE, "EmbeddedConnectionsLayout" );
    
    ElementList<DiagramConnectionLayout> getEmbeddedConnectionsLayout();    
    
}
