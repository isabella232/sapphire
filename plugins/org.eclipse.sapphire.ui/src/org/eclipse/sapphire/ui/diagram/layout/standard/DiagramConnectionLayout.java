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

public interface DiagramConnectionLayout extends Element 
{
    ElementType TYPE = new ElementType( DiagramConnectionLayout.class );
    
    // *** ConnectionId ***
    
    @XmlBinding( path = "id")
    @Required

    ValueProperty PROP_CONNECTION_ID = new ValueProperty( TYPE, "ConnectionId" );

    Value<String> getConnectionId();
    void setConnectionId( String name );
    
    // *** LabelX ***
    
    @Type( base = Integer.class )
    @XmlBinding( path = "labelPosition/@x" )
    @DefaultValue( text = "-1" )
    
    ValueProperty PROP_LABEL_X = new ValueProperty( TYPE, "LabelX");
    
    Value<Integer> getLabelX();
    void setLabelX(Integer value);
    void setLabelX(String value);

    // *** LabelY ***
    
    @Type( base = Integer.class )
    @XmlBinding( path = "labelPosition/@y" )

    ValueProperty PROP_LABEL_Y = new ValueProperty( TYPE, "LabelY");
    
    Value<Integer> getLabelY();
    void setLabelY(Integer value);
    void setLabelY(String value);
    @DefaultValue( text = "-1" )
    
    // *** ConnectionBendpoints***

    @Type( base = DiagramBendPointLayout.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "bendpoint", type = DiagramBendPointLayout.class ) )
    
    ListProperty PROP_CONNECTION_BENDPOINTS = new ListProperty( TYPE, "ConnectionBendPoints" );
    
    ElementList<DiagramBendPointLayout> getConnectionBendpoints();
    	
}
