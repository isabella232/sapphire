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
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.ui.SapphireEditorFormPage;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistContext;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistContribution;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistContributor;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistSection;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.texteditor.ITextEditor;

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
        final SapphireEditorFormPage page = context.getPropertyEditor().getNearestPart( SapphireEditorFormPage.class );
        
        if( page == null )
        {
            return;
        }
        
        final ITextEditor sourceView = page.getSourceView();
        
        if( sourceView == null )
        {
            return;
        }
        
        final IModelElement element = context.getModelElement();
        final ModelProperty prop = context.getProperty();
        
        boolean contribute = false;
        
        if( prop instanceof ValueProperty )
        {
            final Value<?> val = ( (Value<?>) ( (ValueProperty) prop ).invokeGetterMethod( element ) );

            if( val.getText( false ) != null )
            {
                contribute = true;
            }
        }
        else if( prop instanceof ListProperty )
        {
            final ModelElementList<?> list = ( (ModelElementList<?>) ( (ListProperty) prop ).invokeGetterMethod( element ) );
            
            if( list.size() > 0 )
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
                    page.showInSourceView( element, prop );
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
