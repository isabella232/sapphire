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

package org.eclipse.sapphire.java;

import java.util.Set;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class JavaType
{
    public abstract String name();
    
    public abstract JavaTypeKind kind();
    
    public abstract JavaType base();
    
    public abstract Set<JavaType> interfaces();
    
    public abstract Object artifact();
    
    public final boolean isOfType( final String type )
    {
        if( type == null )
        {
            throw new IllegalArgumentException();
        }
        
        if( name().equals( type ) )
        {
            return true;
        }
        
        for( JavaType t : interfaces() )
        {
            if( t.isOfType( type ) )
            {
                return true;
            }
        }
        
        final JavaType base = base();
        
        if( base != null && base.isOfType( type ) )
        {
            return true;
        }
        
        return false;
    }
    
}
