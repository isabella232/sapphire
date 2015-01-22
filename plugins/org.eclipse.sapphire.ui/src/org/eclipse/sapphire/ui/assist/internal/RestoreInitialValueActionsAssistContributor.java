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

import org.eclipse.sapphire.InitialValueService;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.Value;
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

public final class RestoreInitialValueActionsAssistContributor extends PropertyEditorAssistContributor
{
    @Text( "Restore initial value" )
    private static LocalizableText restore;
    
    static
    {
        LocalizableText.init( RestoreInitialValueActionsAssistContributor.class );
    }

    public RestoreInitialValueActionsAssistContributor()
    {
        setId( ID_RESTORE_INITIAL_VALUE_ACTIONS_CONTRIBUTOR );
        setPriority( PRIORITY_RESTORE_INITIAL_VALUE_ACTIONS_CONTRIBUTOR );
    }
    
    @Override
    public void contribute( final PropertyEditorAssistContext context )
    {
    	SapphirePart part = context.getPart();
        Property property = null;
        if (part instanceof PropertyEditorPart)
        {
        	property = ((PropertyEditorPart)part).property();
        	
        }
        else if (part instanceof WithPart)
        {
        	property = ((WithPart)part).property();
        }
        
        if( property == null || property.definition().isReadOnly() )
        {
            return;
        }
        
        if( property instanceof Value<?> )
        {
            final Value<?> value = (Value<?>) property;
            final InitialValueService initialValueService = value.service( InitialValueService.class );
            
            if( initialValueService != null )
            {
                final String initialText = initialValueService.value();
                final String currentText = value.text( false );
                
                if( initialText != null && ! initialText.equals( currentText ) )
                {
                    final PropertyEditorAssistContribution.Factory contribution = PropertyEditorAssistContribution.factory();
                    
                    contribution.text( "<p><a href=\"action\" nowrap=\"true\">" + escapeForXml( restore.text() ) + "</a></p>" );
                    
                    contribution.link
                    (
                        "action",
                        new Runnable()
                        {
                            public void run()
                            {
                                value.write( initialText, true );
                            }
                        }
                    );
                    
                    final PropertyEditorAssistSection section = context.getSection( SECTION_ID_ACTIONS );
                    section.addContribution( contribution.create() );
                }
            }
        }
    }
    
}
