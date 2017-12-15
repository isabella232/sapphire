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

package org.eclipse.sapphire.ui.def.internal;

import static org.eclipse.ui.plugin.AbstractUIPlugin.imageDescriptorFromPlugin;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.sapphire.ui.def.IImportDirective;
import org.eclipse.sapphire.ui.def.IPackageReference;
import org.osgi.framework.Bundle;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ImportDirectiveMethods
{
    public static Class<?> resolveClass( final IImportDirective directive,
                                         final String className )
    {
        final String bundleId = directive.getBundle().getText();
        
        if( bundleId != null )
        {
            final Bundle bundle = Platform.getBundle( bundleId );
            
            if( bundle != null )
            {
                for( IPackageReference packageRef : directive.getPackages() )
                {
                    final String packageName = packageRef.getName().getText();
                    
                    if( packageName != null )
                    {
                        final String fullClassName = packageName + "." + className;
                        
                        try
                        {
                            return bundle.loadClass( fullClassName );
                        }
                        catch( ClassNotFoundException e ) {}
                    }
                }
            }
        }
        
        return null;
    }

    public static Class<?> resolveClass( final String className,
                                         final String bundleId,
                                         final String... packageNames )
    {
        if( bundleId != null )
        {
            final Bundle bundle = Platform.getBundle( bundleId );
            
            if( bundle != null )
            {
                for( String packageName : packageNames )
                {
                    final String fullClassName = packageName + "." + className;
                    
                    try
                    {
                        return bundle.loadClass( fullClassName );
                    }
                    catch( ClassNotFoundException e ) {}
                }
            }
        }
        
        return null;
    }
    
    public static ImageDescriptor resolveImage( final IImportDirective directive,
                                                final String imagePath )
    {
        final String bundleId = directive.getBundle().getText();
        
        if( bundleId != null )
        {
            return imageDescriptorFromPlugin( bundleId, imagePath );
        }
        
        return null;
    }
    
}
