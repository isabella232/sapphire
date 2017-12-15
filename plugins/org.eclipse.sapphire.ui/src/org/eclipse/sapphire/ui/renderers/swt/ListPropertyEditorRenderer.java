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

package org.eclipse.sapphire.ui.renderers.swt;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementListener;
import org.eclipse.sapphire.modeling.ModelPropertyChangeEvent;
import org.eclipse.sapphire.ui.SapphirePropertyEditor;
import org.eclipse.sapphire.ui.SapphireRenderingContext;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class ListPropertyEditorRenderer

    extends PropertyEditorRenderer
    
{
    private ModelElementListener listElementListener;

    public ListPropertyEditorRenderer( final SapphireRenderingContext context,
                                       final SapphirePropertyEditor part )
    {
        super( context, part );
        
        this.listElementListener = new ModelElementListener()
        {
            @Override
            public void propertyChanged( final ModelPropertyChangeEvent event )
            {
                handleListElementChangedEvent( event );
            }
        };
        
        attachListElementListener();
    }

    @Override
    public ListProperty getProperty()
    {
        return (ListProperty) super.getProperty();
    }

    @SuppressWarnings("unchecked")
    public final ModelElementList<IModelElement> getList()
    {
        final IModelElement modelElement = getModelElement();
        
        if( modelElement != null )
        {
            return (ModelElementList<IModelElement>) getProperty().invokeGetterMethod( modelElement );
        }
        
        return null;
    }
    
    @Override
    protected void handlePropertyChangedEvent()
    {
        super.handlePropertyChangedEvent();
        attachListElementListener();
    }

    protected void handleListElementChangedEvent( final ModelPropertyChangeEvent event )
    {
    }
    
    @Override
    protected void handleDisposeEvent()
    {
        final ModelElementList<IModelElement> list = getList();

        if( list != null )
        {
            for( IModelElement entry : list )
            {
                entry.removeListener( this.listElementListener );
            }
        }
    }
    
    private void attachListElementListener()
    {
        final ModelElementList<IModelElement> list = getList();
        
        if( list != null )
        {
            for( IModelElement entry : list )
            {
                entry.addListener( this.listElementListener );
            }
        }
    }
    
}
