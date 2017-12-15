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

package org.eclipse.sapphire.ui.def.internal;

import org.eclipse.sapphire.services.DefaultValueService;
import org.eclipse.sapphire.services.DefaultValueServiceData;
import org.eclipse.sapphire.ui.def.ISapphireHint;
import org.eclipse.sapphire.ui.def.ISapphirePartDef;
import org.eclipse.sapphire.ui.def.PropertyEditorDef;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SapphireHintValueDefaultValueService extends DefaultValueService
{
    @Override
    protected DefaultValueServiceData data()
    {
        refresh();
        return super.data();
    }

    @Override
    protected DefaultValueServiceData compute()
    {
        return new DefaultValueServiceData( getDefaultValue( context( ISapphireHint.class ).getName().getText() ) );
    }
    
    public static String getDefaultValue( final String hint )
    {
        if( hint != null )
        {
            if( hint.equals( ISapphirePartDef.HINT_HIDE_IF_DISABLED ) )
            {
                return String.valueOf( Boolean.FALSE );
            }
            else if( hint.equals( PropertyEditorDef.HINT_CHECKBOX_LAYOUT ) )
            {
                return PropertyEditorDef.HINT_VALUE_CHECKBOX_LAYOUT_TRAILING_LABEL;
            }
        }
        
        return null;
    }
    
}
