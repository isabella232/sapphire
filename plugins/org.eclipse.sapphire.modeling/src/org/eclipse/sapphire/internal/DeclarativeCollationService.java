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

package org.eclipse.sapphire.internal;

import java.util.Comparator;

import org.eclipse.sapphire.Collation;
import org.eclipse.sapphire.CollationService;
import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.ValueProperty;
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
 * A {@link CollationService} implementation that derives its behavior from @{@link Collation} annotation.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class DeclarativeCollationService extends CollationService
{
    private static final Comparator<String> IGNORE_CASE_COMPARATOR = new Comparator<String>()
    {
        public int compare( final String str1, final String str2 )
        {
            return str1.compareToIgnoreCase( str2 );
        }
    };
    
    private FunctionResult ignoreCaseDifferencesFunctionResult;
    
    @Override
    protected void initCollationService()
    {
        final Collation annotation = context( PropertyDef.class ).getAnnotation( Collation.class );
        Function ignoreCaseDifferencesFunction;
        
        try
        {
            ignoreCaseDifferencesFunction = ExpressionLanguageParser.parse( annotation.ignoreCaseDifferences() );
            ignoreCaseDifferencesFunction = FailSafeFunction.create( ignoreCaseDifferencesFunction, Boolean.class, false );
        }
        catch( Exception e )
        {
            Sapphire.service( LoggingService.class ).log( e );
            ignoreCaseDifferencesFunction = Literal.FALSE;
        }
        
        final Element element = context( Element.class );
        final FunctionContext context = ( element == null ? new FunctionContext() : new ModelElementFunctionContext( element ) );
        
        this.ignoreCaseDifferencesFunctionResult = ignoreCaseDifferencesFunction.evaluate( context );
        
        this.ignoreCaseDifferencesFunctionResult.attach
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
    
    @Override
    protected Comparator<String> compute()
    {
        if( ( (Boolean) this.ignoreCaseDifferencesFunctionResult.value() ) == true )
        {
            return IGNORE_CASE_COMPARATOR;
        }
        else
        {
            return DefaultCollationService.COMPARATOR;
        }
    }

    public static final class GlobalCondition extends ServiceCondition
    {
        @Override
        public boolean applicable( final ServiceContext context )
        {
            final ValueProperty property = context.find( ValueProperty.class );
            
            if( property != null )
            {
                final Collation collation = property.getAnnotation( Collation.class );
                
                if( collation != null )
                {
                    return collation.global() || 
                           collation.ignoreCaseDifferences().equalsIgnoreCase( "true" ) || 
                           collation.ignoreCaseDifferences().equalsIgnoreCase( "false" );
                }
            }
            
            return false;
        }
    }

    public static final class InstanceCondition extends ServiceCondition
    {
        @Override
        public boolean applicable( final ServiceContext context )
        {
            final ValueProperty property = context.find( ValueProperty.class );
            
            if( property != null )
            {
                final Collation collation = property.getAnnotation( Collation.class );
                
                if( collation != null )
                {
                    return ! collation.global() && 
                           ! collation.ignoreCaseDifferences().equalsIgnoreCase( "true" ) && 
                           ! collation.ignoreCaseDifferences().equalsIgnoreCase( "false" );
                }
            }
            
            return false;
        }
    }

}
