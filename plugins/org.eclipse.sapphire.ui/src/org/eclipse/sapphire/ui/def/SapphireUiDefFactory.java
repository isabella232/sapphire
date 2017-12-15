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

import java.net.URL;

import org.eclipse.sapphire.modeling.ClassLocator;
import org.eclipse.sapphire.modeling.LoggingService;
import org.eclipse.sapphire.modeling.ResourceLocator;
import org.eclipse.sapphire.modeling.ResourceStoreException;
import org.eclipse.sapphire.modeling.SharedModelsCache;
import org.eclipse.sapphire.modeling.UrlResourceStore;
import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.modeling.xml.XmlResourceStore;
import org.eclipse.sapphire.osgi.BundleResourceStore;

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
            LoggingService.log( e );
        }
        
        return null;
    }
    
    public static ISapphireUiDef load( final ISapphireUiDef context,
                                       final String path )
    {
        final ResourceLocator resourceLocator = context.adapt( ResourceLocator.class );
        final URL url = resourceLocator.find( path );
        
        if( url != null )
        {
            final ClassLocator classLocator = context.adapt( ClassLocator.class );
            
            try
            {
                final UrlResourceStore resourceStore = new UrlResourceStore( url )
                {
                    @Override
                    @SuppressWarnings( "unchecked" )
                    
                    public <A> A adapt( final Class<A> adapterType )
                    {
                        if( adapterType == ResourceLocator.class )
                        {
                            return (A) resourceLocator;
                        }
                        else if( adapterType == ClassLocator.class )
                        {
                            return (A) classLocator;
                        }
                        
                        return super.adapt( adapterType );
                    }
                };
                
                return load( new XmlResourceStore( resourceStore ), false );
            }
            catch( ResourceStoreException e )
            {
                LoggingService.log( e );
            }
        }
        
        return null;
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
        return (ISapphireCompositeDef) def.getPartDef( compositeId, true, ISapphireCompositeDef.class );
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
        return (ISapphireDialogDef) def.getPartDef( wizardId, true, ISapphireDialogDef.class );
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
        return (ISapphireWizardDef) def.getPartDef( wizardId, true, ISapphireWizardDef.class );
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
