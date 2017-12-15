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

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class MethodParameterModel

    extends BaseModel
    
{
    private String name;
    private TypeReference type;
    private boolean isFinal = true;
    
    public MethodParameterModel()
    {
    }
    
    public MethodParameterModel( final String name,
                                 final TypeReference type,
                                 final boolean isFinal )
    {
        this.name = name;
        this.type = type;
        this.isFinal = isFinal;
    }
    
    public MethodParameterModel( final String name,
                                 final TypeReference type )
    {
        this( name, type, true );
    }
    
    public MethodParameterModel( final String name,
                                 final Class<?> type,
                                 final boolean isFinal )
    {
        this( name, new TypeReference( type ), isFinal );
    }
    
    public MethodParameterModel( final String name,
                                 final Class<?> type )
    {
        this( name, type, true );
    }
    
    public String getName()
    {
        return this.name;
    }
    
    public void setName( final String name )
    {
        this.name = name;
    }
    
    public TypeReference getType()
    {
        return this.type;
    }
    
    public void setType( final TypeReference type )
    {
        this.type = type;
    }
    
    public boolean isFinal()
    {
        return this.isFinal;
    }
    
    public void setFinal( final boolean isFinal )
    {
        this.isFinal = isFinal;
    }
    
    @Override
    public void write( final IndentingPrintWriter pw )
    {
        if( this.isFinal )
        {
            pw.print( "final " );
        }
            
        pw.print( this.type.getSimpleName() );
        pw.print( ' ' );
        pw.print( this.name );
    }

}
