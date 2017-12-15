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

package org.eclipse.sapphire.sdk.build.processor.internal.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class BaseModel
{
    private BaseModel parent = null;
    private Map<String,Object> data = new HashMap<String,Object>();
    
    public BaseModel getParent()
    {
        return this.parent;
    }
    
    public void setParent( final BaseModel parent )
    {
        this.parent = parent;
    }
    
    public Object getData( final String key )
    {
        return this.data.get( key );
    }
    
    public void setData( final String key,
                         final Object value )
    {
        this.data.put( key, value );
    }
    
    public abstract void write( IndentingPrintWriter pw );
    
    @Override
    public final String toString()
    {
        final StringWriter sw = new StringWriter();
        final IndentingPrintWriter pw = new IndentingPrintWriter( new PrintWriter( sw ) );
        
        write( pw );
        
        return sw.toString();
    }
    
}
