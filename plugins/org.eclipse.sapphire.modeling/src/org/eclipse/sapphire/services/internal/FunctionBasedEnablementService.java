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

package org.eclipse.sapphire.services.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.modeling.annotations.Enablement;
import org.eclipse.sapphire.modeling.el.FailSafeFunction;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.el.Literal;
import org.eclipse.sapphire.modeling.el.ModelElementFunctionContext;
import org.eclipse.sapphire.modeling.el.parser.ExpressionLanguageParser;
import org.eclipse.sapphire.services.EnablementService;
import org.eclipse.sapphire.services.EnablementServiceData;
import org.eclipse.sapphire.services.ServiceCondition;
import org.eclipse.sapphire.services.ServiceContext;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class FunctionBasedEnablementService extends EnablementService
{
    private final List<FunctionResult> functionResults = new ArrayList<FunctionResult>();
    
    @Override
    protected void initEnablementService()
    {
        final ModelElementFunctionContext fnContext = new ModelElementFunctionContext( context( Element.class ) );
        
        final Listener functionResultListener = new Listener()
        {
            @Override
            public void handle( final Event event )
            {
                refresh();
            }
        };
        
        for( Enablement annotation : context( PropertyDef.class ).getAnnotations( Enablement.class ) )
        {
            Function function = null;
            
            try
            {
                function = ExpressionLanguageParser.parse( annotation.expr() );
            }
            catch( Exception e )
            {
                Sapphire.service( LoggingService.class ).log( e );
                function = null;
            }
            
            if( function != null )
            {
                function = FailSafeFunction.create( function, Literal.create( Boolean.class ), Literal.create( Boolean.FALSE ) );
                
                final FunctionResult functionResult = function.evaluate( fnContext );
                
                functionResult.attach( functionResultListener );
                this.functionResults.add( functionResult );
            }
        }
    }

    @Override
    protected EnablementServiceData compute()
    {
        boolean state = true;
        
        for( FunctionResult result : this.functionResults )
        {
            state = ( state && (Boolean) result.value() );
        }
        
        return new EnablementServiceData( state );
    }

    @Override
    public void dispose()
    {
        super.dispose();
        
        for( FunctionResult result : this.functionResults )
        {
            try
            {
                result.dispose();
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
            return context.find( PropertyDef.class ).hasAnnotation( Enablement.class );
        }
    }
    
}
