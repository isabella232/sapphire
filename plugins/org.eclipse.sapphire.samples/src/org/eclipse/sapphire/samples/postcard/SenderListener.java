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

package org.eclipse.sapphire.samples.postcard;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.PropertyContentEvent;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.Value;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class SenderListener extends FilteredListener<PropertyContentEvent>
{
    @Override
    protected void handleTypedEvent( final PropertyContentEvent event )
    {
        final Value<?> property = (Value<?>) event.property();
        final Preferences prefs = InstanceScope.INSTANCE.getNode( "org.eclipse.sapphire.samples" ).node( "SendPostcardWizard" );
        
        prefs.put( property.name(), property.text( false ) );

        try
        {
            prefs.flush();
        }
        catch( BackingStoreException e )
        {
            Sapphire.service( LoggingService.class ).log( e );
        }
    }

}
