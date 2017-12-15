/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.def;

import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.localization.Localizable;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@Label( standard = "with" )
@GenerateImpl

public interface ISapphireWithDirectiveDef

    extends PageBookDef
    
{
    ModelElementType TYPE = new ModelElementType( ISapphireWithDirectiveDef.class );

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
    
    // *** Label ***
    
    @Label( standard = "label" )
    @Localizable
    @XmlBinding( path = "label" )
    
    ValueProperty PROP_LABEL = new ValueProperty( TYPE, "Label" );
    
    Value<String> getLabel();
    void setLabel( String value );
    
}
