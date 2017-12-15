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

import org.eclipse.sapphire.modeling.ElementProperty;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementHandle;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.sapphire.services.DefaultValueService;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistContext;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistContribution;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistContributor;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistSection;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ResetActionsAssistContributor extends PropertyEditorAssistContributor
{
    public ResetActionsAssistContributor()
    {
        setId( ID_RESET_ACTIONS_CONTRIBUTOR );
        setPriority( PRIORITY_RESET_ACTIONS_CONTRIBUTOR );
    }
    
    @Override
    public void contribute( final PropertyEditorAssistContext context )
    {
        final IModelElement element = context.getModelElement();
        final ModelProperty prop = context.getProperty();
        
        if( context.isPropertyEditorReadOnly() )
        {
            return;
        }
        
        if( prop instanceof ValueProperty )
        {
            final Value<?> val = element.read( (ValueProperty) prop );

            if( val.getText( false ) != null )
            {
                final DefaultValueService defaultValueService = element.service( prop, DefaultValueService.class );
                final boolean hasDefaultValue = ( defaultValueService == null ? false : defaultValueService.value() != null );
                final boolean isBooleanType = prop.getTypeClass().equals( Boolean.class );
                
                final String actionText
                    = ( hasDefaultValue || isBooleanType ? Resources.restoreDefaultValue : Resources.clear );
                
                final PropertyEditorAssistContribution.Factory contribution = PropertyEditorAssistContribution.factory();
                
                contribution.text( "<p><a href=\"action\" nowrap=\"true\">" + escapeForXml( actionText ) + "</a></p>" );
                
                contribution.link
                (
                    "action",
                    new Runnable()
                    {
                        public void run()
                        {
                            element.write( (ValueProperty) prop, null );
                        }
                    }
                );
                
                final PropertyEditorAssistSection section = context.getSection( SECTION_ID_ACTIONS );
                section.addContribution( contribution.create() );
            }
        }
        else if( prop instanceof ListProperty )
        {
            final ModelElementList<?> list = element.read( (ListProperty) prop );

            if( ! list.isEmpty() )
            {
                final PropertyEditorAssistContribution.Factory contribution = PropertyEditorAssistContribution.factory();
                
                contribution.text( "<p><a href=\"action\" nowrap=\"true\">" + escapeForXml( Resources.clear ) + "</a></p>" );
                
                contribution.link
                (
                    "action",
                    new Runnable()
                    {
                        public void run()
                        {
                            list.clear();
                        }
                    }
                );
                
                final PropertyEditorAssistSection section = context.getSection( SECTION_ID_ACTIONS );
                section.addContribution( contribution.create() );
            }
        }
        else if( prop instanceof ElementProperty )
        {
            final ModelElementHandle<?> handle = element.read( (ElementProperty) prop );

            if( handle.element() != null )
            {
                final PropertyEditorAssistContribution.Factory contribution = PropertyEditorAssistContribution.factory();
                
                contribution.text( "<p><a href=\"action\" nowrap=\"true\">" + escapeForXml( Resources.clear ) + "</a></p>" );
                
                contribution.link
                (
                    "action",
                    new Runnable()
                    {
                        public void run()
                        {
                            handle.remove();
                        }
                    }
                );
                
                final PropertyEditorAssistSection section = context.getSection( SECTION_ID_ACTIONS );
                section.addContribution( contribution.create() );
            }
        }
    }
    
    private static final class Resources extends NLS
    {
        public static String restoreDefaultValue;
        public static String clear;
        
        static
        {
            initializeMessages( ResetActionsAssistContributor.class.getName(), Resources.class );
        }
    }
    
}
