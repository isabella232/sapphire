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

import org.eclipse.sapphire.ui.def.ActionHandlerFactoryDef;
import org.eclipse.sapphire.ui.def.ISapphireParam;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class SapphireActionHandlerFactoryDefMethods
{
    public static String getParam( final ActionHandlerFactoryDef def,
                                   final String name )
    {
        if( name == null )
        {
            throw new IllegalArgumentException();
        }
        
        for( ISapphireParam param : def.getParams() )
        {
            if( name.equals( param.getName().content() ) )
            {
                return param.getValue().content();
            }
        }
        
        return null;
    }
    
}
