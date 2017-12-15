/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.sdk.build.processor.internal.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.sun.mirror.type.PrimitiveType;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class TypeReference

    implements Comparable<TypeReference>

{
    public static final Map<PrimitiveType.Kind,TypeReference> PRIMITIVE_TYPES = new HashMap<PrimitiveType.Kind,TypeReference>();
    
    static
    {
        for( PrimitiveType.Kind typeKind : PrimitiveType.Kind.values() )
        {
            PRIMITIVE_TYPES.put( typeKind, new TypeReference( typeKind.name().toLowerCase() ) );
        }
    }
    
    public static final TypeReference VOID_TYPE = new TypeReference( "void" ); //$NON-NLS-1$
    public static final TypeReference BOOLEAN_TYPE = PRIMITIVE_TYPES.get( PrimitiveType.Kind.BOOLEAN );
    public static final TypeReference WILDCARD_TYPE_PARAM = new TypeReference( "?" ); //$NON-NLS-1$
    
    private final String qualifiedName;
    private final String simpleName;
    private final String packageName;
    
    public TypeReference( final String qualifiedName )
    {
        this.qualifiedName = qualifiedName;
        
        final int lastDot = this.qualifiedName.lastIndexOf( '.' );
        
        if( lastDot == -1 )
        {
            this.simpleName = this.qualifiedName;
            this.packageName = null;
        }
        else
        {
            this.simpleName = this.qualifiedName.substring( lastDot + 1 );
            this.packageName = this.qualifiedName.substring( 0, lastDot );
        }
    }
    
    public TypeReference( final String packageName,
                          final String simpleName )
    {
        this.packageName = packageName;
        this.simpleName = simpleName;
        
        if( this.packageName == null )
        {
            this.qualifiedName = simpleName;
        }
        else
        {
            this.qualifiedName = this.packageName + "." + this.simpleName; //$NON-NLS-1$
        }
    }
    
    public TypeReference( final Class<?> cl )
    {
        this( cl.getName() );
    }
    
    @Override
    public final boolean equals( final Object obj )
    {
        if( obj instanceof TypeReference )
        {
            return getQualifiedName().equals( ( (TypeReference) obj  ).getQualifiedName() );
        }
        
        return false;
    }
    
    @Override
    public final int hashCode()
    {
        return getQualifiedName().hashCode();
    }
    
    public final int compareTo( final TypeReference type )
    {
        if( type == this )
        {
            return 0;
        }
        else if( type == null )
        {
            return -1;
        }
        else
        {
            return getQualifiedName().compareTo( type.getQualifiedName() );
        }
    }
    
    public final String getQualifiedName()
    {
        return this.qualifiedName;
    }
    
    public final String getSimpleName()
    {
        return this.simpleName;
    }
    
    public final String getPackage()
    {
        return this.packageName;
    }
    
    public TypeReference getBase()
    {
        return null;
    }
    
    public ParameterizedTypeReference parameterize( final String... parameters )
    {
        final TypeReference[] tr = new TypeReference[ parameters.length ];
        
        for( int i = 0; i < parameters.length; i++ )
        {
            tr[ i ] = new TypeReference( parameters[ i ] );
        }
        
        return new ParameterizedTypeReference( this, tr );
    }

    public ParameterizedTypeReference parameterize( final TypeReference... parameters )
    {
        return new ParameterizedTypeReference( this, parameters );
    }
    
    public ParameterizedTypeReference parameterize( final Class<?>... parameters )
    {
        final TypeReference[] tr = new TypeReference[ parameters.length ];
        
        for( int i = 0; i < parameters.length; i++ )
        {
            tr[ i ] = new TypeReference( parameters[ i ] );
        }
        
        return new ParameterizedTypeReference( this, tr );
    }

    public ArrayTypeReference array( final int dimensions )
    {
        return new ArrayTypeReference( this, dimensions );
    }
    
    public void contributeNecessaryImports( final Set<TypeReference> imports )
    {
        imports.add( this );
    }
    
    @Override
    public final String toString()
    {
        return getQualifiedName();
    }
    
}

