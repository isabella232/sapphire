/******************************************************************************
 * Copyright (c) 2011 Oracle and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Greg Amerson - [342771] Support "image+label" hint for when actions are presented in a toolbar
 ******************************************************************************/

package org.eclipse.sapphire.ui.def.internal;

import java.util.SortedSet;

import org.eclipse.sapphire.modeling.IModelParticle;
import org.eclipse.sapphire.modeling.PossibleValuesService;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.ui.def.ISapphireActionDef;
import org.eclipse.sapphire.ui.def.ISapphireHint;
import org.eclipse.sapphire.ui.def.ISapphirePartDef;
import org.eclipse.sapphire.ui.def.ISapphirePropertyEditorDef;
import org.eclipse.sapphire.ui.def.ISapphireWithDirectiveDef;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SapphireHintValuePossibleValuesService

    extends PossibleValuesService
    
{
    @Override
    protected void fillPossibleValues( final SortedSet<String> values )
    {
        final ISapphireHint element = (ISapphireHint) element();
        final IModelParticle partdef = element.parent().parent();
        final String hint = element.getName().getText();
        
        if( hint != null )
        {
            if( hint.equals( ISapphirePropertyEditorDef.HINT_CHECKBOX_LAYOUT ) )
            {
                values.add( ISapphirePropertyEditorDef.HINT_VALUE_CHECKBOX_LAYOUT_LEADING_LABEL );
                values.add( ISapphirePropertyEditorDef.HINT_VALUE_CHECKBOX_LAYOUT_TRAILING_LABEL );
                values.add( ISapphirePropertyEditorDef.HINT_VALUE_CHECKBOX_LAYOUT_TRAILING_LABEL_INDENTED );
            }
            else if ( hint.equals( ISapphirePartDef.HINT_STYLE ) ) 
            {
                if( partdef instanceof ISapphireWithDirectiveDef )
                {
                    values.add( ISapphireWithDirectiveDef.HINT_VALUE_STYLE_CHECKBOX );
                    values.add( ISapphireWithDirectiveDef.HINT_VALUE_STYLE_RADIO_BUTTONS );
                    values.add( ISapphireWithDirectiveDef.HINT_VALUE_STYLE_DROP_DOWN_LIST );
                }
                else if( partdef instanceof ISapphireActionDef )
                {
                    values.add( ISapphireActionDef.HINT_VALUE_STYLE_IMAGE );
                    values.add( ISapphireActionDef.HINT_VALUE_STYLE_IMAGE_TEXT );
                    values.add( ISapphireActionDef.HINT_VALUE_STYLE_TEXT );
                }
            }
        }
    }
    
    @Override
    public Status.Severity getInvalidValueSeverity( final String invalidValue )
    {
        return Status.Severity.OK;
    }
    
}
