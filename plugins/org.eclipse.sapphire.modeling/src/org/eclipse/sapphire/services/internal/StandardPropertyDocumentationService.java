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

import java.util.List;
import java.util.SortedSet;

import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.annotations.Documentation;
import org.eclipse.sapphire.modeling.annotations.DocumentationMergeStrategy;
import org.eclipse.sapphire.modeling.localization.LocalizationService;
import org.eclipse.sapphire.services.FactsAggregationService;
import org.eclipse.sapphire.services.ServiceCondition;
import org.eclipse.sapphire.services.ServiceContext;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class StandardPropertyDocumentationService extends StandardDocumentationService
{
    @Override
    protected void initStandardDocumentationService( final StringBuilder content,
                                                     final List<Topic> topics )
    {
        final Property property = context( Property.class );
        
        init( property.definition(), content, topics );
        
        final SortedSet<String> facts = property.service( FactsAggregationService.class ).facts();
        
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
    
    private static void init( final PropertyDef property,
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
    
    public static final class Condition extends ServiceCondition
    {
        @Override
        public boolean applicable( final ServiceContext context )
        {
            final Property property = context.find( Property.class );
            
            for( PropertyDef p = property.definition(); p != null ; p = p.getBase() )
            {
                if( p.hasAnnotation( Documentation.class ) )
                {
                    return true;
                }
            }
            
            if( ! property.service( FactsAggregationService.class ).facts().isEmpty() )
            {
                return true;
            }
            
            return false;
        }
    }
    
}
