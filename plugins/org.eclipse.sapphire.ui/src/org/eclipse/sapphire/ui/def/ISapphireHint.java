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

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DependsOn;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.PossibleValues;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.Service;
import org.eclipse.sapphire.modeling.annotations.Services;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.ui.def.internal.SapphireHintValueDefaultValueService;
import org.eclipse.sapphire.ui.def.internal.SapphireHintValuePossibleValuesService;

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
    @Required
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
            PropertyEditorDef.HINT_CHECKBOX_LAYOUT,
            "column.widths",
            "factory",
            ISapphirePartDef.HINT_HIDE_IF_DISABLED,
            "listeners",
            "prefer.combo",
            ISapphirePartDef.HINT_PREFER_FORM_STYLE,
            "prefer.radio.buttons",
            "prefer.vertical.radio.buttons",
            "read.only",
            "show.header",
            ISapphirePartDef.HINT_STYLE,
            "suppress.assist.contributors"
        },
        invalidValueMessage = "\"{0}\" is not a valid hint.",
        invalidValueSeverity = Status.Severity.OK
    )
    
    ValueProperty PROP_NAME = new ValueProperty( TYPE, "Name" );
    
    Value<String> getName();
    void setName( String name );
    
    // *** Value ***
    
    @Label( standard = "value" )
    @Required
    @XmlBinding( path = "value" )
    @DependsOn( value = "Name" )
    @Services( { @Service( impl = SapphireHintValueDefaultValueService.class ), @Service( impl = SapphireHintValuePossibleValuesService.class ) } )
    
    ValueProperty PROP_VALUE = new ValueProperty( TYPE, "Value" );
    
    Value<String> getValue();
    void setValue( String value );
}
