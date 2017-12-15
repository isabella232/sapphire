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

import static org.eclipse.sapphire.modeling.util.MiscUtil.equal;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class DataService<T> extends Service
{
    private T data;
    private boolean initialized;
    private boolean readPriorToInit;
    
    @Override
    protected final void init()
    {
        initDataService();
        
        refresh();
        
        this.initialized = true;
    }

    protected void initDataService()
    {
    }
    
    // TODO: Make final when override to call refresh() workaround is no longer necessary.
    
    public T data()
    {
        if( ! this.initialized )
        {
            this.readPriorToInit = true;
        }
        
        return this.data;
    }
    
    protected abstract T compute();
    
    protected final void refresh()
    {
        final T newData = compute();
        
        if( ! equal( this.data, newData ) )
        {
            this.data = newData;
            
            if( this.initialized || this.readPriorToInit )
            {
                broadcast();
            }
        }
    }

}
