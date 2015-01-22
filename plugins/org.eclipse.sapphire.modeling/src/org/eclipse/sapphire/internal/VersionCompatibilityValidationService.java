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

package org.eclipse.sapphire.internal;

import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.MasterVersionCompatibilityService;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.Version;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.services.ValidationService;

/**
 * An implementation of ValidationService that produces a validation error when a property 
 * is not compatible with the version compatibility target yet contains data. 
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class VersionCompatibilityValidationService extends ValidationService
{
    @Text( "Not compatible with version {0} of {1}" )
    private static LocalizableText notCompatibleWithVersionMessage;
    
    @Text( "Version constraint exists, but no version constraint target was found" )
    private static LocalizableText versionConstraintTargetNotFoundMessage;
    
    static
    {
        LocalizableText.init( VersionCompatibilityValidationService.class );
    }

    private MasterVersionCompatibilityService versionCompatibilityService;
    private Listener versionCompatibilityServiceListener;
    
    @Override
    protected void initValidationService()
    {
        this.versionCompatibilityService = context( Property.class ).service( MasterVersionCompatibilityService.class );
        
        this.versionCompatibilityServiceListener = new Listener()
        {
            @Override
            public void handle( final Event event )
            {
                refresh();
            }
        };
        
        this.versionCompatibilityService.attach( this.versionCompatibilityServiceListener );
    }
    
    @Override
    protected Status compute()
    {
        if( ! this.versionCompatibilityService.compatible() && ! context( Property.class ).empty() )
        {
            final String message;
            
            final Version version = this.versionCompatibilityService.version();
            final String versioned = this.versionCompatibilityService.versioned();
            
            if( version == null )
            {
                message = versionConstraintTargetNotFoundMessage.text();
            }
            else
            {
                message = notCompatibleWithVersionMessage.format( version.toString(), versioned );
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

}
