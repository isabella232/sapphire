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

package org.eclipse.sapphire.ui.forms.swt;

import java.util.List;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.PropertyContentEvent;
import org.eclipse.sapphire.modeling.ModelPath;
import org.eclipse.sapphire.ui.forms.FormComponentPart;
import org.eclipse.swt.widgets.Composite;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class ListPropertyEditorPresentation extends PropertyEditorPresentation
{
    private Listener listElementListener;

    public ListPropertyEditorPresentation( final FormComponentPart part, final SwtPresentation parent, final Composite composite )
    {
        super( part, parent, composite );
        
        this.listElementListener = new FilteredListener<PropertyContentEvent>()
        {
            @Override
            protected void handleTypedEvent( final PropertyContentEvent event )
            {
                handleChildPropertyEvent( event );
            }
        };
        
        attachListElementListener();
        
        addOnDisposeOperation
        (
            new Runnable()
            {
                public void run()
                {
                    final ElementList<?> property = property();
                    
                    if( ! property.disposed() )
                    {
                        final List<ModelPath> childPropertyPaths = part().getChildProperties();
                        
                        for( final Element entry : property )
                        {
                            for( final ModelPath childPropertyPath : childPropertyPaths )
                            {
                                entry.detach( ListPropertyEditorPresentation.this.listElementListener, childPropertyPath );
                            }
                        }
                    }
                }
            }
        );
    }
    
    @Override
    public ElementList<?> property()
    {
        return (ElementList<?>) super.property();
    }
    
    public ElementList<?> list()
    {
        return property();
    }
    
    @Override
    protected void handlePropertyChangedEvent()
    {
        super.handlePropertyChangedEvent();
        attachListElementListener();
    }

    protected void handleChildPropertyEvent( final PropertyContentEvent event )
    {
    }
    
    private void attachListElementListener()
    {
        final List<ModelPath> childPropertyPaths = part().getChildProperties();
        
        for( final Element entry : property() )
        {
            for( final ModelPath childPropertyPath : childPropertyPaths )
            {
                entry.attach( this.listElementListener, childPropertyPath );
            }
        }
    }
    
}
