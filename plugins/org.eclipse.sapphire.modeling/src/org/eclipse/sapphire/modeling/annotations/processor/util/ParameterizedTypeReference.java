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

package org.eclipse.sapphire.modeling.annotations.processor.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ParameterizedTypeReference

    extends TypeReference
    
{
    private static final String ARRAY_NOTATION = "[]";
    private static final String[] COMMON_ARRAY_DIMENSIONS;
    
    static
    {
        COMMON_ARRAY_DIMENSIONS = new String[ 5 ];
        
        for( int i = 1; i < COMMON_ARRAY_DIMENSIONS.length; i++ )
        {
            COMMON_ARRAY_DIMENSIONS[ i ] = COMMON_ARRAY_DIMENSIONS[ i - 1 ] + ARRAY_NOTATION;
        }
    }
    
    private final TypeReference base;
    private final List<TypeReference> parameters;
    
    public ParameterizedTypeReference( final TypeReference base,
                                       final TypeReference... parameters )
    {
        super( base.getQualifiedName() + generateParametersNotation( parameters ) );
        
        if( base instanceof ArrayTypeReference )
        {
            throw new IllegalArgumentException();
        }
        
        this.base = base;

        final List<TypeReference> params = new ArrayList<TypeReference>();
        
        for( TypeReference param : parameters )
        {
            params.add( param );
        }
        
        this.parameters = Collections.unmodifiableList( params );
    }
    
    @Override
    public TypeReference getBase()
    {
        return this.base;
    }

    public List<TypeReference> getParameters()
    {
        return this.parameters;
    }
    
    @Override
    public void contributeNecessaryImports( final Set<TypeReference> imports )
    {
        this.base.contributeNecessaryImports( imports );
        
        for( TypeReference param : this.parameters )
        {
            param.contributeNecessaryImports( imports );
        }
    }
    
    private static String generateParametersNotation( final TypeReference... parameters )
    {
        final StringBuilder buf = new StringBuilder();
        
        buf.append( '<' );
        
        boolean first = true;
        
        for( TypeReference param : parameters )
        {
            if( first )
            {
                first = false;
            }
            else
            {
                buf.append( ',' );
            }
            
            buf.append( param.getSimpleName() );
        }
        
        buf.append( '>' );
        
        return buf.toString();
    }
    
}
