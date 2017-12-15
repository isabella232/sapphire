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

package org.eclipse.sapphire.modeling.el;

import java.util.List;

import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class DeferredFunction

    extends Function
    
{
    private final String name;
    
    public DeferredFunction( final String name )
    {
        this.name = name;
    }
    
    public static DeferredFunction create( final String name,
                                           final List<Function> operands )
    {
        final DeferredFunction function = new DeferredFunction( name );
        function.init( operands );
        return function;
    }

    @Override
    public String name()
    {
        return this.name;
    }
    
    @Override
    public FunctionResult evaluate( final FunctionContext context )
    {
        return new FunctionResult( this, context )
        {
            private FunctionResult baseResult;
            
            @Override
            protected void init()
            {
                super.init();
                
                final Function function = context().function( name(), DeferredFunction.this.operands() );
                this.baseResult = function.evaluate( context() );
                
                final Listener listener = new Listener()
                {
                    @Override
                    public void handle( final Event event )
                    {
                        refresh();
                    }
                };
                
                this.baseResult.attach( listener );
            }

            @Override
            protected Object evaluate()
            {
                return this.baseResult.value();
            }

            @Override
            public void dispose()
            {
                super.dispose();
                this.baseResult.dispose();
            }
        };
    }
    
}
