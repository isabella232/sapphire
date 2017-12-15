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

import org.eclipse.sapphire.ui.def.ISapphireActionHandlerFactoryDef;
import org.eclipse.sapphire.ui.def.ISapphireParam;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SapphireActionHandlerFactoryDefMethods
{
    public static String getParam( final ISapphireActionHandlerFactoryDef def,
                                   final String name )
    {
        if( name == null )
        {
            throw new IllegalArgumentException();
        }
        
        for( ISapphireParam param : def.getParams() )
        {
            if( name.equals( param.getName().getContent() ) )
            {
                return param.getValue().getContent();
            }
        }
        
        return null;
    }
    
}
