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

package org.eclipse.sapphire.sdk.build.processor.internal.util;

import java.util.Set;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ArrayTypeReference

    extends TypeReference
    
{
    private static final String ARRAY_NOTATION = "[]";
    private static final String[] COMMON_ARRAY_DIMENSIONS;
    
    static
    {
        COMMON_ARRAY_DIMENSIONS = new String[ 5 ];
        COMMON_ARRAY_DIMENSIONS[ 0 ] = "";
        
        for( int i = 1; i < COMMON_ARRAY_DIMENSIONS.length; i++ )
        {
            COMMON_ARRAY_DIMENSIONS[ i ] = COMMON_ARRAY_DIMENSIONS[ i - 1 ] + ARRAY_NOTATION;
        }
    }
    
    private final TypeReference base;
    private final int dimensions;
    
    public ArrayTypeReference( final TypeReference base,
                               final int dimensions )
    {
        super( base.getQualifiedName() + generateArrayNotation( dimensions ) );
        
        if( base instanceof ArrayTypeReference )
        {
            throw new IllegalArgumentException();
        }
        
        this.base = base;
        this.dimensions = dimensions; 
    }
    
    @Override
    public TypeReference getBase()
    {
        return this.base;
    }

    public int getDimensions()
    {
        return this.dimensions;
    }

    @Override
    public void contributeNecessaryImports( final Set<TypeReference> imports )
    {
        this.base.contributeNecessaryImports( imports );
    }
    
    private static String generateArrayNotation( final int dimensions )
    {
        if( dimensions < COMMON_ARRAY_DIMENSIONS.length )
        {
            return COMMON_ARRAY_DIMENSIONS[ dimensions ];
        }
        else
        {
            final StringBuilder buf = new StringBuilder();
            
            for( int i = 0; i < dimensions; i++ )
            {
                buf.append( ARRAY_NOTATION );
            }
            
            return buf.toString();
        }
    }
    
}
