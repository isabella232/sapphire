/******************************************************************************
 * Copyright (c) 2014 Oracle and Liferay
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Gregory Amerson - [342771] Support "image+label" hint for when actions are presented in a toolbar
 ******************************************************************************/

package org.eclipse.sapphire.ui.def.internal;

import java.util.Set;

import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.PropertyContentEvent;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.services.PossibleValuesService;
import org.eclipse.sapphire.ui.def.ActionDef;
import org.eclipse.sapphire.ui.def.ISapphireHint;
import org.eclipse.sapphire.ui.def.PartDef;
import org.eclipse.sapphire.ui.forms.PropertyEditorDef;
import org.eclipse.sapphire.ui.forms.WithDef;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 * @author <a href="mailto:gregory.amerson@liferay.com">Gregory Amerson</a>
 */

public final class SapphireHintValuePossibleValuesService extends PossibleValuesService
{
    @Override
    protected void init()
    {
        context( ISapphireHint.class ).getName().attach
        (
            new FilteredListener<PropertyContentEvent>()
            {
                @Override
                protected void handleTypedEvent( final PropertyContentEvent event )
                {
                    broadcast();
                }
            }
        );
    }

    @Override
    protected void fillPossibleValues( final Set<String> values )
    {
        final ISapphireHint element = context( ISapphireHint.class );
        final PartDef partdef = element.nearest( PartDef.class );
        final String hint = element.getName().text();
        
        if( hint != null )
        {
            if( hint.equals( PropertyEditorDef.HINT_CHECKBOX_LAYOUT ) )
            {
                values.add( PropertyEditorDef.HINT_VALUE_CHECKBOX_LAYOUT_LEADING_LABEL );
                values.add( PropertyEditorDef.HINT_VALUE_CHECKBOX_LAYOUT_TRAILING_LABEL );
                values.add( PropertyEditorDef.HINT_VALUE_CHECKBOX_LAYOUT_TRAILING_LABEL_INDENTED );
            }
            else if( hint.equals( PartDef.HINT_STYLE ) ) 
            {
                if( partdef instanceof WithDef )
                {
                    values.add( WithDef.HINT_VALUE_STYLE_CHECKBOX );
                    values.add( WithDef.HINT_VALUE_STYLE_RADIO_BUTTONS );
                    values.add( WithDef.HINT_VALUE_STYLE_DROP_DOWN_LIST );
                }
                else if( partdef instanceof ActionDef )
                {
                    values.add( ActionDef.HINT_VALUE_STYLE_IMAGE );
                    values.add( ActionDef.HINT_VALUE_STYLE_IMAGE_TEXT );
                    values.add( ActionDef.HINT_VALUE_STYLE_TEXT );
                }
            }
            else if( hint.equals( PropertyEditorDef.HINT_SHOW_HEADER ) || 
                     hint.equals( PropertyEditorDef.HINT_BROWSE_ONLY ) ||
                     hint.equals( PropertyEditorDef.HINT_READ_ONLY ) ||
                     hint.equals( PropertyEditorDef.HINT_BORDER ) ||
                     hint.equals( PropertyEditorDef.HINT_FACTORY ) )
            {
                values.add( Boolean.TRUE.toString() );
                values.add( Boolean.FALSE.toString() );
            }
        }
    }
    
    @Override
    public Status.Severity getInvalidValueSeverity( final String invalidValue )
    {
        return Status.Severity.OK;
    }
    
}
