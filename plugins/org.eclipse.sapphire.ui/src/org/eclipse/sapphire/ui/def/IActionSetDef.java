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

import org.eclipse.sapphire.modeling.IRemovable;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.IModelElementForXml;
import org.eclipse.sapphire.modeling.xml.annotations.BooleanPropertyXmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.GenerateXmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.ListPropertyXmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.ListPropertyXmlBindingMapping;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateXmlBinding

public interface IActionSetDef

    extends IModelElementForXml, IRemovable
    
{
    ModelElementType TYPE = new ModelElementType( IActionSetDef.class );
    
    // *** SuppressDefaultActions ***
    
    @Type( base = Boolean.class )
    @Label( standard = "suppress default actions" )
    @DefaultValue( "false" )
    @BooleanPropertyXmlBinding( path = "suppress-default-actions", treatExistenceAsValue = true, valueWhenPresent = true )
    
    ValueProperty PROP_SUPPRESS_DEFAULT_ACTIONS = new ValueProperty( TYPE, "SuppressDefaultActions" );
    
    Value<Boolean> getSuppressDefaultActions();
    void setSuppressDefaultActions( String suppressDefaultActions );
    void setSuppressDefaultActions( Boolean suppressDefaultActions );
    
    // *** Overrides ***
    
    @Type( base = IActionOverride.class )
    @ListPropertyXmlBinding( mappings = { @ListPropertyXmlBindingMapping( element = "override", type = IActionOverride.class ) } )
                             
    ListProperty PROP_OVERRIDES = new ListProperty( TYPE, "Overrides" );
    
    ModelElementList<IActionOverride> getOverrides();
    
    // *** Groups ***
    
    @Type( base = IActionGroupDef.class )
    @ListPropertyXmlBinding( mappings = { @ListPropertyXmlBindingMapping( element = "action-group", type = IActionGroupDef.class ) } )
                             
    ListProperty PROP_GROUPS = new ListProperty( TYPE, "Groups" );
    
    ModelElementList<IActionGroupDef> getGroups();
    
}
