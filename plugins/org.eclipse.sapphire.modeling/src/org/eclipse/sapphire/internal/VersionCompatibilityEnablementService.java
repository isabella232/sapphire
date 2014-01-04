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

package org.eclipse.sapphire.internal;

import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.ImpliedElementProperty;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.MasterVersionCompatibilityService;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.PropertyContentEvent;
import org.eclipse.sapphire.services.EnablementService;
import org.eclipse.sapphire.services.EnablementServiceData;

/**
 * Implementation of EnablementService that determines property's enablement state based on property's 
 * compatibility with the version compatibility target.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class VersionCompatibilityEnablementService extends EnablementService
{
    private MasterVersionCompatibilityService versionCompatibilityService;
    private Listener versionCompatibilityServiceListener;
    private Listener propertyListener;
    
    @Override
    protected void initEnablementService()
    {
        final Property property = context( Property.class );
        
        this.versionCompatibilityService = property.service( MasterVersionCompatibilityService.class );
        
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
        
        if( property.definition() instanceof ImpliedElementProperty )
        {
            property.element().attach( this.propertyListener, property.name() + "/*" );
        }
        else
        {
            property.element().attach( this.propertyListener, property.name() );
        }
    }

    @Override
    protected EnablementServiceData compute()
    {
        return new EnablementServiceData( this.versionCompatibilityService.compatible() || ! context( Property.class ).empty() );
    }
    
    @Override
    public void dispose()
    {
        super.dispose();
        
        final Property property = context( Property.class );
        
        this.versionCompatibilityService.detach( this.versionCompatibilityServiceListener );
        
        if( property.definition() instanceof ImpliedElementProperty )
        {
            property.element().detach( this.propertyListener, property.name() + "/*" );
        }
        else
        {
            property.element().detach( this.propertyListener, property.name() );
        }
    }

}
