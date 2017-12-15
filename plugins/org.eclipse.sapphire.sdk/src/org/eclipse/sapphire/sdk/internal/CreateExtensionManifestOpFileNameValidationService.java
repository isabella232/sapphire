/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation
 ******************************************************************************/

package org.eclipse.sapphire.sdk.internal;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.sapphire.sdk.extensibility.SapphireExtensionDef;
import org.eclipse.sapphire.services.ValidationService;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class CreateExtensionManifestOpFileNameValidationService extends ValidationService
{
    @Override
    public Status validate()
    {
        final Value<String> target = context( IModelElement.class ).read( context( ValueProperty.class ) );
        final String fileName = target.getText();
        
    	if( fileName != null && ! fileName.equals( SapphireExtensionDef.FILE_NAME ) )
    	{
            final String msg = NLS.bind( Resources.invalidFileName, fileName );
            return Status.createWarningStatus( msg );
    	}
        
        return Status.createOkStatus();
    }
    
    private static final class Resources extends NLS
    {
        public static String invalidFileName;
        
        static
        {
            initializeMessages( CreateExtensionManifestOpFileNameValidationService.class.getName(), Resources.class );
        }
    }
    
}
