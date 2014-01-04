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

package org.eclipse.sapphire.modeling.el;

import org.eclipse.sapphire.modeling.CapitalizationType;

/**
 * A function that always evaluates to the same value. 
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class Literal extends Function
{
    public static final Literal NULL = create( null );
    public static final Literal TRUE = create( Boolean.TRUE );
    public static final Literal FALSE = create( Boolean.FALSE );
    
    private Object value;
    
    public Literal( final Object value )
    {
        if( value instanceof Function )
        {
            throw new IllegalArgumentException();
        }
        
        this.value = value;
    }
    
    public static Literal create( final Object obj )
    {
        final Literal literal = new Literal( obj );
        literal.init();
        return literal;
    }
    
    @Override
    public String name()
    {
        return "Literal";
    }
    
    public Object value()
    {
        return this.value;
    }

    @Override
    public FunctionResult evaluate( final FunctionContext context )
    {
        return new FunctionResult( this, context )
        {
            @Override
            protected Object evaluate()
            {
                Object val = Literal.this.value;
                
                if( val instanceof String )
                {
                    val = context().getLocalizationService().text( (String) val, CapitalizationType.NO_CAPS, true );
                }
                
                return val;
            }
        };
    }

    @Override
    public void toString( final StringBuilder buf,
                          final boolean topLevel )
    {
        if( this.value == null )
        {
            buf.append( "null" );
        }
        else if( this.value instanceof Boolean || this.value instanceof Number )
        {
            buf.append( this.value.toString() );
        }
        else if( this.value instanceof String )
        {
            if( topLevel )
            {
                buf.append( (String) this.value );
            }
            else
            {
                buf.append( '"' );
                buf.append( ( (String) this.value ).replace( "\"", "\\\"" ) );
                buf.append( '"' );
            }
        }
        else
        {
            buf.append( '$' );
            buf.append( this.value.toString() );
            buf.append( '$' );
        }
    }

}
