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
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.MasterVersionCompatibilityService;
import org.eclipse.sapphire.Version;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.sapphire.services.ValidationService;

/**
 * An abstract implementation of ValidationService that produces a validation error when a property 
 * or an element is not compatible with the version compatibility target. 
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class VersionCompatibilityValidationService extends ValidationService
{
    private MasterVersionCompatibilityService versionCompatibilityService;
    private Listener versionCompatibilityServiceListener;
    
    @Override
    protected void init()
    {
        super.init();
        
        this.versionCompatibilityService = element().service( property(), MasterVersionCompatibilityService.class );
        
        this.versionCompatibilityServiceListener = new Listener()
        {
            @Override
            public void handle( final Event event )
            {
                broadcast();
            }
        };
        
        this.versionCompatibilityService.attach( this.versionCompatibilityServiceListener );
    }
    
    protected abstract IModelElement element();
    
    protected abstract ModelProperty property();
    
    protected boolean problem()
    {
        return ( ! this.versionCompatibilityService.compatible() );
    }

    @Override
    public Status validate()
    {
        if( problem() )
        {
            final String message;
            
            final Version version = this.versionCompatibilityService.version();
            final String versioned = this.versionCompatibilityService.versioned();
            
            if( version == null )
            {
                message = Resources.versionConstraintTargetNotFoundMessage;
            }
            else
            {
                message = Resources.bind( Resources.notCompatibleWithVersionMessage, version.toString(), versioned );
            }
            
            return Status.createErrorStatus( message );
        }
        
        return Status.createOkStatus();
    }
    
    @Override
    public void dispose()
    {
        super.dispose();
        
        if( this.versionCompatibilityService != null )
        {
            this.versionCompatibilityService.detach( this.versionCompatibilityServiceListener );
        }
    }
    
    private static final class Resources extends NLS
    {
        public static String notCompatibleWithVersionMessage;
        public static String versionConstraintTargetNotFoundMessage;
        
        static
        {
            initializeMessages( VersionCompatibilityValidationService.class.getName(), Resources.class );
        }
    }

}
