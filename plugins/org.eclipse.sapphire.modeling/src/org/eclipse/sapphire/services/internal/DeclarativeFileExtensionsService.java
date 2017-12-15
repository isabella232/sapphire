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

package org.eclipse.sapphire.services.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.LoggingService;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ModelPropertyService;
import org.eclipse.sapphire.modeling.ModelPropertyServiceFactory;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.FileExtensions;
import org.eclipse.sapphire.modeling.annotations.ValidFileExtensions;
import org.eclipse.sapphire.modeling.el.FailSafeFunction;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.el.Literal;
import org.eclipse.sapphire.modeling.el.ModelElementFunctionContext;
import org.eclipse.sapphire.modeling.el.parser.ExpressionLanguageParser;
import org.eclipse.sapphire.services.FileExtensionsService;

/**
 * Implementation of FileExtensionsService that derives its behavior from @FileExtensions annotation.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@SuppressWarnings( "deprecation" )

public final class DeclarativeFileExtensionsService extends FileExtensionsService
{
    private FunctionResult functionResult;
    
    @Override
    public void initFileExtensionsService( final IModelElement element,
                                           final ModelProperty property,
                                           final String[] params )
    {
        final FileExtensions fileExtensionsAnnotation = property.getAnnotation( FileExtensions.class );
        
        if( fileExtensionsAnnotation != null )
        {
            Function function = null;
            
            try
            {
                function = ExpressionLanguageParser.parse( fileExtensionsAnnotation.expr() );
            }
            catch( Exception e )
            {
                LoggingService.log( e );
                function = null;
            }
            
            if( function != null )
            {
                function = FailSafeFunction.create( function, Literal.create( List.class ), Literal.create( Collections.emptyList() ) );
                
                this.functionResult = function.evaluate( new ModelElementFunctionContext( element ) );
                
                this.functionResult.addListener
                (
                    new FunctionResult.Listener()
                    {
                        @Override
                        public void handleValueChanged()
                        {
                            refresh();
                        }
                    }
                );
            }
        }
        else
        {
            final ValidFileExtensions validFileExtensionsAnnotation = property.getAnnotation( ValidFileExtensions.class );
            
            if( validFileExtensionsAnnotation != null )
            {
                final List<String> extensions = new ArrayList<String>();
                
                for( String extension : validFileExtensionsAnnotation.value() )
                {
                    extension = extension.trim();
                    
                    if( extension.length() > 0 )
                    {
                        extensions.add( extension );
                    }
                }
                
                this.functionResult = Literal.create( extensions ).evaluate( new ModelElementFunctionContext( element ) );
            }
        }
    }

    @Override
    protected void compute( final List<String> extensions )
    {
        if( this.functionResult != null )
        {
            for( Object extension : (List<?>) this.functionResult.value() )
            {
                extensions.add( (String) extension );
            }
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
    
    public static final class Factory extends ModelPropertyServiceFactory
    {
        @Override
        public boolean applicable( final IModelElement element,
                                   final ModelProperty property,
                                   final Class<? extends ModelPropertyService> service )
        {
            return property instanceof ValueProperty && ( property.hasAnnotation( FileExtensions.class ) || property.hasAnnotation( ValidFileExtensions.class ) );
        }

        @Override
        public ModelPropertyService create( final IModelElement element,
                                            final ModelProperty property,
                                            final Class<? extends ModelPropertyService> service )
        {
            return new DeclarativeFileExtensionsService();
        }
    }
    
}
