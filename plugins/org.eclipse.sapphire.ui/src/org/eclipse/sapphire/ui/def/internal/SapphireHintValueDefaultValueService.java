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

package org.eclipse.sapphire.ui.def.internal;

import org.eclipse.sapphire.DefaultValueService;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.PropertyContentEvent;
import org.eclipse.sapphire.ui.def.ISapphireHint;
import org.eclipse.sapphire.ui.forms.PropertyEditorDef;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class SapphireHintValueDefaultValueService extends DefaultValueService
{
    @Override
    protected void initDefaultValueService()
    {
        context( ISapphireHint.class ).getName().attach
        (
            new FilteredListener<PropertyContentEvent>()
            {
                @Override
                protected void handleTypedEvent( final PropertyContentEvent event )
                {
                    refresh();
                }
            }
        );
    }

    @Override
    protected String compute()
    {
        return getDefaultValue( context( ISapphireHint.class ).getName().text() );
    }
    
    public static String getDefaultValue( final String hint )
    {
        if( hint != null )
        {
            if( hint.equals( PropertyEditorDef.HINT_CHECKBOX_LAYOUT ) )
            {
                return PropertyEditorDef.HINT_VALUE_CHECKBOX_LAYOUT_TRAILING_LABEL;
            }
        }
        
        return null;
    }
    
}
