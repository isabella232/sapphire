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

package org.eclipse.sapphire.samples.postcard;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.services.DefaultValueService;
import org.eclipse.sapphire.services.DefaultValueServiceData;
import org.osgi.service.prefs.Preferences;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class SenderDefaultValueService extends DefaultValueService
{
    private String def;
    
    @Override
    protected void initDefaultValueService()
    {
        final Value<?> property = context( Value.class );
        final Preferences prefs = InstanceScope.INSTANCE.getNode( "org.eclipse.sapphire.samples" ).node( "SendPostcardWizard" );
        
        this.def = prefs.get( property.name(), null );
    }

    @Override
    protected DefaultValueServiceData compute()
    {
        return new DefaultValueServiceData( this.def );
    }

}
