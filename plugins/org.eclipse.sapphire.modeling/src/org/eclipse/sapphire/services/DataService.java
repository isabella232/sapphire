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

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class DataService<T> extends Service
{
    private T data;
    
    @Override
    protected final void init()
    {
        initDataService();
        refresh();
    }

    protected void initDataService()
    {
    }
    
    // TODO: Make final when override to call refresh() workaround is no longer necessary.
    
    protected T data()
    {
        return this.data;
    }
    
    protected abstract T compute();
    
    protected final void refresh()
    {
        final T newData = compute();
        
        // Due to possibility of the service being accessed in reentrant fashion during service
        // initialization it isn't safe to assume that data hasn't been seen before the initial
        // refresh call. As such, a service event must be broadcast even after the initial refresh.
        
        if( this.data == null || ! this.data.equals( newData ) )
        {
            this.data = newData;
            broadcast();
        }
    }

}
