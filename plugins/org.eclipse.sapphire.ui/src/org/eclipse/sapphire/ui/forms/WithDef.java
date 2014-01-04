/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.forms;

import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.Documentation;
import org.eclipse.sapphire.modeling.annotations.Enablement;
import org.eclipse.sapphire.modeling.annotations.Image;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.localization.Localizable;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@Label( standard = "with" )
@Image( path = "WithDef.png" )
@XmlBinding( path = "with" )

public interface WithDef extends PageBookDef
{
    ElementType TYPE = new ElementType( WithDef.class );

    static final String HINT_VALUE_STYLE_CHECKBOX = "checkbox";
    static final String HINT_VALUE_STYLE_RADIO_BUTTONS = "radio.buttons";
    static final String HINT_VALUE_STYLE_DROP_DOWN_LIST = "drop.down.list";
    
    // *** Path ***
    
    @Label( standard = "path" )
    @Required
    @XmlBinding( path = "path" )
    
    ValueProperty PROP_PATH = new ValueProperty( TYPE, "Path" );
    
    Value<String> getPath();
    void setPath( String value );
    
    // *** ShowLabel ***
    
    @Type( base = Boolean.class )
    @Label( standard = "show label" )
    @DefaultValue( text = "true" )
    @XmlBinding( path = "show-label" )
    
    @Documentation
    (
        content = "Indicates whether the label should be shown. The label can be hidden to reduce UI " +
                  "clutter if the with section is already adequately described by its context."
    )
    
    ValueProperty PROP_SHOW_LABEL = new ValueProperty( TYPE, "ShowLabel" );
    
    Value<Boolean> getShowLabel();
    void setShowLabel( String value );
    void setShowLabel( Boolean value );
    
    // *** Label ***
    
    @Type( base = Function.class )
    @Label( standard = "label" )
    @Enablement( expr = "${ ShowLabel }" )
    @Localizable
    @XmlBinding( path = "label" )
    
    @Documentation
    (
        content = "Overrides the label. By default, the with section will use property's label from model."
    )
    
    ValueProperty PROP_LABEL = new ValueProperty( TYPE, "Label" );
    
    Value<Function> getLabel();
    void setLabel( String value );
    void setLabel( Function value );
    
}
