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

package org.eclipse.sapphire.ui.forms.internal;

import org.eclipse.sapphire.DisposeEvent;
import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.PropertyContentEvent;
import org.eclipse.sapphire.Resource;
import org.eclipse.sapphire.modeling.ModelPath;
import org.eclipse.sapphire.modeling.xml.XmlResource;
import org.eclipse.sapphire.ui.Presentation;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.SourceEditorService;
import org.eclipse.sapphire.ui.def.ActionHandlerDef;
import org.eclipse.sapphire.ui.forms.MasterDetailsContentNodePart;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class OutlineNodeShowInSourceActionHandler extends SapphireActionHandler
{
    @Override
    public void init( final SapphireAction action, final ActionHandlerDef def ) 
    {
        super.init( action, def );
        
        final Element element = ( (MasterDetailsContentNodePart) getPart() ).getLocalModelElement();
        
        final Listener listener = new FilteredListener<PropertyContentEvent>()
        {
            @Override
            protected void handleTypedEvent( final PropertyContentEvent event )
            {
                refreshVisibility();
            }
        };
        
        element.attach( listener, ModelPath.ALL_DESCENDENTS );

        refreshVisibility();
        
        attach
        (
            new Listener()
            {
                @Override
                public void handle( final Event event )
                {
                    if( event instanceof DisposeEvent )
                    {
                        element.detach( listener, ModelPath.ALL_DESCENDENTS );
                    }
                }
            }
        );
    }

    private void refreshVisibility()
    {
        final MasterDetailsContentNodePart node = (MasterDetailsContentNodePart) getPart();
        final Element element = node.getLocalModelElement();
        final Resource resource = element.resource();
        
        setVisible( resource instanceof XmlResource && ( (XmlResource) resource ).getXmlElement() != null );
    }

    @Override
    protected Object run( final Presentation context )
    {
        final MasterDetailsContentNodePart node = (MasterDetailsContentNodePart) getPart();
        final Element element = node.getLocalModelElement();
        
        element.adapt( SourceEditorService.class ).show( element, null );
        
        return null;
    }
   
}
