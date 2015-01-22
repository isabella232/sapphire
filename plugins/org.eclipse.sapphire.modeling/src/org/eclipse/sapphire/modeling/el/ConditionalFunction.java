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

import java.util.Collections;
import java.util.List;

import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.util.MiscUtil;

/**
 * Function that returns one of two alternatives depending on a condition. 
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ConditionalFunction extends Function
{
    public static ConditionalFunction create( final Function condition,
                                              final Function positive,
                                              final Function negative )
    {
        final ConditionalFunction function = new ConditionalFunction();
        function.init( condition, positive, negative );
        return function;
    }
    
    @Override
    public String name()
    {
        return "Conditional";
    }

    @Override
    public boolean operator()
    {
        return true;
    }

    @Override
    public int precedence()
    {
        return 7;
    }

    @Override
    public FunctionResult evaluate( final FunctionContext context )
    {
        return new FunctionResult( this, context )
        {
            private Boolean lastConditionValue;
            private FunctionResult lastActiveBranch;
            private Listener listener;
            
            @Override
            protected void init()
            {
                super.init();
                
                this.listener = new Listener()
                {
                    @Override
                    public void handle( final Event event )
                    {
                        refresh();
                    }
                };
            }

            @Override
            protected List<FunctionResult> initOperands()
            {
                // Only initialize the condition operand as the other two operands should only be evaluated based
                // on condition's value.
                
                return Collections.singletonList( function().operands().get( 0 ).evaluate( context ) );
            }

            @Override
            protected Object evaluate()
            {
                final Boolean conditionValue = cast( operand( 0 ), Boolean.class );
                
                if( this.lastActiveBranch != null && ! MiscUtil.equal( this.lastConditionValue, conditionValue ) )
                {
                    this.lastConditionValue = null;
                    this.lastActiveBranch.dispose();
                    this.lastActiveBranch = null;
                }
                
                if( this.lastActiveBranch == null )
                {
                    this.lastConditionValue = conditionValue;
                    
                    final FunctionResult res;
                    
                    if( conditionValue == true )
                    {
                        res = function().operand( 1 ).evaluate( context );
                    }
                    else
                    {
                        res = function().operand( 2 ).evaluate( context );
                    }
                    
                    if( this.lastActiveBranch != null )
                    {
                        throw new IllegalStateException();
                    }
                    
                    this.lastActiveBranch = res;
                    this.lastActiveBranch.attach( this.listener );
                }

                return this.lastActiveBranch.value();
            }

            @Override
            public void dispose()
            {
                super.dispose();
                
                if( this.lastActiveBranch != null )
                {
                    this.lastActiveBranch.dispose();
                }
            }
        };
    }

    @Override
    public void toString( final StringBuilder buf,
                          final boolean topLevel )
    {
        toString( buf, operand( 0 ) );
        buf.append( " ? " );
        toString( buf, operand( 1 ) );
        buf.append( " : " );
        toString( buf, operand( 2 ) );
    }
    
    private void toString( final StringBuilder buf,
                           final Function operand )
    {
        final boolean parens = ( precedence() <= operand.precedence() );
        
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
