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

package org.eclipse.sapphire.internal;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementProperty;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.ImpliedElementProperty;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.RequiredConstraintService;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.el.FailSafeFunction;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.el.Literal;
import org.eclipse.sapphire.modeling.el.ModelElementFunctionContext;
import org.eclipse.sapphire.modeling.el.parser.ExpressionLanguageParser;
import org.eclipse.sapphire.services.ServiceCondition;
import org.eclipse.sapphire.services.ServiceContext;

/**
 * {@link RequiredConstraintService} implementation that derives its behavior from @{@link Required} annotation.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class DeclarativeRequiredConstraintService extends RequiredConstraintService
{
    private FunctionResult functionResult;
    
    @Override
    protected void initRequiredConstraintService()
    {
        Function function;
        
        final Required annotation = context( PropertyDef.class ).getAnnotation( Required.class );
        
        if( annotation == null )
        {
            function = Literal.FALSE;
        }
        else
        {
            final String expr = annotation.value().trim();
            
            if( expr.length() == 0 )
            {
                function = Literal.TRUE;
            }
            else
            {
                try
                {
                    function = ExpressionLanguageParser.parse( expr );
                    function = FailSafeFunction.create( function, Boolean.class, false );
                }
                catch( Exception e )
                {
                    Sapphire.service( LoggingService.class ).log( e );
                    function = Literal.FALSE;
                }
            }
        }

        final ModelElementFunctionContext context = new ModelElementFunctionContext( context( Element.class ) );
        
        this.functionResult = function.evaluate( context );
        
        final Listener listener = new Listener()
        {
            @Override
            public void handle( final Event event )
            {
                refresh();
            }
        };
        
        this.functionResult.attach( listener );
    }

    @Override
    protected Boolean compute()
    {
        return (Boolean) this.functionResult.value();
    }

    @Override
    public void dispose()
    {
        super.dispose();
        
        if( this.functionResult != null )
        {
            try
            {
                this.functionResult.dispose();
            }
            catch( Exception e )
            {
                Sapphire.service( LoggingService.class ).log( e );
            }
        }
    }
    
    public static final class Condition extends ServiceCondition
    {
        @Override
        public boolean applicable( final ServiceContext context )
        {
            final PropertyDef property = context.find( PropertyDef.class );
            return ( property instanceof ValueProperty || ( property instanceof ElementProperty && ! ( property instanceof ImpliedElementProperty ) ) );
        }
    }
    
}
