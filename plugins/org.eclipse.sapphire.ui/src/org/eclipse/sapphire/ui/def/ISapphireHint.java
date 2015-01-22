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
import org.eclipse.sapphire.PossibleValues;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.Service;
import org.eclipse.sapphire.modeling.annotations.Services;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.ui.def.internal.SapphireHintValueDefaultValueService;
import org.eclipse.sapphire.ui.def.internal.SapphireHintValuePossibleValuesService;
import org.eclipse.sapphire.ui.forms.PropertyEditorDef;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@Label( standard = "hint" )

public interface ISapphireHint extends Element
{
    ElementType TYPE = new ElementType( ISapphireHint.class );
    
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
            PropertyEditorDef.HINT_LISTENERS,
            PropertyEditorDef.HINT_READ_ONLY,
            PropertyEditorDef.HINT_SHOW_HEADER,
            PartDef.HINT_STYLE,
            PropertyEditorDef.HINT_SUPPRESS_ASSIST_CONTRIBUTORS
        },
        invalidValueMessage = "\"${Name}\" is not a valid hint.",
        invalidValueSeverity = Status.Severity.OK
    )
    
    ValueProperty PROP_NAME = new ValueProperty( TYPE, "Name" );
    
    Value<String> getName();
    void setName( String name );
    
    // *** Value ***
    
    @Label( standard = "value" )
    @Required
    @XmlBinding( path = "value" )
    @Services( { @Service( impl = SapphireHintValueDefaultValueService.class ), @Service( impl = SapphireHintValuePossibleValuesService.class ) } )
    
    ValueProperty PROP_VALUE = new ValueProperty( TYPE, "Value" );
    
    Value<String> getValue();
    void setValue( String value );
}
