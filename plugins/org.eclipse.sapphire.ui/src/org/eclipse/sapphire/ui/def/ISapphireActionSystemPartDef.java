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
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
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
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public interface ISapphireActionSystemPartDef

    extends ISapphireConditionHostDef, ISapphireActionContextsHostDef
    
{
    ModelElementType TYPE = new ModelElementType( ISapphireActionSystemPartDef.class );
    
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
    
    // *** Images ***
    
    @Type( base = ISapphireActionImage.class )
    @Label( standard = "images" )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "image", type = ISapphireActionImage.class ) )
    
    ListProperty PROP_IMAGES = new ListProperty( TYPE, "Images" );
    
    ModelElementList<ISapphireActionImage> getImages();
    
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
    
    @Type( base = ISapphireActionLocationHint.class, possible = { ISapphireActionLocationHintBefore.class, ISapphireActionLocationHintAfter.class } )
    @Label( standard = "location hints" )
    @CustomXmlListBinding( impl = LocationHintsBinding.class )
    
    ListProperty PROP_LOCATION_HINTS = new ListProperty( TYPE, "LocationHints" );
    
    ModelElementList<ISapphireActionLocationHint> getLocationHints();
    
}
