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

package org.eclipse.sapphire.ui.forms.swt;

import java.util.Collection;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.PropertyEvent;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.modeling.EditFailedException;
import org.eclipse.sapphire.ui.forms.PropertyEditorPart;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Control;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class AbstractBinding
{
    private PropertyEditorPresentation propertyEditorPresentation;
    private Control control;
    
    public AbstractBinding( final PropertyEditorPresentation propertyEditorPresentation,
                            final Control control )
    {
        this.propertyEditorPresentation = propertyEditorPresentation;
        this.control = control;

        final Property property = propertyEditorPresentation.property();
        
        final Listener propertyChangeListener = new FilteredListener<PropertyEvent>()
        {
            @Override
            protected void handleTypedEvent( final PropertyEvent event )
            {
                updateTarget();
            }
        };
        
        property.attach( propertyChangeListener );
        
        this.control.addDisposeListener
        (
            new DisposeListener()
            {
                public void widgetDisposed( final DisposeEvent event )
                {
                    property.detach( propertyChangeListener );
                }
            }
        );
        
        initialize( this.propertyEditorPresentation, control );
        updateTarget();
    }
    
    public final PropertyEditorPresentation presentation()
    {
        return this.propertyEditorPresentation;
    }
    
    public final PropertyEditorPart part()
    {
        return this.propertyEditorPresentation.part();
    }
    
    public Property property()
    {
        return this.propertyEditorPresentation.property();
    }
    
    public final Element element()
    {
        return this.propertyEditorPresentation.property().element();
    }
    
    public final void updateModel()
    {
        boolean rollback = false;
        
        try
        {
            doUpdateModel();
        }
        catch( Exception e )
        {
            final EditFailedException editFailedException = EditFailedException.findAsCause( e );
            
            if( editFailedException != null )
            {
                rollback = true;
            }
            else
            {
                Sapphire.service( LoggingService.class ).log( e );
            }
        }
    
        if( rollback )
        {
            updateTarget();
        }
    }

    public final void updateTarget()
    {
        if( this.control != null )
        {
            if( this.control.isDisposed() )
            {
                return;
            }
            
            if( this.control.getDisplay().getThread() != Thread.currentThread() )
            {
                this.control.getDisplay().asyncExec
                (
                    new Runnable()
                    {
                        public void run()
                        {
                            updateTarget();
                        }
                    }
                );
                
                return;
            }
        }
        
        try
        {
            doUpdateTarget();
            updateTargetAttributes();
        }
        catch( Exception e )
        {
            Sapphire.service( LoggingService.class ).log( e );
        }
    }
    
    public final void updateTargetAttributes()
    {
        if( ! this.control.isDisposed() ) 
        {
            final boolean enabled = this.propertyEditorPresentation.property().enabled();
            this.control.setEnabled( enabled );
            
            final Object relatedControls = this.control.getData( PropertyEditorPart.RELATED_CONTROLS );
            
            if( relatedControls != null )
            {
                if( relatedControls instanceof Control )
                {
                    ( (Control) relatedControls ).setEnabled( enabled );
                }
                else if( relatedControls instanceof Collection<?> )
                {
                    for( Object control : (Collection<?>) relatedControls )
                    {
                        if( control != null )
                        {
                            ( (Control) control ).setEnabled( enabled );
                        }
                    }
                }
            }
        }
    }
    
    protected void initialize( PropertyEditorPresentation propertyEditorPresentation,
                               Control control )
    {
        
    }

    protected abstract void doUpdateTarget();
    protected abstract void doUpdateModel();
    
}