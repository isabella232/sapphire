/*******************************************************************************
 * Copyright (c) 2016 Oracle and Accenture
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Kamesh Sampath - initial implementation
 *******************************************************************************/

package org.eclipse.sapphire.internal;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.InitialValueService;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.modeling.annotations.InitialValue;
import org.eclipse.sapphire.modeling.el.FailSafeFunction;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.el.Literal;
import org.eclipse.sapphire.modeling.el.ModelElementFunctionContext;
import org.eclipse.sapphire.modeling.el.parser.ExpressionLanguageParser;
import org.eclipse.sapphire.services.ServiceCondition;
import org.eclipse.sapphire.services.ServiceContext;

/**
 * Implementation of {@link InitialValueService} that draws the initial value from @{@link InitialValue} annotation.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 * @author <a href="mailto:kamesh.sampath@accenture.com">Kamesh Sampath</a>
 */

public final class StandardInitialValueService extends InitialValueService
{
    private FunctionResult functionResult;
    
    @Override
    protected void initInitialValueService()
    {
        final InitialValue annotation = context( PropertyDef.class ).getAnnotation( InitialValue.class );
        
        if( annotation != null )
        {
            final String expr = annotation.text();
            
            if( expr.length() > 0 )
            {
                Function function = null;
                
                try
                {
                    function = ExpressionLanguageParser.parse( expr );
                }
                catch( Exception e )
                {
                    Sapphire.service( LoggingService.class ).log( e );
                    function = null;
                }
                
                if( function != null )
                {
                    function = FailSafeFunction.create( function, Literal.create( String.class ) );
                    
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
            }
        }
    }

    @Override
    protected String compute()
    {
        if( this.functionResult == null )
        {
            return null;
        }
        else
        {
            return (String) this.functionResult.value();
        }
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
            
            if( property != null )
            {
                return property.hasAnnotation( InitialValue.class );
            }
            
            return false;
        }
    }
    
}
