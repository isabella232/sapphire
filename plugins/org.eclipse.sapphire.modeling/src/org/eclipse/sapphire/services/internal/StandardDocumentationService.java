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

package org.eclipse.sapphire.services.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.annotations.Documentation;
import org.eclipse.sapphire.modeling.localization.LocalizationService;
import org.eclipse.sapphire.services.DocumentationService;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class StandardDocumentationService extends DocumentationService
{
    protected final static String LINE_BREAK = "[br/]";
    protected final static String PARAGRAPH_BREAK = "[pbr/]";
    
    private String content;
    private List<Topic> topics;
    
    @Override
    protected void init()
    {
        super.init();
        
        final StringBuilder content = new StringBuilder();
        final List<Topic> topics = new ArrayList<Topic>();
        
        initStandardDocumentationService( content, topics );
        
        this.content = content.toString();
        this.topics = Collections.unmodifiableList( topics );
    }
    
    protected abstract void initStandardDocumentationService( StringBuilder content,
                                                              List<Topic> topics );
    
    @Override
    public final String content()
    {
        return this.content;
    }

    @Override
    public final List<Topic> topics()
    {
        return this.topics;
    }
    
    protected static final List<Topic> convert( final Documentation.Topic[] topics,
                                                final LocalizationService localization )
    {
        if( topics.length == 0 )
        {
            return Collections.emptyList();
        }
        else if( topics.length == 1 )
        {
            return Collections.singletonList( convert( topics[ 0 ], localization ) );
        }
        else
        {
            final List<Topic> resources = new ArrayList<Topic>( topics.length );
            
            for( Documentation.Topic topic : topics )
            {
                resources.add( convert( topic, localization ) );
            }
            
            return resources;
        }
    }
    
    protected static final Topic convert( final Documentation.Topic topic,
                                          final LocalizationService localization )
    {
        final String label = localization.text( topic.label().trim(), CapitalizationType.NO_CAPS, false );
        return new Topic( label, topic.url() );
    }
    
}
