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

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.NonNullValue;
import org.eclipse.sapphire.modeling.annotations.PossibleValues;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.ui.def.internal.SapphireHintValueDefaultValueProvider;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@Label( standard = "hint" )
@GenerateImpl

public interface ISapphireHint

    extends IModelElement
    
{
    ModelElementType TYPE = new ModelElementType( ISapphireHint.class );
    
    // *** Name ***
    
    @Label( standard = "name" )
    @NonNullValue
    @XmlBinding( path = "name" )
    
    @PossibleValues
    (
        values = 
        {
            "assist.contributors",
            "aux.text",
            "aux.text.provider",
            "border",
            "browse.only",
            "column.widths",
            ISapphirePartDef.HINT_EXPAND_VERTICALLY,
            "factory",
            ISapphirePartDef.HINT_HEIGHT,
            ISapphirePartDef.HINT_HIDE_IF_DISABLED,
            "listeners",
            "margin.left",
            "prefer.combo",
            ISapphirePartDef.HINT_PREFER_FORM_STYLE,
            "prefer.radio.buttons",
            "prefer.vertical.radio.buttons",
            "read.only",
            "show.header",
            "show.label",
            "show.label.above",
            "suppress.assist.contributors",
            ISapphirePartDef.HINT_WIDTH
        },
        invalidValueMessage = "\"{0}\" is not a valid hint."
    )
    
    ValueProperty PROP_NAME = new ValueProperty( TYPE, "Name" );
    
    Value<String> getName();
    void setName( String name );
    
    // *** Value ***
    
    @Label( standard = "value" )
    @NonNullValue
    @XmlBinding( path = "value" )
    @DefaultValue( service = SapphireHintValueDefaultValueProvider.class )
    
    ValueProperty PROP_VALUE = new ValueProperty( TYPE, "Value" );
    
    Value<String> getValue();
    void setValue( String value );
}
