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
import org.eclipse.sapphire.modeling.annotations.Enablement;
import org.eclipse.sapphire.modeling.el.FailSafeFunction;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.el.ModelElementFunctionContext;
import org.eclipse.sapphire.modeling.el.parser.ExpressionLanguageParser;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class EnablementServiceFactory

    extends ModelPropertyServiceFactory
    
{
    private static EnablementService DEFAULT_ENABLEMENT_SERVICE = new EnablementService()
    {
        @Override
        public boolean isEnabled()
        {
            return true;
        }
    };
    
    @Override
    public boolean applicable( final IModelElement element,
                               final ModelProperty property,
                               final Class<? extends ModelPropertyService> service )
    {
        return true;
    }

    @Override
    public ModelPropertyService create( final IModelElement element,
                                        final ModelProperty property,
                                        final Class<? extends ModelPropertyService> service )
    {
        List<EnablementService> services = new ArrayList<EnablementService>();
        
        for( Enablement annotation : property.getAnnotations( Enablement.class ) )
        {
            EnablementService svc = null;
            
            if( ! annotation.service().equals( EnablementService.class ) )
            {
                try
                {
                    svc = annotation.service().newInstance();
                    svc.init( element, property, annotation.params() );
                }
                catch( Exception e )
                {
                    SapphireModelingFrameworkPlugin.log( e );
                    svc = null;
                }
            }
            
            if( svc == null && annotation.expr().length() > 0 )
            {
                Function f = null;
                FunctionResult fr = null;
                
                try
                {
                    f = ExpressionLanguageParser.parse( annotation.expr() );
                }
                catch( Exception e )
                {
                    SapphireModelingFrameworkPlugin.log( e );
                    f = null;
                }
                
                if( f != null )
                {
                    f = FailSafeFunction.create( f, Boolean.class );
                    fr = f.evaluate( new ModelElementFunctionContext( element ) );
                }
                
                svc = new FunctionBasedEnablementService( fr );
                svc.init( element, property, new String[ 0 ] );
            }
            
            if( svc != null )
            {
                services.add( svc );
            }
        }
        
        final int count = services.size();
        final EnablementService result;
        
        if( count == 0 )
        {
            result = DEFAULT_ENABLEMENT_SERVICE;
        }
        else if( count == 1 )
        {
            result = services.get( 0 );
        }
        else
        {
            result = new UnionEnablementService( services );
        }
        
        return result;
    }
    
    private static final class UnionEnablementService extends EnablementService
    {
        private final List<EnablementService> enablers;
        
        public UnionEnablementService( final List<EnablementService> enablers )
        {
            this.enablers = enablers;
        }

        @Override
        public boolean isEnabled()
        {
            for( EnablementService enabler : this.enablers )
            {
                try
                {
                    if( ! enabler.isEnabled() )
                    {
                        return false;
                    }
                }
                catch( Exception e )
                {
                    SapphireModelingFrameworkPlugin.log( e );
                }
            }

            return true;
        }
    }
    
    private static final class FunctionBasedEnablementService extends EnablementService
    {
        private final FunctionResult functionResult;
        
        public FunctionBasedEnablementService( final FunctionResult functionResult )
        {
            this.functionResult = functionResult;
        }
        
        @Override
        public void init( final IModelElement element,
                          final ModelProperty property,
                          final String[] params )
        {
            super.init( element, property, params );
            
            this.functionResult.addListener
            (
                new FunctionResult.Listener()
                {
                    @Override
                    public void handleValueChanged()
                    {
                        element.refresh( property );
                    }
                }
            );
        }
    
        @Override
        public boolean isEnabled()
        {
            Boolean enabled = null;
            
            if( this.functionResult != null )
            {
                enabled = (Boolean) this.functionResult.value();
            }
            
            if( enabled == null )
            {
                enabled = false;
            }
            
            return enabled;
        }
    }
    
}
