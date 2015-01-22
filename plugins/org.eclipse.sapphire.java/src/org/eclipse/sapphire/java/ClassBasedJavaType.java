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

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ClassBasedJavaType extends JavaType
{
    private final Class<?> cl;
    private final JavaTypeKind kind;
    private final JavaType base;
    private final Set<JavaType> interfaces;
    
    public ClassBasedJavaType( final Class<?> cl )
    {
        if( cl == null )
        {
            throw new IllegalArgumentException();
        }
        
        this.cl = cl;
        
        if( cl.isAnnotation() )
        {
            this.kind = JavaTypeKind.ANNOTATION;
        }
        else if( cl.isEnum() )
        {
            this.kind = JavaTypeKind.ENUM;
        }
        else if( cl.isInterface() )
        {
            this.kind = JavaTypeKind.INTERFACE;
        }
        else if( Modifier.isAbstract( cl.getModifiers() ) )
        {
            this.kind = JavaTypeKind.ABSTRACT_CLASS;
        }
        else
        {
            this.kind = JavaTypeKind.CLASS;
        }
        
        final Class<?> base = cl.getSuperclass();
        
        if( base != null )
        {
            this.base = new ClassBasedJavaType( base );
        }
        else
        {
            this.base = null;
        }
        
        final Set<JavaType> interfaces = new HashSet<JavaType>();
        
        for( Class<?> i : cl.getInterfaces() )
        {
            interfaces.add( new ClassBasedJavaType( i ) );
        }
        
        this.interfaces = Collections.unmodifiableSet( interfaces );
    }
    
    @Override
    public Class<?> artifact()
    {
        return this.cl;
    }
    
    @Override
    public String name()
    {
        return this.cl.getName();
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
