/******************************************************************************
 * Copyright (c) 2014 Oracle and Liferay
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Gregory Amerson - [374622] Add ability to specify action tooltips
 ******************************************************************************/

package org.eclipse.sapphire.ui.def;

import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.LongString;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.annotations.Whitespace;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.localization.Localizable;
import org.eclipse.sapphire.modeling.xml.annotations.CustomXmlListBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlValueBinding;
import org.eclipse.sapphire.ui.def.internal.LocationHintsBinding;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 * @author <a href="mailto:gregory.amerson@liferay.com">Gregory Amerson</a>
 */

public interface ActionSystemPartDef extends ISapphireConditionHostDef, ActionContextsHostDef
{
    ElementType TYPE = new ElementType( ActionSystemPartDef.class );
    
    // *** Id ***
    
    @Label( standard = "id" )
    @XmlBinding( path = "id" )
    
    ValueProperty PROP_ID = new ValueProperty( TYPE, "Id" );
    
    Value<String> getId();
    void setId( String value );
    
    // *** Label ***
    
    @Type( base = Function.class )
    @Localizable
    @Label( standard = "label" )
    @XmlBinding( path = "label" )
    
    ValueProperty PROP_LABEL = new ValueProperty( TYPE, "Label" );
    
    Value<Function> getLabel();
    void setLabel( String value );
    void setLabel( Function value );
    
    // *** ToolTip ***
    
    @Type( base = Function.class )
    @Localizable
    @Label( standard = "tool tip" )
    @DefaultValue( text = "${ Label }" )
    @XmlBinding( path = "tooltip" )
    
    ValueProperty PROP_TOOL_TIP = new ValueProperty( TYPE, "ToolTip" );

    Value<Function> getToolTip();
    void setToolTip( String value );
    void setToolTip( Function value );

    // *** Images ***
    
    @Type( base = ImageReference.class )
    @Label( standard = "images" )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "image", type = ImageReference.class ) )
    
    ListProperty PROP_IMAGES = new ListProperty( TYPE, "Images" );
    
    ElementList<ImageReference> getImages();
    
    // *** Description ***
    
    @LongString
    @Localizable
    @Label( standard = "description" )
    @Whitespace( collapse = true )
    @XmlValueBinding( path = "description" )
    
    ValueProperty PROP_DESCRIPTION = new ValueProperty( TYPE, "Description" );
    
    Value<String> getDescription();
    void setDescription( String value );
    
    // *** LocationHints ***
    
    @Type( base = ActionLocationHint.class, possible = { ActionLocationHintBefore.class, ActionLocationHintAfter.class } )
    @Label( standard = "location hints" )
    @CustomXmlListBinding( impl = LocationHintsBinding.class )
    
    ListProperty PROP_LOCATION_HINTS = new ListProperty( TYPE, "LocationHints" );
    
    ElementList<ActionLocationHint> getLocationHints();
    
}
