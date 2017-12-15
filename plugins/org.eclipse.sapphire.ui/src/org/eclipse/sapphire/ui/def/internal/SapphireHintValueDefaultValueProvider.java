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

package org.eclipse.sapphire.ui.def.internal;

import org.eclipse.sapphire.modeling.DefaultValueService;
import org.eclipse.sapphire.ui.def.ISapphireHint;
import org.eclipse.sapphire.ui.def.ISapphirePartDef;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SapphireHintValueDefaultValueProvider

    extends DefaultValueService
    
{
    @Override
    public String getDefaultValue()
    {
        final ISapphireHint hint = (ISapphireHint) element();
        return getDefaultValue( hint.getName().getText() );
    }
    
    public static String getDefaultValue( final String hint )
    {
        if( hint != null )
        {
            if( hint.equals( ISapphirePartDef.HINT_HIDE_IF_DISABLED ) )
            {
                return String.valueOf( Boolean.FALSE );
            }
            else if( hint.equals( ISapphirePartDef.HINT_EXPAND_VERTICALLY ) )
            {
                return String.valueOf( Boolean.FALSE );
            }
        }
        
        return null;
    }
    
}
