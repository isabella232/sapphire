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

package org.eclipse.sapphire.modeling.scripting;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ScriptsManager
{
    private static final Map<String,SoftReference<Script>> cache = new HashMap<String,SoftReference<Script>>();
    
    public static Script loadScript( final String script )
    {
        SoftReference<Script> ref = cache.get( script );
        Script s = null;
        
        if( ref != null )
        {
            s = ref.get();
        }
        
        if( s == null )
        {
            s = new Script( script );
            cache.put( script, new SoftReference<Script>( s ) );
        }
        
        return s;
    }
}
