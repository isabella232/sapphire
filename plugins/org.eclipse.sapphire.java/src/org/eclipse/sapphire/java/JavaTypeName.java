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

package org.eclipse.sapphire.java;

import org.eclipse.sapphire.modeling.annotations.Label;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@Label( standard = "Java type name" )

public final class JavaTypeName extends JavaQualifiedName
{
    private final String pkg;
    private final String full;
    private final String simple;
    
    public JavaTypeName( final String name )
    {
        super( name );
        
        final int lastDot = name.lastIndexOf( '.' );
        
        if( lastDot == -1 )
        {
            this.pkg = null;
            this.full = name;
        }
        else
        {
            this.pkg = name.substring( 0, lastDot );
            this.full = name.substring( lastDot + 1 );
        }
        
        final int lastDollar = this.full.lastIndexOf( '$' );
        
        if( lastDollar == -1 )
        {
            this.simple = this.full;
        }
        else
        {
            this.simple = this.full.substring( lastDollar + 1 );
        }
    }
    
    public String pkg()
    {
        return this.pkg;
    }
    
    public String simple()
    {
        return this.simple;
    }
    
    public String full()
    {
        return this.full;
    }
    
    public String qualified()
    {
        return toString();
    }
    
}
