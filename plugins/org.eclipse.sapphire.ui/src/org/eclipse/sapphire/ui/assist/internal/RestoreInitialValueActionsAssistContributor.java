/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.assist.internal;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.sapphire.services.InitialValueService;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistContext;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistContribution;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistContributor;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistSection;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class RestoreInitialValueActionsAssistContributor extends PropertyEditorAssistContributor
{
    public RestoreInitialValueActionsAssistContributor()
    {
        setId( ID_RESTORE_INITIAL_VALUE_ACTIONS_CONTRIBUTOR );
        setPriority( PRIORITY_RESTORE_INITIAL_VALUE_ACTIONS_CONTRIBUTOR );
    }
    
    @Override
    public void contribute( final PropertyEditorAssistContext context )
    {
        final IModelElement element = context.getModelElement();
        final ModelProperty property = context.getProperty();
        
        if( property.isReadOnly() )
        {
            return;
        }
        
        if( property instanceof ValueProperty )
        {
            final ValueProperty prop = (ValueProperty) property; 
            final InitialValueService initialValueService = element.service( prop, InitialValueService.class );
            
            if( initialValueService != null )
            {
                final String initialValue = initialValueService.value();
                final String currentValue = element.read( prop ).getText( false );
                
                if( initialValue != null && ! initialValue.equals( currentValue ) )
                {
                    final PropertyEditorAssistContribution.Factory contribution = PropertyEditorAssistContribution.factory();
                    
                    contribution.text( "<p><a href=\"action\" nowrap=\"true\">" + escapeForXml( Resources.restore ) + "</a></p>" );
                    
                    contribution.link
                    (
                        "action",
                        new Runnable()
                        {
                            public void run()
                            {
                                element.write( prop, initialValue );
                            }
                        }
                    );
                    
                    final PropertyEditorAssistSection section = context.getSection( SECTION_ID_ACTIONS );
                    section.addContribution( contribution.create() );
                }
            }
        }
    }
    
    private static final class Resources extends NLS
    {
        public static String restore;
        
        static
        {
            initializeMessages( RestoreInitialValueActionsAssistContributor.class.getName(), Resources.class );
        }
    }
    
}
