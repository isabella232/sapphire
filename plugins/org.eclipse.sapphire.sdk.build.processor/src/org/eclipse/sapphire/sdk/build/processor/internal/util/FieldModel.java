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

public final class FieldModel

    extends BaseModel
    
{
    private String name;
    private TypeReference type;
    private AccessModifier accessModifier = AccessModifier.PRIVATE;
    private boolean isStatic = false;
    private boolean isFinal = false;
    private Object value;
    
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
    
    public void setType( final Class<?> type )
    {
        setType( new TypeReference( type.getName() ) );
    }
    
    public AccessModifier getAccessModifier()
    {
        return this.accessModifier;
    }
    
    public void setAccessModifier( final AccessModifier accessModifier )
    {
        this.accessModifier = accessModifier;
    }
    
    public boolean isStatic()
    {
        return this.isStatic;
    }
    
    public void setStatic( final boolean isStatic )
    {
        this.isStatic = isStatic;
    }
    
    public boolean isFinal()
    {
        return this.isFinal;
    }
    
    public void setFinal( final boolean isFinal )
    {
        this.isFinal = isFinal;
    }
    
    public Object getValue()
    {
        return this.value;
    }
    
    public void setValue( final Object value )
    {
        this.value = value;
    }
    
    @Override
    public void write( final IndentingPrintWriter pw )
    {
        this.accessModifier.write( pw );
        
        if( this.isStatic )
        {
            pw.print( "static " );
        }
        
        if( this.isFinal )
        {
            pw.print( "final " );
        }
        
        pw.print( this.type.getSimpleName() );
        pw.print( ' ' );
        pw.print( this.name );
        
        if( this.value != null )
        {
            pw.print( " = " );
            pw.print( this.value );
        }
        
        pw.print( ';' );
        pw.println();
    }

}
