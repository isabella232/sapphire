/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.assist.internal;

import org.eclipse.osgi.util.NLS;
import org.eclipse.sapphire.modeling.ElementProperty;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ImpliedElementProperty;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.ui.SourceEditorService;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistContext;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistContribution;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistContributor;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistSection;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ShowInSourceActionAssistContributor

    extends PropertyEditorAssistContributor
    
{
    public ShowInSourceActionAssistContributor()
    {
        setId( ID_SHOW_IN_SOURCE_ACTION_CONTRIBUTOR );
        setPriority( PRIORITY_SHOW_IN_SOURCE_ACTION_CONTRIBUTOR );
    }
    
    @Override
    public void contribute( final PropertyEditorAssistContext context )
    {
        final IModelElement element = context.getModelElement();
        final ModelProperty prop = context.getProperty();
        final SourceEditorService sourceEditorService = element.adapt( SourceEditorService.class );
        
        if( sourceEditorService == null )
        {
            return;
        }
        
        boolean contribute = false;
        
        if( prop instanceof ValueProperty )
        {
            if( element.read( (ValueProperty) prop ).getText( false ) != null )
            {
                contribute = true;
            }
        }
        else if( prop instanceof ListProperty )
        {
            if( element.read( (ListProperty) prop ).size() > 0 )
            {
                contribute = true;
            }
        }
        else if( prop instanceof ElementProperty && ! ( prop instanceof ImpliedElementProperty ) )
        {
            if( element.read( (ElementProperty) prop ).element() != null )
            {
                contribute = true;
            }
        }
        
        if( ! contribute )
        {
            return;
        }

        final PropertyEditorAssistContribution contribution = new PropertyEditorAssistContribution();
        contribution.setText( "<p><a href=\"action\" nowrap=\"true\">" + escapeForXml( Resources.action ) + "</a></p>" );
        
        contribution.setHyperlinkListener
        (
            new HyperlinkAdapter()
            {
                @Override
                public void linkActivated( final HyperlinkEvent event )
                {
                    sourceEditorService.show( element, prop );
                }
            }
        );
        
        final PropertyEditorAssistSection section = context.getSection( SECTION_ID_ACTIONS );
        section.addContribution( contribution );
    }
    
    private static final class Resources
        
        extends NLS
    
    {
        public static String action;
        
        static
        {
            initializeMessages( ShowInSourceActionAssistContributor.class.getName(), Resources.class );
        }
    }
    
}
