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

package org.eclipse.sapphire.ui.assist.internal;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.SourceEditorService;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistContext;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistContribution;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistContributor;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistSection;
import org.eclipse.sapphire.ui.forms.PropertyEditorPart;
import org.eclipse.sapphire.ui.forms.WithPart;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ShowInSourceActionAssistContributor extends PropertyEditorAssistContributor
{
    @Text( "Show in source" )
    private static LocalizableText action;
    
    static
    {
        LocalizableText.init( ShowInSourceActionAssistContributor.class );
    }

    public ShowInSourceActionAssistContributor()
    {
        setId( ID_SHOW_IN_SOURCE_ACTION_CONTRIBUTOR );
        setPriority( PRIORITY_SHOW_IN_SOURCE_ACTION_CONTRIBUTOR );
    }
    
    @Override
    public void contribute( final PropertyEditorAssistContext context )
    {
    	SapphirePart part = context.getPart();
        Property property0 = null;
        if (part instanceof PropertyEditorPart)
        {
        	property0 = ((PropertyEditorPart)part).property();
        	
        }
        else if (part instanceof WithPart)
        {
        	property0 = ((WithPart)part).property();
        }
        final Element element = part.getLocalModelElement();
        
        final SourceEditorService sourceEditorService = element.adapt( SourceEditorService.class );
        
        if( sourceEditorService == null )
        {
            return;
        }
        
        boolean contribute = false;
        
        if( property0 == null )
        {
        	contribute = true;
        }
        else if( ! property0.definition().isDerived() )
        {
            contribute = ! property0.empty();
        }
        
        if( ! contribute )
        {
            return;
        }

        final Property property = property0;
        final PropertyEditorAssistContribution.Factory contribution = PropertyEditorAssistContribution.factory();
        
        contribution.text( "<p><a href=\"action\" nowrap=\"true\">" + escapeForXml( action.text() ) + "</a></p>" );
        
        contribution.link
        (
            "action",
            new Runnable()
            {
                public void run()
                {
                    sourceEditorService.show( element, property != null ? property.definition() : null);
                }
            }
        );
        
        final PropertyEditorAssistSection section = context.getSection( SECTION_ID_ACTIONS );
        section.addContribution( contribution.create() );
    }
    
}
