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
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.Version;
import org.eclipse.sapphire.VersionCompatibilityTarget;
import org.eclipse.sapphire.VersionCompatibilityTargetService;
import org.eclipse.sapphire.modeling.el.FailSafeFunction;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.el.Literal;
import org.eclipse.sapphire.modeling.el.ModelElementFunctionContext;
import org.eclipse.sapphire.modeling.el.parser.ExpressionLanguageParser;
import org.eclipse.sapphire.services.ServiceCondition;
import org.eclipse.sapphire.services.ServiceContext;

/**
 * Implementation of VersionCompatibilityTargetService that derives its behavior from @VersionCompatibilityTarget annotation.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class DeclarativeVersionCompatibilityTargetService extends VersionCompatibilityTargetService
{
    private FunctionResult versionFunctionResult;
    private FunctionResult versionedFunctionResult;
    
    @Override
    protected void initContextVersionService()
    {
        final Element element = context( Element.class );
        final PropertyDef property = context( PropertyDef.class );
        
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
            Sapphire.service( LoggingService.class ).log( e );
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
            Sapphire.service( LoggingService.class ).log( e );
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
                Sapphire.service( LoggingService.class ).log( e );
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
                return property.hasAnnotation( VersionCompatibilityTarget.class );
            }
            else
            {
                final Element element = context.find( Element.class );
                return element != null && element.type().hasAnnotation( VersionCompatibilityTarget.class );
            }
        }
    }
    
}
