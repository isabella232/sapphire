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

package org.eclipse.sapphire.ui.def.internal;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.sapphire.Context;
import org.eclipse.sapphire.ConversionService;
import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ImageData;
import org.eclipse.sapphire.ui.def.IPackageReference;
import org.eclipse.sapphire.ui.def.ISapphireUiDef;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class StringToImageDataConversionService extends ConversionService<String,ImageData>
{
    public StringToImageDataConversionService()
    {
        super( String.class, ImageData.class );
    }

    @Override
    public ImageData convert( final String string )
    {
        final Element element = context( Element.class );
        final Context ctxt = element.adapt( Context.class );
        
        InputStream stream = null;
        
        if( string != null && ! string.contains( "/" ) )
        {
            final ISapphireUiDef sdef = element.nearest( ISapphireUiDef.class );
            
            if( sdef != null )
            {
                for( IPackageReference p : sdef.getImportedPackages() )
                {
                    final String pname = p.getName().content();
                    
                    if( pname != null )
                    {
                        final String possibleFullPath = pname.replace( '.', '/' ) + "/" + string;
                        stream = ctxt.findResource( possibleFullPath );
                        
                        if( stream != null )
                        {
                            break;
                        }
                    }
                }
            }
        }
        
        if( stream == null )
        {
            stream = ctxt.findResource( string );
        }
        
        if( stream != null )
        {
            try
            {
                return ImageData.readFromStream( stream ).optional();
            }
            finally
            {
                try
                {
                    stream.close();
                }
                catch( IOException e ) {}
            }
        }
        
        return null;
    }

}
