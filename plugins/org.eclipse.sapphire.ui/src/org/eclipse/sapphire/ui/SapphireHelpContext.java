/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ling Hao - initial implementation and ongoing maintenance
 *    Konstantin Komissarchik - [342098] Separate dependency on org.eclipse.core.runtime (part 1)
 *    Konstantin Komissarchik - [350340] Eliminate DocumentationProvider annotation in favor of service approach
 ******************************************************************************/

package org.eclipse.sapphire.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.help.IContext;
import org.eclipse.help.IContext2;
import org.eclipse.help.IHelpResource;
import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.util.internal.DocumentationUtil;
import org.eclipse.sapphire.services.DocumentationService;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public final class SapphireHelpContext implements IContext, IContext2 
{
    private final String title;
    private final String content;
    private final List<IHelpResource> topics;
    
    public SapphireHelpContext( final Element element, 
                                final PropertyDef property ) 
    {
        String title = null;
        final StringBuilder content = new StringBuilder();
        final List<DocumentationService.Topic> topics = new ArrayList<DocumentationService.Topic>();
        
        DocumentationService propDocService = null;
        
        if(property != null)
        {
        	propDocService = element.property( property ).service( DocumentationService.class );
        }
        
        final DocumentationService elDocService = element.service( DocumentationService.class );
        
        content.append( "[br/]" );
        
        if( propDocService == null )
        {
            if( elDocService != null )
            {
                title = element.type().getLabel( true, CapitalizationType.TITLE_STYLE, false );
                content.append( elDocService.content() );
                topics.addAll( elDocService.topics() );
            }
        }
        else
        {
            title = property.getLabel( true, CapitalizationType.TITLE_STYLE, false );
            content.append( propDocService.content() );
            topics.addAll( propDocService.topics() );
            
            if( elDocService != null )
            {
                content.append( "[pbr/][b]" );
                content.append( element.type().getLabel( true, CapitalizationType.TITLE_STYLE, false ) );
                content.append( "[/b][pbr/]" );
                content.append( elDocService.content() );
                topics.addAll( elDocService.topics() );
            }
        }
        
        content.append( "[pbr/]" );
        
        this.title = title;
        this.content = DocumentationUtil.decodeDocumentationTags( content.toString() );
        
        this.topics = new ArrayList<IHelpResource>( topics.size() );
        
        for( final DocumentationService.Topic topic : topics )
        {
            final IHelpResource hres = new IHelpResource()
            {
                public String getLabel()
                {
                    return topic.label();
                }
                
                public String getHref()
                {
                    return topic.url();
                }
            };
            
            this.topics.add( hres );
        }
    }
    
    public String getTitle() 
    {
        return this.title;
    }

    public String getText() 
    {
        return this.content;
    }
    
    public String getStyledText() 
    {
        return null;
    }

    public String getCategory( final IHelpResource topic ) 
    {
        return null;
    }

    public IHelpResource[] getRelatedTopics()
    {
        return this.topics.toArray( new IHelpResource[ this.topics.size() ] );
    }

}