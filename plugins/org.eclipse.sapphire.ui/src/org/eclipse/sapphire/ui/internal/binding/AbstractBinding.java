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

package org.eclipse.sapphire.ui.internal.binding;

import java.util.Collection;

import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.EditFailedException;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.PropertyEvent;
import org.eclipse.sapphire.ui.PropertyEditorPart;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.internal.SapphireUiFrameworkPlugin;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Control;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class AbstractBinding
{
    private PropertyEditorPart editor;
    private Listener propertyChangeListener;
    private SapphireRenderingContext context;
    private Control control;
    
    public AbstractBinding( final PropertyEditorPart editor,
                            final SapphireRenderingContext context,
                            final Control control )
    {
        this.editor = editor;
        this.context = context;
        this.control = control;

        this.propertyChangeListener = new FilteredListener<PropertyEvent>()
        {
            @Override
            protected void handleTypedEvent( final PropertyEvent event )
            {
                updateTarget();
            }
        };

        getModelElement().attach( this.propertyChangeListener, this.editor.getProperty().getName() );
        
        this.control.addDisposeListener
        (
            new DisposeListener()
            {
                public void widgetDisposed( final DisposeEvent event )
                {
                    dispose();
                }
            }
        );
        
        initialize( editor, context, control );
        updateTarget();
    }
    
    public final IModelElement getModelElement()
    {
        return this.editor.getLocalModelElement();
    }
    
    public ModelProperty getProperty()
    {
        return this.editor.getProperty();
    }
    
    public Object getPropertyValue()
    {
        return getModelElement().read( getProperty() );
    }
    
    public final SapphireRenderingContext getContext()
    {
        return this.context;
    }

    public final Control getControl()
    {
        return this.control;
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
                SapphireUiFrameworkPlugin.log( e );
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
            SapphireUiFrameworkPlugin.log( e );
        }
    }
    
    public final void updateTargetAttributes()
    {
        if( ! this.control.isDisposed() ) 
        {
            final boolean enabled = getModelElement().enabled( getProperty() );
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
    
    public void dispose()
    {
        getModelElement().detach( this.propertyChangeListener, getProperty().getName() );            
    }
    
    protected void initialize( PropertyEditorPart editor,
                               SapphireRenderingContext context,
                               Control control )
    {
        
    }

    protected abstract void doUpdateTarget();
    protected abstract void doUpdateModel();
    
}