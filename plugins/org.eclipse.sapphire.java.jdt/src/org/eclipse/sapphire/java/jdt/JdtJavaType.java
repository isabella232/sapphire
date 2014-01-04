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

package org.eclipse.sapphire.java.jdt;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.sapphire.java.JavaType;
import org.eclipse.sapphire.java.JavaTypeKind;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class JdtJavaType extends JavaType
{
    private final IType type;
    private final JavaTypeKind kind;
    private final JavaType base;
    private final Set<JavaType> interfaces;
    
    public JdtJavaType( final IType type ) throws JavaModelException
    {
        if( type == null )
        {
            throw new IllegalArgumentException();
        }
        
        this.type = type;
        
        if( type.isAnnotation() )
        {
            this.kind = JavaTypeKind.ANNOTATION;
        }
        else if( type.isEnum() )
        {
            this.kind = JavaTypeKind.ENUM;
        }
        else if( type.isInterface() )
        {
            this.kind = JavaTypeKind.INTERFACE;
        }
        else if( Flags.isAbstract( type.getFlags() ) )
        {
            this.kind = JavaTypeKind.ABSTRACT_CLASS;
        }
        else
        {
            this.kind = JavaTypeKind.CLASS;
        }
        
        final ITypeHierarchy typeHierarchy = type.newSupertypeHierarchy( null );
        
        final IType base = typeHierarchy.getSuperclass( type );
        
        if( base != null )
        {
            this.base = new JdtJavaType( base );
        }
        else
        {
            this.base = null;
        }
        
        final Set<JavaType> interfaces = new HashSet<JavaType>();
        
        for( IType i : typeHierarchy.getSuperInterfaces( type ) )
        {
            interfaces.add( new JdtJavaType( i ) );
        }
        
        this.interfaces = Collections.unmodifiableSet( interfaces );
    }
    
    @Override
    public IType artifact()
    {
        return this.type;
    }
    
    @Override
    public String name()
    {
        return this.type.getFullyQualifiedName();
    }
    
    @Override
    public JavaTypeKind kind()
    {
        return this.kind;
    }
    
    @Override
    public JavaType base()
    {
        return this.base;
    }

    @Override
    public Set<JavaType> interfaces()
    {
        return this.interfaces;
    }
    
}
