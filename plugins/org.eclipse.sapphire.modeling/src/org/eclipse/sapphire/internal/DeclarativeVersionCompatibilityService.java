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
import org.eclipse.sapphire.Since;
import org.eclipse.sapphire.Version;
import org.eclipse.sapphire.VersionCompatibility;
import org.eclipse.sapphire.VersionCompatibilityService;
import org.eclipse.sapphire.VersionCompatibilityTargetService;
import org.eclipse.sapphire.VersionConstraint;
import org.eclipse.sapphire.modeling.el.ConcatFunction;
import org.eclipse.sapphire.modeling.el.FailSafeFunction;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.el.Literal;
import org.eclipse.sapphire.modeling.el.ModelElementFunctionContext;
import org.eclipse.sapphire.modeling.el.parser.ExpressionLanguageParser;
import org.eclipse.sapphire.modeling.util.MiscUtil;
import org.eclipse.sapphire.services.ServiceCondition;
import org.eclipse.sapphire.services.ServiceContext;

/**
 * Implementation of VersionCompatibilityService that derives its behavior from @VersionCompatibility and @Since annotations.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class DeclarativeVersionCompatibilityService extends VersionCompatibilityService
{
    private VersionCompatibilityTargetService versionCompatibilityTargetService;
    private Listener versionCompatibilityTargetServiceListener;
    private FunctionResult functionResult;
    
    @Override
    protected void initVersionCompatibilityService()
    {
        final Element element = context( Element.class );
        final PropertyDef property = context( PropertyDef.class );
        
        this.versionCompatibilityTargetService = VersionCompatibilityTargetService.find( element, property );
        
        this.versionCompatibilityTargetServiceListener = new Listener()
        {
            @Override
            public void handle( final Event event )
            {
                refresh();
            }
        };
        
        this.versionCompatibilityTargetService.attach( this.versionCompatibilityTargetServiceListener );
        
        Function function = null;
        
        final VersionCompatibility versionCompatibilityAnnotation = property.getAnnotation( VersionCompatibility.class );

        if( versionCompatibilityAnnotation != null )
        {
            try
            {
                function = ExpressionLanguageParser.parse( versionCompatibilityAnnotation.value() );
            }
            catch( Exception e )
            {
                Sapphire.service( LoggingService.class ).log( e );
            }
        }
        else
        {
            final Since sinceAnnotation = property.getAnnotation( Since.class );
            
            try
            {
                function = ExpressionLanguageParser.parse( sinceAnnotation.value() );
            }
            catch( Exception e )
            {
                Sapphire.service( LoggingService.class ).log( e );
            }
            
            if( function != null )
            {
                function = ConcatFunction.create( "[", function );
            }
        }
        
        if( function == null )
        {
            function = Literal.NULL;
        }
        else
        {
            function = FailSafeFunction.create( function, Literal.create( VersionConstraint.class ), null );
            
            this.functionResult = function.evaluate( new ModelElementFunctionContext( element ) );
            
            this.functionResult.attach
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
        final Version version = this.versionCompatibilityTargetService.version();
        final String versioned = this.versionCompatibilityTargetService.versioned();
        
        boolean compatible = false;
        
        final VersionConstraint constraint = (VersionConstraint) this.functionResult.value();
        
        if( constraint != null && version != null )
        {
            compatible = constraint.check( version );
        }
        
        return new Data( versioned, version, constraint, compatible );
    }
    
    public VersionConstraint constraint()
    {
        final Data data = (Data) data();
        return ( data == null ? null : data.constraint() );
    }

    @Override
    public void dispose()
    {
        super.dispose();
        
        if( this.versionCompatibilityTargetService != null )
        {
            this.versionCompatibilityTargetService.detach( this.versionCompatibilityTargetServiceListener );
        }
        
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
    
    public static final class Data extends VersionCompatibilityService.Data
    {
        private final VersionConstraint constraint;
        
        public Data( final String versioned,
                     final Version version,
                     final VersionConstraint constraint,
                     final boolean compatible )
        {
           super( compatible, version, versioned );
           
           this.constraint = constraint;
        }
        
        public VersionConstraint constraint()
        {
            return this.constraint;
        }
        
        @Override
        public boolean equals( final Object obj )
        {
            if( obj instanceof Data )
            {
                final Data data = (Data) obj;
                return super.equals( obj ) && MiscUtil.equal( this.constraint, data.constraint );
            }
            
            return false;
        }
        
        @Override
        public int hashCode()
        {
            return super.hashCode() + ( this.constraint == null ? 0 : this.constraint.hashCode() );
        }
    }
    
    public static final class Condition extends ServiceCondition
    {
        @Override
        public boolean applicable( final ServiceContext context )
        {
            final Element element = context.find( Element.class );
            final PropertyDef property = context.find( PropertyDef.class );
            
            return ( property.hasAnnotation( VersionCompatibility.class ) || property.hasAnnotation( Since.class ) ) &&
                   ( VersionCompatibilityTargetService.find( element, property ) != null );
        }
    }
    
}
