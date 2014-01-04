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

import java.util.Collection;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class JavaTypeConstraintServiceData
{
    private final SortedSet<JavaTypeKind> kinds;
    private final SortedSet<String> types;
    private final JavaTypeConstraintBehavior behavior;
    
    public JavaTypeConstraintServiceData( final Collection<JavaTypeKind> kinds,
                                          final Collection<String> types,
                                          final JavaTypeConstraintBehavior behavior )
    {
        final SortedSet<JavaTypeKind> kindsCopy = new TreeSet<JavaTypeKind>();
        
        for( JavaTypeKind kind : kinds )
        {
            if( kind != null )
            {
                kindsCopy.add( kind );
            }
        }

        this.kinds = Collections.unmodifiableSortedSet( kindsCopy );
        
        final SortedSet<String> typesCopy = new TreeSet<String>();
        
        for( String type : types )
        {
            if( type != null )
            {
                type = type.trim();
                
                if( type.length() > 0 )
                {
                    typesCopy.add( type );
                }
            }
        }

        this.types = Collections.unmodifiableSortedSet( typesCopy );
        
        this.behavior = ( behavior == null ? JavaTypeConstraintBehavior.ALL : behavior );
    }
    
    public JavaTypeConstraintServiceData( final Collection<JavaTypeKind> kinds,
                                          final Collection<String> types )
    {
        this( kinds, types, JavaTypeConstraintBehavior.ALL );
    }
    
    public SortedSet<JavaTypeKind> kinds()
    {
        return this.kinds;
    }
    
    public SortedSet<String> types()
    {
        return this.types;
    }
    
    public JavaTypeConstraintBehavior behavior()
    {
        return this.behavior;
    }
    
    @Override
    public boolean equals( final Object obj )
    {
        if( obj instanceof JavaTypeConstraintServiceData )
        {
            final JavaTypeConstraintServiceData data = (JavaTypeConstraintServiceData) obj;
            return this.kinds.equals( data.kinds ) && this.types.equals( data.types ) && ( this.behavior == data.behavior );
        }
        
        return false;
    }

    @Override
    public int hashCode()
    {
        return this.kinds.hashCode() ^ this.types.hashCode() ^ this.behavior.hashCode();
    }
    
}
