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
import java.util.Collections;
import java.util.List;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.FileExtensions;
import org.eclipse.sapphire.modeling.el.FailSafeFunction;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.el.Literal;
import org.eclipse.sapphire.modeling.el.ModelElementFunctionContext;
import org.eclipse.sapphire.modeling.el.parser.ExpressionLanguageParser;
import org.eclipse.sapphire.services.FileExtensionsService;
import org.eclipse.sapphire.services.FileExtensionsServiceData;
import org.eclipse.sapphire.services.ServiceCondition;
import org.eclipse.sapphire.services.ServiceContext;

/**
 * Implementation of FileExtensionsService that derives its behavior from @FileExtensions annotation.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class DeclarativeFileExtensionsService extends FileExtensionsService
{
    private FunctionResult functionResult;
    
    @Override
    protected void initFileExtensionsService()
    {
        final FileExtensions fileExtensionsAnnotation = context( PropertyDef.class ).getAnnotation( FileExtensions.class );
        
        Function function = null;
        
        try
        {
            function = ExpressionLanguageParser.parse( fileExtensionsAnnotation.expr() );
        }
        catch( Exception e )
        {
            Sapphire.service( LoggingService.class ).log( e );
            function = null;
        }
        
        if( function != null )
        {
            function = FailSafeFunction.create( function, Literal.create( List.class ), Literal.create( Collections.emptyList() ) );
            
            this.functionResult = function.evaluate( new ModelElementFunctionContext( context( Element.class ) ) );
            
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
    protected FileExtensionsServiceData compute()
    {
        final List<String> extensions = new ArrayList<String>();
        
        if( this.functionResult != null )
        {
            for( Object extension : (List<?>) this.functionResult.value() )
            {
                extensions.add( (String) extension );
            }
        }
        
        return new FileExtensionsServiceData( extensions );
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
            final ValueProperty property = context.find( ValueProperty.class );
            return property != null && property.hasAnnotation( FileExtensions.class );
        }
    }
    
}
