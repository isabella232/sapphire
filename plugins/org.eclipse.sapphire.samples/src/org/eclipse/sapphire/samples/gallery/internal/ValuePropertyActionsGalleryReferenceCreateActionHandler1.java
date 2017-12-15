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
import org.eclipse.sapphire.samples.gallery.IValuePropertyActionsGallery;
import org.eclipse.sapphire.samples.gallery.IValuePropertyActionsGalleryEntity;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.def.ISapphireActionHandlerDef;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ValuePropertyActionsGalleryReferenceCreateActionHandler1

    extends ValuePropertyActionsGalleryReferenceCreateActionHandlerBase
    
{
    private ModelPropertyListener listener;
    
    @Override
    public void init( final SapphireAction action,
                      final ISapphireActionHandlerDef def )
    {
        super.init( action, def );
        
        this.listener = new ModelPropertyListener()
        {
            @Override
            public void handlePropertyChangedEvent( final ModelPropertyChangeEvent event )
            {
                refreshActionState();
            }
        };
        
        final IModelElement element = getModelElement();
        
        element.nearest( IValuePropertyActionsGallery.class ).addListener( this.listener, "Entities/*" );
        element.addListener( this.listener, getProperty().getName() );
        
        refreshActionState();
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

    @Override
    public void dispose()
    {
        super.dispose();
        
        final IModelElement element = getModelElement();
        
        element.nearest( IValuePropertyActionsGallery.class ).removeListener( this.listener, "Entities/*" );
        element.addListener( this.listener, getProperty().getName() );
    }
    
}
