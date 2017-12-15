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

package org.eclipse.sapphire.samples.gallery.internal;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelPropertyChangeEvent;
import org.eclipse.sapphire.modeling.ModelPropertyListener;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphirePropertyEditorActionHandler;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.def.ISapphireActionHandlerDef;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class PrefixActionHandlerForValue

    extends SapphirePropertyEditorActionHandler
    
{
    private String prefix;
    private ModelPropertyListener listener;
    
    @Override
    public void init( final SapphireAction action,
                      final ISapphireActionHandlerDef def )
    {
        super.init( action, def );
        
        this.prefix = def.getParam( "prefix" ) + " ";
        
        this.listener = new ModelPropertyListener()
        {
            @Override
            public void handlePropertyChangedEvent( final ModelPropertyChangeEvent event )
            {
                refreshEnabledState();
                refreshCheckedState();
            }
        };
        
        getModelElement().addListener( this.listener, getProperty().getName() );
        
        refreshEnabledState();
        refreshCheckedState();
    }
    
    @Override
    public ValueProperty getProperty()
    {
        return (ValueProperty) super.getProperty();
    }

    @Override
    protected Object run( final SapphireRenderingContext context )
    {
        final IModelElement element = getModelElement();
        final ValueProperty property = getProperty();
        final String oldValue = element.read( property ).getText();
        final String newValue;
        
        if( isChecked() )
        {
            newValue = this.prefix + oldValue;
        }
        else
        {
            if( oldValue.startsWith( this.prefix ) )
            {
                if( oldValue.length() == this.prefix.length() )
                {
                    newValue = null;
                }
                else
                {
                    newValue = oldValue.substring( this.prefix.length() );
                }
            }
            else
            {
                newValue = oldValue;
            }
        }
        
        element.write( property, newValue );
        
        return null;
    }
    
    private void refreshEnabledState()
    {
        final String value = getModelElement().read( getProperty() ).getText();
        setEnabled( value != null );
    }

    private void refreshCheckedState()
    {
        final String value = getModelElement().read( getProperty() ).getText();
        setChecked( value != null && value.startsWith( this.prefix ) );
    }

    @Override
    public void dispose()
    {
        super.dispose();
        
        getModelElement().removeListener( this.listener, getProperty().getName() );
    }
    
}
