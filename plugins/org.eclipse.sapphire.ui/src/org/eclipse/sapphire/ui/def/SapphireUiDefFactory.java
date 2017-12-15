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

package org.eclipse.sapphire.ui.def;

import org.eclipse.sapphire.modeling.BundleResourceStore;
import org.eclipse.sapphire.modeling.ResourceStoreException;
import org.eclipse.sapphire.modeling.SharedModelsCache;
import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.modeling.xml.XmlResourceStore;
import org.eclipse.sapphire.ui.internal.SapphireUiFrameworkPlugin;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SapphireUiDefFactory
{
    public static ISapphireUiDef load( final String bundleId,
                                       final String path )
    {
        try
        {
            return load( new XmlResourceStore( new BundleResourceStore( bundleId, path ) ), false );
        }
        catch( ResourceStoreException e )
        {
            SapphireUiFrameworkPlugin.log( e );
            return null;
        }
    }
    
    public static ISapphireUiDef load( final XmlResourceStore resourceStore,
                                       final boolean writable )
    {
        ISapphireUiDef model;
        
        if( writable )
        {
            model = ISapphireUiDef.TYPE.instantiate( new RootXmlResource( resourceStore ) );
        }
        else
        {
            final SharedModelsCache.StandardKey key = new SharedModelsCache.StandardKey( resourceStore, ISapphireUiDef.TYPE );
            
            model = (ISapphireUiDef) SharedModelsCache.retrieve( key );
            
            if( model == null )
            {
                model = ISapphireUiDef.TYPE.instantiate( new RootXmlResource( resourceStore ) );
                SharedModelsCache.store( key, model );
            }
        }
        
        return model;
    }
    
    public static ISapphireCompositeDef getCompositeDef( final String path )
    {
        final String[] segments = parseDefPath( path );
        return getCompositeDef( segments[ 0 ], segments[ 1 ], segments[ 2 ] );
    }
    
    public static ISapphireCompositeDef getCompositeDef( final String bundleId,
                                                         final String defFilePath,
                                                         final String compositeId )
    {
        final ISapphireUiDef def = SapphireUiDefFactory.load( bundleId, defFilePath );
        return def.getCompositeDef( compositeId, true );
    }
    
    public static ISapphireDialogDef getDialogDef( final String path )
    {
        final String[] segments = parseDefPath( path );
        return getDialogDef( segments[ 0 ], segments[ 1 ], segments[ 2 ] );
    }
    
    public static ISapphireDialogDef getDialogDef( final String bundleId,
                                                   final String defFilePath,
                                                   final String wizardId )
    {
        final ISapphireUiDef def = SapphireUiDefFactory.load( bundleId, defFilePath );
        return def.getDialogDef( wizardId, true );
    }
    
    public static ISapphireWizardDef getWizardDef( final String path )
    {
        final String[] segments = parseDefPath( path );
        return getWizardDef( segments[ 0 ], segments[ 1 ], segments[ 2 ] );
    }
    
    public static ISapphireWizardDef getWizardDef( final String bundleId,
                                                   final String defFilePath,
                                                   final String wizardId )
    {
        final ISapphireUiDef def = SapphireUiDefFactory.load( bundleId, defFilePath );
        return def.getWizardDef( wizardId, true );
    }
    
    private static String[] parseDefPath( final String path )
    {
        final int firstSeparator = path.indexOf( '/' );
        final int secondSeparator = path.lastIndexOf( '!' );
        final int lastIndex = path.length() - 1;
        
        if( firstSeparator <= 0 || firstSeparator >= lastIndex ||
            secondSeparator <= 0 || secondSeparator >= lastIndex ||
            firstSeparator > secondSeparator )
        {
            throw new IllegalArgumentException();
        }

        final String bundleId = path.substring( 0, firstSeparator );
        final String defFilePath = path.substring( firstSeparator + 1, secondSeparator );
        final String compositeId = path.substring( secondSeparator + 1 );
        
        return new String[] { bundleId, defFilePath, compositeId };
    }
    
}
