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

import org.eclipse.sapphire.DefaultValueService;
import org.eclipse.sapphire.ElementHandle;
import org.eclipse.sapphire.ListProperty;
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

public final class ResetActionsAssistContributor extends PropertyEditorAssistContributor
{
    @Text( "Restore default value" )
    private static LocalizableText restoreDefaultValue;
    
    @Text( "Clear" )
    private static LocalizableText clear;
    
    static
    {
        LocalizableText.init( ResetActionsAssistContributor.class );
    }

    public ResetActionsAssistContributor()
    {
        setId( ID_RESET_ACTIONS_CONTRIBUTOR );
        setPriority( PRIORITY_RESET_ACTIONS_CONTRIBUTOR );
    }
    
    @Override
    public void contribute( final PropertyEditorAssistContext context )
    {
    	SapphirePart part = context.getPart();
        Property property0 = null;
        boolean propertyReadOnly = false;
        if (part instanceof PropertyEditorPart)
        {
        	property0 = ((PropertyEditorPart)part).property();
        	propertyReadOnly = ( (PropertyEditorPart) part ).isReadOnly();
        	
        }
        else if (part instanceof WithPart)
        {
        	property0 = ((WithPart)part).property();
        	propertyReadOnly = property0.definition().isReadOnly();
        }
        
    	        
        if( property0 == null || property0.empty() || propertyReadOnly  )
        {
            return;
        }
        
        final Property property = property0;
        if( property instanceof Value<?> )
        {
        	
            final DefaultValueService defaultValueService = property.service( DefaultValueService.class );
            final boolean hasDefaultValue = ( defaultValueService == null ? false : defaultValueService.value() != null );
            final boolean isBooleanType = property.definition().getTypeClass().equals( Boolean.class );
            
            final String actionText
                = ( hasDefaultValue || isBooleanType ? restoreDefaultValue.text() : clear.text() );
            
            final PropertyEditorAssistContribution.Factory contribution = PropertyEditorAssistContribution.factory();
            
            contribution.text( "<p><a href=\"action\" nowrap=\"true\">" + escapeForXml( actionText ) + "</a></p>" );
            
            contribution.link
            (
                "action",
                new Runnable()
                {
                    public void run()
                    {
                        property.clear();
                    }
                }
            );
            
            final PropertyEditorAssistSection section = context.getSection( SECTION_ID_ACTIONS );
            section.addContribution( contribution.create() );
        }
        else if( property.definition() instanceof ListProperty )
        {
            final PropertyEditorAssistContribution.Factory contribution = PropertyEditorAssistContribution.factory();
            
            contribution.text( "<p><a href=\"action\" nowrap=\"true\">" + escapeForXml( clear.text() ) + "</a></p>" );
            
            contribution.link
            (
                "action",
                new Runnable()
                {
                    public void run()
                    {
                        property.clear();
                    }
                }
            );
            
            final PropertyEditorAssistSection section = context.getSection( SECTION_ID_ACTIONS );
            section.addContribution( contribution.create() );
        }
        else if( property instanceof ElementHandle<?> )
        {
            final PropertyEditorAssistContribution.Factory contribution = PropertyEditorAssistContribution.factory();
            
            contribution.text( "<p><a href=\"action\" nowrap=\"true\">" + escapeForXml( clear.text() ) + "</a></p>" );
            
            contribution.link
            (
                "action",
                new Runnable()
                {
                    public void run()
                    {
                        property.clear();
                    }
                }
            );
            
            final PropertyEditorAssistSection section = context.getSection( SECTION_ID_ACTIONS );
            section.addContribution( contribution.create() );
        }
    }
    
}
