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

package org.eclipse.sapphire.modeling.el;

import org.eclipse.sapphire.modeling.ModelElementHandle;

/**
 * An function that pulls a property from the context. 
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class RootPropertyAccessFunction

    extends Function

{
    public static RootPropertyAccessFunction create( final Function property )
    {
        final RootPropertyAccessFunction literal = new RootPropertyAccessFunction();
        literal.init( property );
        return literal;
    }
    
    public static RootPropertyAccessFunction create( final String property )
    {
        return create( Literal.create( property ) );
    }
    
    @Override
    public FunctionResult evaluate( final FunctionContext context )
    {
        return new FunctionResult( this, context )
        {
            private FunctionContext.Listener listener;
            
            @Override
            protected void init()
            {
                super.init();
                
                this.listener = new FunctionContext.Listener()
                {
                    @Override
                    public void handlePropertyChanged( final String property )
                    {
                        if( cast( operand( 0 ).value(), String.class ).equals( property ) )
                        {
                            refresh();
                        }
                    }
                };
                
                context().addListener( this.listener );
            }

            @Override
            protected Object evaluate()
            {
                Object res = context().property( cast( operand( 0 ).value(), String.class ) );
                
                if( res instanceof ModelElementHandle<?> )
                {
                    res = ( (ModelElementHandle<?>) res ).element();
                }
                
                return res;
            }
            
            @Override
            public void dispose()
            {
                super.dispose();
                context().removeListener( this.listener );
            }
            
        };
    }
    
}
