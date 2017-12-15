/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.services;

import java.util.Map;

import org.eclipse.sapphire.Event;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class ServiceEvent extends Event
{
    private final Service service;
    
    public ServiceEvent( final Service service )
    {
        if( service == null )
        {
            throw new IllegalArgumentException();
        }
        
        this.service = service;
    }
    
    public final Service service()
    {
        return this.service;
    }
    
    @Override
    public Map<String,String> fillTracingInfo( final Map<String,String> info )
    {
        super.fillTracingInfo( info );
        
        info.put( "service", service().getClass().getName() + '(' + System.identityHashCode( service() ) + ')' );
        
        return info;
    }
    
}
