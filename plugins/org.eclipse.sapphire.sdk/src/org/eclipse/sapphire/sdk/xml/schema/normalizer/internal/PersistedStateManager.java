/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation
 ******************************************************************************/

package org.eclipse.sapphire.sdk.xml.schema.normalizer.internal;

import static org.eclipse.sapphire.modeling.util.MiscUtil.createStringDigest;

import java.io.File;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.sapphire.modeling.FileResourceStore;
import org.eclipse.sapphire.modeling.Path;
import org.eclipse.sapphire.modeling.ResourceStoreException;
import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.modeling.xml.XmlResource;
import org.eclipse.sapphire.modeling.xml.XmlResourceStore;
import org.eclipse.sapphire.sdk.xml.schema.normalizer.CreateNormalizedXmlSchemaOp;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class PersistedStateManager
{
    public static final PersistedState load( final Path sourceSchemaFile  )
    {
        if( sourceSchemaFile != null )
        {
            final String digest = createStringDigest( sourceSchemaFile.toPortableString() );
            
            File file = ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile();
            file = new File( file, ".metadata/.plugins/org.eclipse.sapphire.sdk" );
            file = new File( file, CreateNormalizedXmlSchemaOp.class.getSimpleName() );
            file = new File( file, digest );
            
            try
            {
                final FileResourceStore fileResourceStore = new FileResourceStore( file );
                final XmlResourceStore xmlResourceStore = new XmlResourceStore( fileResourceStore );
                final XmlResource xmlResource = new RootXmlResource( xmlResourceStore );
                
                return PersistedState.TYPE.instantiate( xmlResource );
            }
            catch( ResourceStoreException e )
            {
                // Fall through and return no persisted state.
            }
        }
        
        return null;
    }
    
}
