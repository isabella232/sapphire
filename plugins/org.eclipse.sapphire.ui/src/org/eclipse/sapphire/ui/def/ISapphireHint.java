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
            PropertyEditorDef.HINT_ASSIST_CONTRIBUTORS,
            PropertyEditorDef.HINT_AUX_TEXT,
            PropertyEditorDef.HINT_AUX_TEXT_PROVIDER,
            PropertyEditorDef.HINT_BORDER,
            PropertyEditorDef.HINT_BROWSE_ONLY,
            PropertyEditorDef.HINT_CHECKBOX_LAYOUT,
            PropertyEditorDef.HINT_COLUMN_WIDTHS,
            PropertyEditorDef.HINT_FACTORY,
            PartDef.HINT_HIDE_IF_DISABLED,
            PropertyEditorDef.HINT_LISTENERS,
            PropertyEditorDef.HINT_PREFER_COMBO,
            PartDef.HINT_PREFER_FORM_STYLE,
            PropertyEditorDef.HINT_PREFER_RADIO_BUTTONS,
            PropertyEditorDef.HINT_PREFER_VERTICAL_RADIO_BUTTONS,
            PropertyEditorDef.HINT_READ_ONLY,
            PropertyEditorDef.HINT_SHOW_HEADER,
            PartDef.HINT_STYLE,
            PropertyEditorDef.HINT_SUPPRESS_ASSIST_CONTRIBUTORS
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
