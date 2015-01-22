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

package org.eclipse.sapphire.ui.assist.internal;

import java.util.Set;
import java.util.TreeSet;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.services.FactsAggregationService;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistContext;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistContribution;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistContributor;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistSection;
import org.eclipse.sapphire.ui.forms.PropertyEditorPart;
import org.eclipse.sapphire.ui.forms.WithPart;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class FactsAssistContributor extends PropertyEditorAssistContributor
{
    public FactsAssistContributor()
    {
        setId( ID_FACTS_CONTRIBUTOR );
        setPriority( PRIORITY_FACTS_CONTRIBUTOR );
    }
    
    @Override
    public void contribute( final PropertyEditorAssistContext context )
    {
    	SapphirePart part = context.getPart();
        final Element element = part.getLocalModelElement();
        Property property = null;
        if (part instanceof PropertyEditorPart)
        {
        	property = ((PropertyEditorPart)part).property();
        }
        else if (part instanceof WithPart)
        {
        	property = ((WithPart)part).property();
        }
        
        boolean contribute = false;
        
        if( property == null )
        {
        	contribute = ! element.validation().ok();
        }
        else if( ! property.validation().ok() )
        {
            contribute = true;
        }
        else
        {
            contribute = ! property.empty();
        }
        
        if( contribute )
        {
        	final Set<String> facts;
        	
        	if( property != null )
        	{
        		facts = property.service( FactsAggregationService.class ).facts();
        	}
        	else
        	{
        		FactsAggregationService service = element.service( FactsAggregationService.class );
        		facts = service != null ? service.facts() : new TreeSet<String>();				
        	}
        	
            for( String fact : facts )
            {
                final PropertyEditorAssistContribution.Factory contribution = PropertyEditorAssistContribution.factory();
                contribution.text( "<p>" + escapeForXml( fact ) + "</p>" );
                
                final PropertyEditorAssistSection section = context.getSection( SECTION_ID_INFO );
                section.addContribution( contribution.create() );
            }
        }
    }
    
}
