/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.def;

import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ReferenceValue;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Reference;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateImpl

public interface IMasterDetailsTreeNodeFactoryDef

    extends IMasterDetailsTreeNodeListEntry
    
{
    ModelElementType TYPE = new ModelElementType( IMasterDetailsTreeNodeFactoryDef.class );
    
    // *** Id ***
    
    @Label( standard = "ID" )
    @XmlBinding( path = "id" )
    
    ValueProperty PROP_ID = new ValueProperty( TYPE, "Id" );
    
    Value<String> getId();
    void setId( String id );
    
    // *** ListProperty ***
    
    @Label( standard = "list property" )
    @XmlBinding( path = "property" )
    
    ValueProperty PROP_LIST_PROPERTY = new ValueProperty( TYPE, "ListProperty" );
    
    Value<String> getListProperty();
    void setListProperty( String listProperty );
    
    // *** TypeSpecificDefinitions ***
    
    @Label( standard = "definitions" )
    @Type( base = IMasterDetailsTreeNodeFactoryEntry.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "node-template", type = IMasterDetailsTreeNodeFactoryEntry.class ) )
    
    ListProperty PROP_TYPE_SPECIFIC_DEFINITIONS = new ListProperty( TYPE, "TypeSpecificDefinitions" );
    
    ModelElementList<IMasterDetailsTreeNodeFactoryEntry> getTypeSpecificDefinitions();
    
    // *** VisibleWhenConditionClass ***
    
    @Reference( target = Class.class )
    @Label( standard = "visible when condition class" )
    @XmlBinding( path = "visible-when/condition/class" )
    
    ValueProperty PROP_VISIBLE_WHEN_CONDITION_CLASS = new ValueProperty( TYPE, "VisibleWhenConditionClass" );
    
    ReferenceValue<Class<?>> getVisibleWhenConditionClass();
    void setVisibleWhenConditionClass( String visibleWhenConditionClass );
    
    // *** VisibleWhenConditionParameter ***
    
    @Label( standard = "visible when condition parameter" )
    @XmlBinding( path = "visible-when/condition/parameter" )
    
    ValueProperty PROP_VISIBLE_WHEN_CONDITION_PARAMETER = new ValueProperty( TYPE, "VisibleWhenConditionParameter" );
    
    Value<String> getVisibleWhenConditionParameter();
    void setVisibleWhenConditionParameter( String visibleWhenConditionParameter );

}
