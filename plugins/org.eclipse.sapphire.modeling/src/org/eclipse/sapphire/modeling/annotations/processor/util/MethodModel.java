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
import java.util.List;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class MethodModel

    extends BaseModel
    
{
    private String name;
    private TypeReference returnType = TypeReference.VOID_TYPE;
    private final List<MethodParameterModel> parameters = new ArrayList<MethodParameterModel>();
    private boolean isAbstract = false;
    private boolean isConstructor = false;
    private boolean isStatic = false;
    private boolean isFinal = false;
    private AccessModifier accessModifier = AccessModifier.PUBLIC;
    private Body body = new Body();
    
    @Override
    public ClassModel getParent()
    {
        return (ClassModel) super.getParent();
    }
    
    public String getName()
    {
        return this.name;
    }
    
    public void setName( final String name )
    {
        this.name = name;
    }
    
    public TypeReference getReturnType()
    {
        return this.returnType;
    }
    
    public void setReturnType( final TypeReference returnType )
    {
        this.returnType = returnType;
    }
    
    public void setReturnType( final Class<?> returnType )
    {
        setReturnType( new TypeReference( returnType ) );
    }
    
    public List<MethodParameterModel> getParameters()
    {
        return this.parameters;
    }
    
    public void addParameter( final MethodParameterModel parameter )
    {
        this.parameters.add( parameter );
        parameter.setParent( this );
    }
    
    public boolean isAbstract()
    {
        return this.isAbstract;
    }
    
    public void setAbstract( final boolean isAbstract )
    {
        this.isAbstract = isAbstract;
    }
    
    public boolean isConstructor()
    {
        return this.isConstructor;
    }
    
    public void setConstructor( final boolean isConstructor )
    {
        this.isConstructor = isConstructor;
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
    
    public void isFinal( final boolean isFinal )
    {
        this.isFinal = isFinal;
    }
    
    public AccessModifier getAccessModifier()
    {
        return this.accessModifier;
    }
    
    public void setAccessModifier( final AccessModifier accessModifier )
    {
        this.accessModifier = accessModifier;
    }
    
    public Body getBody()
    {
        return this.body;
    }
    
    @Override
    public void write( final IndentingPrintWriter pw )
    {
        this.accessModifier.write( pw );
        
        if( ! this.isConstructor )
        {
            if( this.isAbstract )
            {
                pw.print( "abstract " );
            }
            else
            {
                if( this.isStatic )
                {
                    pw.print( "static " );
                }
                
                if( this.isFinal )
                {
                    pw.print( "final " );
                }
            }
            
            pw.print( this.returnType.getSimpleName() );
            pw.print( ' ' );
            pw.print( this.name );
        }
        else
        {
            pw.print( getParent().getName().getSimpleName() );
        }
        
        if( this.parameters.isEmpty() )
        {
            pw.print( "()" );
        }
        else
        {
            pw.print( '(' );
            
            boolean isFirst = true;
            
            for( MethodParameterModel param : this.parameters )
            {
                if( isAbstract() )
                {
                    param.setFinal( false );
                }
                
                if( isFirst )
                {
                    isFirst = false;
                }
                else
                {
                    pw.print( ',' );
                }
                    
                pw.print( ' ' );
                param.write( pw );
            }
            
            pw.print( " )" );
        }
        
        if( this.isAbstract )
        {
            pw.print( ';' );
        }
        else
        {
            pw.println();
            pw.print( '{' );
            pw.println();
            pw.increaseIndent();
            
            this.body.write( pw );
            
            pw.decreaseIndent();
            pw.print( '}' );
        }
        
        pw.println();
        pw.println();
    }

}
