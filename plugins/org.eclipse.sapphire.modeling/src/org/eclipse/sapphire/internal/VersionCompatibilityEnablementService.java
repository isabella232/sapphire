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

package org.eclipse.sapphire.internal;

import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.MasterVersionCompatibilityService;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ImpliedElementProperty;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.PropertyContentEvent;
import org.eclipse.sapphire.services.EnablementService;
import org.eclipse.sapphire.services.EnablementServiceData;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ServiceFactory;

/**
 * Implementation of EnablementService that determines property's enablement state based on property's 
 * compatibility with the version compatibility target.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class VersionCompatibilityEnablementService extends EnablementService
{
    private MasterVersionCompatibilityService versionCompatibilityService;
    private Listener versionCompatibilityServiceListener;
    private Listener propertyListener;
    
    @Override
    protected void initEnablementService()
    {
        final IModelElement element = context( IModelElement.class );
        final ModelProperty property = context( ModelProperty.class );
        
        this.versionCompatibilityService = element.service( property, MasterVersionCompatibilityService.class );
        
        this.versionCompatibilityServiceListener = new Listener()
        {
            @Override
            public void handle( final Event event )
            {
                refresh();
            }
        };
        
        this.versionCompatibilityService.attach( this.versionCompatibilityServiceListener );
        
        this.propertyListener = new FilteredListener<PropertyContentEvent>()
        {
            @Override
            protected void handleTypedEvent( final PropertyContentEvent event )
            {
                refresh();
            }
        };
        
        if( property instanceof ImpliedElementProperty )
        {
            element.attach( this.propertyListener, property.getName() + "/*" );
        }
        else
        {
            element.attach( this.propertyListener, property.getName() );
        }
    }

    @Override
    protected EnablementServiceData compute()
    {
        final IModelElement element = context( IModelElement.class );
        final ModelProperty property = context( ModelProperty.class );
        
        return new EnablementServiceData( this.versionCompatibilityService.compatible() || ! element.empty( property ) );
    }
    
    @Override
    public void dispose()
    {
        super.dispose();
        
        final IModelElement element = context( IModelElement.class );
        final ModelProperty property = context( ModelProperty.class );
        
        this.versionCompatibilityService.detach( this.versionCompatibilityServiceListener );
        
        if( property instanceof ImpliedElementProperty )
        {
            element.detach( this.propertyListener, property.getName() + "/*" );
        }
        else
        {
            element.detach( this.propertyListener, property.getName() );
        }
    }
    
    public static final class Factory extends ServiceFactory
    {
        @Override
        public boolean applicable( final ServiceContext context,
                                   final Class<? extends Service> service )
        {
            return true;
        }

        @Override
        public Service create( final ServiceContext context,
                               final Class<? extends Service> service )
        {
            return new VersionCompatibilityEnablementService();
        }
    }

}
