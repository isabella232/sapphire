/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.services.internal;

import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.LoggingService;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.annotations.Derived;
import org.eclipse.sapphire.modeling.el.FailSafeFunction;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.el.Literal;
import org.eclipse.sapphire.modeling.el.ModelElementFunctionContext;
import org.eclipse.sapphire.modeling.el.parser.ExpressionLanguageParser;
import org.eclipse.sapphire.services.DerivedValueService;
import org.eclipse.sapphire.services.DerivedValueServiceData;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ServiceFactory;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class StandardDerivedValueService extends DerivedValueService
{
    private FunctionResult functionResult;
    
    @Override
    protected void initDerivedValueService()
    {
        final Derived annotation = context( ModelProperty.class ).getAnnotation( Derived.class );
        
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
                    LoggingService.log( e );
                    function = null;
                }
                
                if( function != null )
                {
                    function = FailSafeFunction.create( function, Literal.create( String.class ) );
                    
                    final ModelElementFunctionContext context = new ModelElementFunctionContext( context( IModelElement.class ) );
                    
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
    protected DerivedValueServiceData compute()
    {
        if( this.functionResult == null )
        {
            return new DerivedValueServiceData( null );
        }
        else
        {
            return new DerivedValueServiceData( (String) this.functionResult.value() );
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
                LoggingService.log( e );
            }
        }
    }
    
    public static final class Factory extends ServiceFactory
    {
        @Override
        public boolean applicable( final ServiceContext context,
                                   final Class<? extends Service> service )
        {
            final ModelProperty property = context.find( ModelProperty.class );
            
            if( property != null )
            {
                final Derived annotation = property.getAnnotation( Derived.class );
                
                if( annotation != null && annotation.text().length() > 0 )
                {
                    return true;
                }
            }
            
            return false;
        }

        @Override
        public Service create( final ServiceContext context,
                               final Class<? extends Service> service )
        {
            return new StandardDerivedValueService();
        }
    }
    
}
