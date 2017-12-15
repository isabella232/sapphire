/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Ling Hao - showing labels for named values && [bugzilla 329114] rewrite context help binding feature
 ******************************************************************************/

package org.eclipse.sapphire.ui.assist.internal;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.Value;
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
        
        if( property instanceof ValueProperty )
        {
            final Value<?> val = element.read( (ValueProperty) property );

            if( val.getText( false ) != null )
            {
                contribute = true;
            }
        }
        else if( property instanceof ListProperty )
        {
            final ModelElementList<?> list = element.read( (ListProperty) property );
            
            if( list.size() > 0 )
            {
                contribute = true;
            }
        }
        
        if( ! contribute )
        {
            return;
        }
        
        for( String fact : element.service( property, FactsAggregationService.class ).facts() )
        {
            final PropertyEditorAssistContribution.Factory contribution = PropertyEditorAssistContribution.factory();
            contribution.text( "<p>" + escapeForXml( fact ) + "</p>" );
            
            final PropertyEditorAssistSection section = context.getSection( SECTION_ID_INFO );
            section.addContribution( contribution.create() );
        }
    }
    
}
