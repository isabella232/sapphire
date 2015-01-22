/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.def;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Documentation;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.LongString;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.Whitespace;
import org.eclipse.sapphire.modeling.xml.annotations.XmlValueBinding;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@Documentation
(
    content = "A presentation style is an abstraction that gives the user interface author some " +
              "influence over how a part is presented without the part type hardcoding the alternatives. " + 
              "Providers of concrete presentations associate their rendering logic with registered " +
              "presentation styles."
)

public interface PresentationStyleDef extends Element
{
    ElementType TYPE = new ElementType( PresentationStyleDef.class );
    
    // *** Id ***

    @Label( standard = "ID" )
    @Required
    @XmlValueBinding( path = "id" )
    
    @Documentation
    (
        content = "The identifier of the presentation style. Must be unique within the scope of all " +
                  "presentation styles associated with a given part type."
    )
    
    ValueProperty PROP_ID = new ValueProperty( TYPE, "Id" );

    Value<String> getId();
    void setId( String value );
    
    // *** PartType ***

    @Label( standard = "part type" )
    @Required
    @XmlValueBinding( path = "part-type" )
    
    @Documentation
    (
        content = "The part type that this presentation style should be associated with."
    )
    
    ValueProperty PROP_PART_TYPE = new ValueProperty( TYPE, "PartType" );

    Value<String> getPartType();
    void setPartType( String value );
    
    // *** Description ***
    
    @LongString
    @Label( standard = "description" )
    @Whitespace( collapse = true )
    @XmlValueBinding( path = "description" )
    
    @Documentation
    (
        content = "The detailed information about the presentation style. The " +
                  "description should be in the form of properly capitalized and punctuated sentences."
    )
    
    ValueProperty PROP_DESCRIPTION = new ValueProperty( TYPE, "Description" );
    
    Value<String> getDescription();
    void setDescription( String value );
    
}
