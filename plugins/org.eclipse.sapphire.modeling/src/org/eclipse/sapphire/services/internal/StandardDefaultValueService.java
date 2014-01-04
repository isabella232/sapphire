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

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.el.FailSafeFunction;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.el.Literal;
import org.eclipse.sapphire.modeling.el.ModelElementFunctionContext;
import org.eclipse.sapphire.modeling.el.parser.ExpressionLanguageParser;
import org.eclipse.sapphire.services.DefaultValueService;
import org.eclipse.sapphire.services.DefaultValueServiceData;
import org.eclipse.sapphire.services.ServiceCondition;
import org.eclipse.sapphire.services.ServiceContext;

/**
 * Implementation of {@link DefaultValueService} that draws the default value from @{@link DefaultValue} annotation.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class StandardDefaultValueService extends DefaultValueService
{
    private FunctionResult functionResult;
    
    @Override
    protected void initDefaultValueService()
    {
        final DefaultValue annotation = context( PropertyDef.class ).getAnnotation( DefaultValue.class );
        
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
    protected DefaultValueServiceData compute()
    {
        if( this.functionResult == null )
        {
            return new DefaultValueServiceData( null );
        }
        else
        {
            return new DefaultValueServiceData( (String) this.functionResult.value() );
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
                return property.hasAnnotation( DefaultValue.class );
            }
            
            return false;
        }
    }
    
}
