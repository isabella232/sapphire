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

package org.eclipse.sapphire.modeling.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.sapphire.modeling.EnablementService;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ModelPropertyService;
import org.eclipse.sapphire.modeling.ModelPropertyServiceFactory;
import org.eclipse.sapphire.modeling.LoggingService;
import org.eclipse.sapphire.modeling.annotations.Enablement;
import org.eclipse.sapphire.modeling.el.FailSafeFunction;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.el.Literal;
import org.eclipse.sapphire.modeling.el.ModelElementFunctionContext;
import org.eclipse.sapphire.modeling.el.parser.ExpressionLanguageParser;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class FunctionBasedEnablementService

    extends EnablementService
    
{
    private final List<FunctionResult> functionResults = new ArrayList<FunctionResult>();
    
    @Override
    public void initEnablementService( final IModelElement element,
                                       final ModelProperty property,
                                       final String[] params )
    {
        final ModelElementFunctionContext context = new ModelElementFunctionContext( element );
        
        final FunctionResult.Listener functionResultListener = new FunctionResult.Listener()
        {
            @Override
            public void handleValueChanged()
            {
                refresh();
            }
        };
        
        for( Enablement annotation : property.getAnnotations( Enablement.class ) )
        {
            Function function = null;
            
            try
            {
                function = ExpressionLanguageParser.parse( annotation.expr() );
            }
            catch( Exception e )
            {
                LoggingService.log( e );
                function = null;
            }
            
            if( function != null )
            {
                function = FailSafeFunction.create( function, Literal.create( Boolean.class ), Literal.create( Boolean.FALSE ) );
                
                final FunctionResult functionResult = function.evaluate( context );
                
                functionResult.addListener( functionResultListener );
                this.functionResults.add( functionResult );
            }
        }
    }

    @Override
    public boolean compute()
    {
        boolean state = true;
        
        for( FunctionResult result : this.functionResults )
        {
            state = ( state && (Boolean) result.value() );
        }
        
        return state;
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
                LoggingService.log( e );
            }
        }
    }
    
    public static final class Factory extends ModelPropertyServiceFactory
    {
        @Override
        public boolean applicable( final IModelElement element,
                                   final ModelProperty property,
                                   final Class<? extends ModelPropertyService> service )
        {
            return property.hasAnnotation( Enablement.class );
        }

        @Override
        public ModelPropertyService create( final IModelElement element,
                                            final ModelProperty property,
                                            final Class<? extends ModelPropertyService> service )
        {
            return new FunctionBasedEnablementService();
        }
    }
    
}
