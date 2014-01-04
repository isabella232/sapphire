/******************************************************************************
 * Copyright (c) 2014 Oracle
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
import java.util.Set;

import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.util.MapFactory;
import org.eclipse.sapphire.util.SetFactory;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ServiceProxy
{
    private final ServiceContext context;
    private final String id;
    private final Class<? extends Service> implementation;
    private final Class<? extends ServiceCondition> condition;
    private final Set<String> overrides;
    private final Map<String,String> parameters;
    private Service service;
    
    public ServiceProxy( final ServiceContext context,
                         final String id,
                         final Class<? extends Service> implementation,
                         final Class<? extends ServiceCondition> condition,
                         final Set<String> overrides,
                         final Map<String,String> parameters )
    {
        if( context == null )
        {
            throw new IllegalArgumentException();
        }
        
        if( id == null )
        {
            throw new IllegalArgumentException();
        }
        
        if( implementation == null )
        {
            throw new IllegalArgumentException();
        }
        
        this.context = context;
        this.id = id;
        this.implementation = implementation;
        this.condition = condition;
        this.overrides = ( overrides == null ? SetFactory.<String>empty() : overrides );
        this.parameters = ( parameters == null ? MapFactory.<String,String>empty() : parameters );
    }
    
    public String id()
    {
        return this.id;
    }
    
    public Class<? extends Service> type()
    {
        return this.implementation;
    }
    
    public Set<String> overrides()
    {
        return this.overrides;
    }
    
    public synchronized Service service()
    {
        if( this.service == null )
        {
            boolean applicable;
            
            if( this.condition == null )
            {
                applicable = true;
            }
            else
            {
                applicable = false;
                
                try
                {
                    final ServiceCondition c = this.condition.newInstance();
                    applicable = c.applicable( this.context );
                }
                catch( Exception e )
                {
                    Sapphire.service( LoggingService.class ).log( e );
                }
            }
            
            if( applicable )
            {
                try
                {
                    final Service instance = this.implementation.newInstance();
                    instance.init( context, this.id, this.parameters, this.overrides );
                    
                    this.service = instance;
                }
                catch( Exception e )
                {
                    Sapphire.service( LoggingService.class ).log( e );
                }
            }
        }
        
        return this.service;
    }
    
    public void dispose()
    {
        if( this.service != null )
        {
            try
            {
                this.service.dispose();
            }
            catch( Exception e )
            {
                Sapphire.service( LoggingService.class ).log( e );
            }
        }
    }

}
