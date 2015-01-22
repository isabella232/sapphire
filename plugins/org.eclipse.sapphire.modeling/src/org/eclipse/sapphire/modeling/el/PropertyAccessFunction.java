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

import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.util.MiscUtil;

/**
 * A function that reads a property from the context or a child element. 
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class PropertyAccessFunction

    extends Function

{
    public static PropertyAccessFunction create( final Function element,
                                                 final Function property )
    {
        final PropertyAccessFunction literal = new PropertyAccessFunction();
        literal.init( element, property );
        return literal;
    }
    
    public static PropertyAccessFunction create( final Function element,
                                                 final String property )
    {
        return create( element, Literal.create( property ) );
    }
    
    public static PropertyAccessFunction create( final Function property )
    {
        final PropertyAccessFunction literal = new PropertyAccessFunction();
        literal.init( property );
        return literal;
    }
    
    public static PropertyAccessFunction create( final String property )
    {
        return create( Literal.create( property ) );
    }
    
    @Override
    public String name()
    {
        return ".";
    }

    @Override
    public boolean operator()
    {
        return true;
    }

    @Override
    public int precedence()
    {
        return 1;
    }
    
    @Override
    public FunctionResult evaluate( final FunctionContext context )
    {
        return new FunctionResult( this, context )
        {
            private Object lastElement;
            private String lastPropertyName;
            private FunctionResult lastPropertyValueResult;
            private Listener lastPropertyValueListener;
            
            @Override
            protected void init()
            {
                super.init();
                
                this.lastPropertyValueListener = new Listener()
                {
                    @Override
                    public void handle( final Event event )
                    {
                        refresh();
                    }
                };
            }

            @Override
            protected Object evaluate()
            {
                final Object element;
                final String property;
                
                if( operands().size() == 1 )
                {
                    element = context;
                    property = cast( operand( 0 ), String.class );
                }
                else
                {
                    element = operand( 0 );
                    property = cast( operand( 1 ), String.class );
                }
                
                if( this.lastPropertyValueResult != null )
                {
                    if( this.lastElement != element || ! MiscUtil.equal( this.lastPropertyName, property ) )
                    {
                        this.lastElement = null;
                        this.lastPropertyName = null;
                        this.lastPropertyValueResult.dispose();
                        this.lastPropertyValueResult = null;
                    }
                }
                
                if( property != null && this.lastPropertyName == null )
                {
                    this.lastElement = element;
                    this.lastPropertyName = property;
                    this.lastPropertyValueResult = context().property( element, property );
                    this.lastPropertyValueResult.attach( this.lastPropertyValueListener );
                }
                
                return ( this.lastPropertyValueResult == null ? null : this.lastPropertyValueResult.value() );
            }
            
            @Override
            public void dispose()
            {
                super.dispose();
                
                if( this.lastPropertyValueResult != null )
                {
                    this.lastPropertyValueResult.dispose();
                }
            }
        };
    }

    @Override
    public void toString( final StringBuilder buf,
                          final boolean topLevel )
    {
        if( operands().size() == 1 )
        {
            buf.append( (String) ( (Literal) operand( 0 ) ).value() );
        }
        else
        {
            operand( 0 ).toString( buf, false );
            
            final Function p = operand( 1 );
            
            if( p instanceof Literal )
            {
                buf.append( '.' );
                ( (Literal) p ).toString( buf, false );
            }
            else
            {
                buf.append( "[ " );
                p.toString( buf, false );
                buf.append( " ]" );
            }
        }
    }

}
