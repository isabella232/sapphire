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

import java.util.List;
import java.util.SortedSet;

import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.annotations.Documentation;
import org.eclipse.sapphire.modeling.annotations.DocumentationMergeStrategy;
import org.eclipse.sapphire.modeling.localization.LocalizationService;
import org.eclipse.sapphire.services.FactsAggregationService;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ServiceFactory;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class StandardPropertyDocumentationService extends StandardDocumentationService
{
    @Override
    protected void initStandardDocumentationService( final StringBuilder content,
                                                     final List<Topic> topics )
    {
        final IModelElement element = context( IModelElement.class );
        final ModelProperty property = context( ModelProperty.class );
        
        init( property, content, topics );
        
        final SortedSet<String> facts = element.service( property, FactsAggregationService.class ).facts();
        
        if( ! facts.isEmpty() )
        {
            if( content.length() > 0 )
            {
                content.append( PARAGRAPH_BREAK );
            }
            
            boolean first = true;
            
            for( String item : facts )
            {
                if( first )
                {
                    first = false;
                }
                else
                {
                    content.append( LINE_BREAK );
                }
                
                content.append( item );
            }
        }
    }
    
    private static void init( final ModelProperty property,
                              final StringBuilder content,
                              final List<Topic> topics )
    {
        if( property == null )
        {
            return;
        }
        
        final Documentation docAnnotation = property.getAnnotation( Documentation.class );
        
        if( docAnnotation == null || docAnnotation.mergeStrategy() != DocumentationMergeStrategy.REPLACE )
        {
            init( property.getBase(), content, topics );
        }
        
        if( docAnnotation != null )
        {
            final LocalizationService localization = property.getLocalizationService();
            final DocumentationMergeStrategy docMergeStrategy = docAnnotation.mergeStrategy();
            final String docAnnotationContent = localization.text( docAnnotation.content().trim(), CapitalizationType.NO_CAPS, false );
            
            if( docAnnotationContent.length() > 0 )
            {
                if( docMergeStrategy == DocumentationMergeStrategy.REPLACE || content.length() == 0 )
                {
                    content.append( docAnnotationContent );
                }
                else if( docMergeStrategy == DocumentationMergeStrategy.APPEND )
                {
                    content.append( PARAGRAPH_BREAK );
                    content.append( docAnnotationContent );
                }
                else
                {
                    content.insert( 0, PARAGRAPH_BREAK );
                    content.insert( 0, docAnnotationContent );
                }
            }
            
            final List<Topic> docAnnotationTopics = convert( docAnnotation.topics(), localization );
            
            if( ! docAnnotationTopics.isEmpty() )
            {
                if( docMergeStrategy != DocumentationMergeStrategy.PREPEND || topics.isEmpty() )
                {
                    topics.addAll( docAnnotationTopics );
                }
                else
                {
                    topics.addAll( 0, docAnnotationTopics );
                }
            }
        }
    }
    
    public static final class Factory extends ServiceFactory
    {
        @Override
        public boolean applicable( final ServiceContext context,
                                   final Class<? extends Service> service )
        {
            final IModelElement element = context.find( IModelElement.class );
            final ModelProperty property = context.find( ModelProperty.class );
            
            for( ModelProperty p = property; p != null ; p = p.getBase() )
            {
                if( p.hasAnnotation( Documentation.class ) )
                {
                    return true;
                }
            }
            
            if( ! element.service( property, FactsAggregationService.class ).facts().isEmpty() )
            {
                return true;
            }
            
            return false;
        }
    
        @Override
        public Service create( final ServiceContext context,
                               final Class<? extends Service> service )
        {
            return new StandardPropertyDocumentationService();
        }
    }
    
}
