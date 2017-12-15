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

package org.eclipse.sapphire.ui.internal.binding;

import java.util.Collection;

import org.eclipse.sapphire.modeling.EditFailedException;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ModelPropertyChangeEvent;
import org.eclipse.sapphire.modeling.ModelPropertyListener;
import org.eclipse.sapphire.ui.SapphirePropertyEditor;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.assist.internal.PropertyEditorAssistDecorator;
import org.eclipse.sapphire.ui.internal.SapphireUiFrameworkPlugin;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Control;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class AbstractBinding
{
    private IModelElement modelElement;
    private ModelProperty property;
    private ModelPropertyListener propertyChangeListener;
    private SapphireRenderingContext context;
    private Control control;
    
    public AbstractBinding( final IModelElement modelElement,
                            final ModelProperty property,
                            final SapphireRenderingContext context,
                            final Control control )
    {
        this.modelElement = modelElement;
        this.property = property;
        this.context = context;
        this.control = control;

        this.propertyChangeListener = new ModelPropertyListener()
        {
            @Override
            public void handlePropertyChangedEvent( final ModelPropertyChangeEvent event )
            {
                updateTarget();
            }
        };

        if( this.modelElement != null )
        {
            this.modelElement.addListener( this.propertyChangeListener, this.property.getName() );
        }
        
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
        
        initialize( modelElement, property, context, control );
        updateTarget();
    }
    
    public final IModelElement getModelElement()
    {
        return this.modelElement;
    }
    
    public final void setModelElement( final IModelElement modelElement )
    {
        if( this.modelElement != null )
        {
            this.modelElement.removeListener( this.propertyChangeListener, this.property.getName() );
        }
        
        this.modelElement = modelElement;
        
        if( this.modelElement != null )
        {
            this.modelElement.addListener( this.propertyChangeListener, this.property.getName() );
        }
        
        updateTarget();
    }
    
    public ModelProperty getProperty()
    {
        return this.property;
    }
    
    public Object getPropertyValue()
    {
        return this.property.invokeGetterMethod( this.modelElement );
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
        if( this.modelElement == null )
        {
            return;
        }
        
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
        if (this.control.isDisposed()) {
            return;
        }
        final PropertyEditorAssistDecorator dec 
            = (PropertyEditorAssistDecorator) this.control.getData( SapphirePropertyEditor.DATA_ASSIST_DECORATOR );
        
        if( dec != null )
        {
            dec.refresh();
        }
        
        final boolean enabled 
            = ( this.modelElement == null ? false : this.modelElement.isPropertyEnabled( this.property ) );
        
        this.control.setEnabled( enabled );
        
        final Object relatedControls = this.control.getData( SapphirePropertyEditor.RELATED_CONTROLS );
        
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
    
    public void dispose()
    {
        if( this.modelElement != null )
        {
            this.modelElement.removeListener( this.propertyChangeListener, this.property.getName() );            
        }
    }
    
    protected void initialize( IModelElement modelElement,
                               ModelProperty property,
                               SapphireRenderingContext context,
                               Control control )
    {
        
    }

    protected abstract void doUpdateTarget();
    protected abstract void doUpdateModel();
    
}