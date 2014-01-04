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

package org.eclipse.sapphire.tests.services.t0010;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.Version;
import org.eclipse.sapphire.VersionCompatibilityService;
import org.eclipse.sapphire.VersionCompatibilityTargetService;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TestVersionCompatibilityService extends VersionCompatibilityService
{
    private VersionCompatibilityTargetService versionCompatibilityTargetService;
    private Listener versionCompatibilityTargetServiceListener;
    private Version min;
    
    protected void initVersionCompatibilityService()
    {
        this.versionCompatibilityTargetService = VersionCompatibilityTargetService.find( context( Element.class ), context( PropertyDef.class ) );
        
        this.versionCompatibilityTargetServiceListener = new Listener()
        {
            @Override
            public void handle( final Event event )
            {
                refresh();
            }
        };
        
        this.versionCompatibilityTargetService.attach( this.versionCompatibilityTargetServiceListener );
    }
    
    public void update( final Version min )
    {
        this.min = min;
        refresh();
    }
    
    public void update( final String min )
    {
        update( new Version( min ) );
    }

    @Override
    protected Data compute()
    {
        final Version version = this.versionCompatibilityTargetService.version();
        final String versioned = this.versionCompatibilityTargetService.versioned();
        final boolean compatible;

        if( this.min == null )
        {
            compatible = true;
        }
        else
        {
            if( version == null )
            {
                compatible = false;
            }
            else
            {
                compatible = version.compareTo( this.min ) > 0;
            }
        }
        
        return new Data( compatible, version, versioned );
    }

    @Override
    public void dispose()
    {
        super.dispose();
        
        if( this.versionCompatibilityTargetService != null )
        {
            this.versionCompatibilityTargetService.detach( this.versionCompatibilityTargetServiceListener );
        }
    }
    
}
