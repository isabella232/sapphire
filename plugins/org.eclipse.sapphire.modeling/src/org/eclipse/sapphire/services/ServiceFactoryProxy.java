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

package org.eclipse.sapphire.services;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class ServiceFactoryProxy
{
    public String id()
    {
        return String.valueOf( hashCode() );
    }
    
    public Class<? extends Service> type()
    {
        return Service.class;
    }
    
    public Set<String> overrides()
    {
        return Collections.emptySet();
    }
    
    public Map<String,String> parameters()
    {
        return Collections.emptyMap();
    }
    
    public final boolean applicable( final ServiceContext context,
                                     final Class<? extends Service> service )
    {
        boolean result = false;
    
        if( service.isAssignableFrom( type() ) )
        {
            result = applicableHandOff( context, service );
        }
    
        return result;
    }
    
    protected abstract boolean applicableHandOff( ServiceContext context,
                                                  Class<? extends Service> service );
    
    public final Service create( final ServiceContext context,
                                 final Class<? extends Service> service )
    {
        Service result = createHandOff( context, service );
    
        if( result != null )
        {
            result.init( context, parameters() );
        }
    
        return result;
    }

    protected abstract Service createHandOff( ServiceContext context,
                                              Class<? extends Service> service );

}
