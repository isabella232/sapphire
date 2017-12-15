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

package org.eclipse.sapphire.ui.form.editors.masterdetails.state;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateImpl

public interface ContentOutlineNodeState extends IModelElement
{
    ModelElementType TYPE = new ModelElementType( ContentOutlineNodeState.class );
    
    // *** Label ***
    
    @XmlBinding( path = "label" )
    
    ValueProperty PROP_LABEL = new ValueProperty( TYPE, "Label" );
    
    Value<String> getLabel();
    void setLabel( String value );
    
    // *** Expanded ***
    
    @Type( base = Boolean.class )
    @XmlBinding( path = "expanded" )
    @DefaultValue( text = "false" )
    
    ValueProperty PROP_EXPANDED = new ValueProperty( TYPE, "Expanded" );
    
    Value<Boolean> getExpanded();
    void setExpanded( String value );
    void setExpanded( Boolean value );
    
    // *** Selected ***
    
    @Type( base = Boolean.class )
    @XmlBinding( path = "selected" )
    @DefaultValue( text = "false" )
    
    ValueProperty PROP_SELECTED = new ValueProperty( TYPE, "Selected" );
    
    Value<Boolean> getSelected();
    void setSelected( String value );
    void setSelected( Boolean value );
    
    // *** Children ***
    
    @Type( base = ContentOutlineNodeState.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "node", type = ContentOutlineNodeState.class ) )
    
    ListProperty PROP_CHILDREN = new ListProperty( TYPE, "Children" );
    
    ModelElementList<ContentOutlineNodeState> getChildren();
    
}
