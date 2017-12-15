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

package org.eclipse.sapphire.ui.renderers.swt;

import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.PropertyInitializationEvent;
import org.eclipse.sapphire.ui.PropertyEditorPart;
import org.eclipse.sapphire.ui.SapphireRenderingContext;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class ListPropertyEditorRenderer extends PropertyEditorRenderer
{
    private Listener listElementListener;

    public ListPropertyEditorRenderer( final SapphireRenderingContext context,
                                       final PropertyEditorPart part )
    {
        super( context, part );
        
        this.listElementListener = new Listener()
        {
            @Override
            public void handle( final Event event )
            {
                if( ! ( event instanceof PropertyInitializationEvent ) )
                {
                    handleListElementChangedEvent( event );
                }
            }
        };
        
        attachListElementListener();
        
        addOnDisposeOperation
        (
            new Runnable()
            {
                public void run()
                {
                    final ModelElementList<IModelElement> list = getList();

                    if( list != null )
                    {
                        for( IModelElement entry : list )
                        {
                            entry.detach( ListPropertyEditorRenderer.this.listElementListener );
                        }
                    }
                }
            }
        );
    }

    @Override
    public ListProperty getProperty()
    {
        return (ListProperty) super.getProperty();
    }

    public final ModelElementList<IModelElement> getList()
    {
        final IModelElement modelElement = getModelElement();
        
        if( modelElement != null )
        {
            return modelElement.read( getProperty() );
        }
        
        return null;
    }
    
    @Override
    protected void handlePropertyChangedEvent()
    {
        super.handlePropertyChangedEvent();
        attachListElementListener();
    }

    protected void handleListElementChangedEvent( final Event event )
    {
    }
    
    private void attachListElementListener()
    {
        final ModelElementList<IModelElement> list = getList();
        
        if( list != null )
        {
            for( IModelElement entry : list )
            {
                entry.attach( this.listElementListener );
            }
        }
    }
    
}
