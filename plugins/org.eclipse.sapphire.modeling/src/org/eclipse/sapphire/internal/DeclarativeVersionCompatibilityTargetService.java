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

package org.eclipse.sapphire.internal;

import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.Version;
import org.eclipse.sapphire.VersionCompatibilityTarget;
import org.eclipse.sapphire.VersionCompatibilityTargetService;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.LoggingService;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.el.FailSafeFunction;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.el.Literal;
import org.eclipse.sapphire.modeling.el.ModelElementFunctionContext;
import org.eclipse.sapphire.modeling.el.parser.ExpressionLanguageParser;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ServiceFactory;

/**
 * Implementation of VersionCompatibilityTargetService that derives its behavior from @VersionCompatibilityTarget annotation.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class DeclarativeVersionCompatibilityTargetService extends VersionCompatibilityTargetService
{
    private FunctionResult versionFunctionResult;
    private FunctionResult versionedFunctionResult;
    
    @Override
    protected void initContextVersionService()
    {
        final IModelElement element = context( IModelElement.class );
        final ModelProperty property = context( ModelProperty.class );
        
        final VersionCompatibilityTarget versionCompatibilityTargetAnnotation;
        
        if( property != null )
        {
            versionCompatibilityTargetAnnotation = property.getAnnotation( VersionCompatibilityTarget.class );
        }
        else
        {
            versionCompatibilityTargetAnnotation = element.type().getAnnotation( VersionCompatibilityTarget.class );
        }
        
        Function versionFunction = null;
        
        try
        {
            versionFunction = ExpressionLanguageParser.parse( versionCompatibilityTargetAnnotation.version() );
        }
        catch( Exception e )
        {
            LoggingService.log( e );
        }
        
        if( versionFunction == null )
        {
            versionFunction = Literal.NULL;
            this.versionedFunctionResult = versionFunction.evaluate( new FunctionContext() );
        }
        else
        {
            versionFunction = FailSafeFunction.create( versionFunction, Literal.create( Version.class ), null );
            
            this.versionFunctionResult = versionFunction.evaluate( new ModelElementFunctionContext( element ) );
            
            this.versionFunctionResult.attach
            (
                new Listener()
                {
                    @Override
                    public void handle( final Event event )
                    {
                        refresh();
                    }
                }
            );
        }

        Function versionedFunction = null;
        
        try
        {
            versionedFunction = ExpressionLanguageParser.parse( versionCompatibilityTargetAnnotation.versioned() );
        }
        catch( Exception e )
        {
            LoggingService.log( e );
        }
        
        if( versionedFunction == null )
        {
            versionedFunction = Literal.NULL;
            this.versionedFunctionResult = versionedFunction.evaluate( new FunctionContext() );
        }
        else
        {
            versionedFunction = FailSafeFunction.create( versionedFunction, Literal.create( String.class ), null );
            
            this.versionedFunctionResult = versionedFunction.evaluate( new ModelElementFunctionContext( element ) );
            
            this.versionedFunctionResult.attach
            (
                new Listener()
                {
                    @Override
                    public void handle( final Event event )
                    {
                        refresh();
                    }
                }
            );
        }
    }
    
    @Override
    protected Data compute()
    {
        return new Data( (Version) this.versionFunctionResult.value(), (String) this.versionedFunctionResult.value() );
    }

    @Override
    public void dispose()
    {
        super.dispose();
        
        if( this.versionFunctionResult != null )
        {
            try
            {
                this.versionFunctionResult.dispose();
            }
            catch( Exception e )
            {
                LoggingService.log( e );
            }
        }

        if( this.versionFunctionResult != null )
        {
            try
            {
                this.versionFunctionResult.dispose();
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
                return property.hasAnnotation( VersionCompatibilityTarget.class );
            }
            else
            {
                final IModelElement element = context.find( IModelElement.class );
                return element != null && element.type().hasAnnotation( VersionCompatibilityTarget.class );
            }
        }

        @Override
        public Service create( final ServiceContext context,
                               final Class<? extends Service> service )
        {
            return new DeclarativeVersionCompatibilityTargetService();
        }
    }
    
}
