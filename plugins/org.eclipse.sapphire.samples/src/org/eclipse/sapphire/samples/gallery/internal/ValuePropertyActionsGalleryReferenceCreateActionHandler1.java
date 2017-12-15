/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.samples.gallery.internal;

import org.eclipse.sapphire.DisposeEvent;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelPropertyChangeEvent;
import org.eclipse.sapphire.modeling.ModelPropertyListener;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.samples.gallery.IValuePropertyActionsGallery;
import org.eclipse.sapphire.samples.gallery.IValuePropertyActionsGalleryEntity;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.def.ISapphireActionHandlerDef;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ValuePropertyActionsGalleryReferenceCreateActionHandler1 extends ValuePropertyActionsGalleryReferenceCreateActionHandlerBase
{
    @Override
    public void init( final SapphireAction action,
                      final ISapphireActionHandlerDef def )
    {
        super.init( action, def );
        
        final ModelPropertyListener listener = new ModelPropertyListener()
        {
            @Override
            public void handlePropertyChangedEvent( final ModelPropertyChangeEvent event )
            {
                refreshActionState();
            }
        };
        
        final IModelElement element = getModelElement();
        final IValuePropertyActionsGallery gallery = element.nearest( IValuePropertyActionsGallery.class );
        final String propertyName = getProperty().getName();
        
        gallery.addListener( listener, "Entities/*" );
        element.addListener( listener, propertyName );
        
        refreshActionState();
        
        attach
        (
            new Listener()
            {
                @Override
                public void handle( final Event event )
                {
                    if( event instanceof DisposeEvent )
                    {
                        gallery.removeListener( listener, "Entities/*" );
                        element.addListener( listener, propertyName );
                    }
                }
            }
        );
    }
    
    private void refreshActionState()
    {
        final String entityName = getModelElement().read( (ValueProperty) getProperty() ).getText();
        
        final boolean newEnablementState = ( entityName != null && ! isEntityDefined( entityName ) );
        setEnabled( newEnablementState );
        
        final String newLabel = ( entityName == null ? "create" : "create " + entityName );
        setLabel( newLabel );
    }

    @Override
    protected Object run( final SapphireRenderingContext context )
    {
        final IModelElement element = getModelElement();
        final String entityName = element.read( (ValueProperty) getProperty() ).getText();
        
        if( entityName != null && ! isEntityDefined( entityName ) )
        {
            final IValuePropertyActionsGalleryEntity entity = element.nearest( IValuePropertyActionsGallery.class ).getEntities().addNewElement();
            entity.setName( entityName );
        }
        
        return null;
    }
    
}
