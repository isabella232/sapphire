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

package org.eclipse.sapphire.modeling.el;

import java.util.List;

import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.util.ListFactory;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class Function
{
    @Text( "Function {0} missing operand {1}." )
    private static LocalizableText missingOperandMessage;
    
    static
    {
        LocalizableText.init( Function.class );
    }

    private Object origin;
    private boolean originInitialized;
    private List<Class<?>> signature;
    private List<Function> operands = ListFactory.empty();
    
    public final void initSignature( final List<Class<?>> signature )
    {
        this.signature = signature;
    }
    
    public final void init( final Function... operands )
    {
        this.operands = ListFactory.unmodifiable( operands );
    }
    
    public final void init( final List<Function> operands )
    {
        this.operands = ListFactory.unmodifiable( operands );
    }
    
    public final void initOrigin( final Object origin,
                                  final boolean applyToOperands )
    {
        if( this.originInitialized )
        {
            throw new IllegalStateException();
        }
        
        this.origin = origin;
        
        if( applyToOperands )
        {
            for( Function operand : this.operands )
            {
                operand.initOrigin( origin, true );
            }
        }
    }
    
    public final Object origin()
    {
        return this.origin;
    }
    
    public abstract String name();
    
    public boolean operator()
    {
        return false;
    }
    
    public int precedence()
    {
        return 1;
    }
    
    public final List<Class<?>> signature()
    {
        return this.signature;
    }
    
    public final List<Function> operands()
    {
        return this.operands;
    }
    
    public final Function operand( final int position )
    {
        if( position < this.operands.size() )
        {
            return this.operands.get( position );
        }
        else
        {
            throw new FunctionException( missingOperandMessage.format( getClass().getName(), String.valueOf( position ) ) );
        }
    }
    
    public abstract FunctionResult evaluate( FunctionContext context );
    
    @Override
    public final String toString()
    {
        final StringBuilder buf = new StringBuilder();
        
        if( this instanceof Literal || this instanceof ConcatFunction )
        {
            toString( buf, true );
        }
        else
        {
            buf.append( "${ " );
            toString( buf, false );
            buf.append( " }" );
        }
        
        return buf.toString();
    }
    
    public void toString( final StringBuilder buf,
                          final boolean topLevel )
    {
        final String name = name();
        
        if( operator() )
        {
            final int precedence = precedence();
            
            boolean first = true;
            boolean addOperatorBeforeNext = ( this.operands.size() == 1 );
            
            for( Function operand : this.operands )
            {
                if( addOperatorBeforeNext )
                {
                    if( first )
                    {
                        first = false;
                    }
                    else
                    {
                        buf.append( ' ' );
                    }
                    
                    buf.append( name );
                    buf.append( ' ' );
                }
                else
                {
                    addOperatorBeforeNext = true;
                    first = false;
                }
                
                final boolean parens = ( precedence < operand.precedence() );
                
                if( parens )
                {
                    buf.append( "( " );
                }
                
                operand.toString( buf, false );
                
                if( parens )
                {
                    buf.append( " )" );
                }
            }
        }
        else
        {
            buf.append( name );
            buf.append( '(' );
            
            if( ! this.operands.isEmpty() )
            {
                boolean first = true;
                
                for( Function operand : this.operands )
                {
                    if( first )
                    {
                        buf.append( ',' );
                    }
                    else
                    {
                        first = false;
                    }
                    
                    buf.append( ' ' );
                    
                    operand.toString( buf, false );
                }
                
                buf.append( ' ' );
            }
            
            buf.append( ')' );
        }
    }
    
}
