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

package org.eclipse.sapphire.ui.assist.internal;

import org.eclipse.sapphire.modeling.ElementProperty;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ImpliedElementProperty;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.services.FactsAggregationService;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistContext;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistContribution;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistContributor;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistSection;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
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
        final IModelElement element = context.getModelElement();
        final ModelProperty property = context.getProperty();
        
        boolean contribute = false;
        
        if( ! element.validation( property ).ok() )
        {
            contribute = true;
        }
        else
        {
            if( property instanceof ValueProperty )
            {
                if( element.read( (ValueProperty) property ).getText( false ) != null )
                {
                    contribute = true;
                }
            }
            else if( property instanceof ElementProperty && ! ( property instanceof ImpliedElementProperty ) )
            {
                if( element.read( (ElementProperty) property ).element() != null )
                {
                    contribute = true;
                }
            }
            else if( property instanceof ListProperty )
            {
                if( element.read( (ListProperty) property ).size() > 0 )
                {
                    contribute = true;
                }
            }
        }
        
        if( contribute )
        {
            for( String fact : element.service( property, FactsAggregationService.class ).facts() )
            {
                final PropertyEditorAssistContribution.Factory contribution = PropertyEditorAssistContribution.factory();
                contribution.text( "<p>" + escapeForXml( fact ) + "</p>" );
                
                final PropertyEditorAssistSection section = context.getSection( SECTION_ID_INFO );
                section.addContribution( contribution.create() );
            }
        }
    }
    
}
