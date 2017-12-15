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

package org.eclipse.sapphire.modeling.xml.schema;

import org.eclipse.sapphire.modeling.LoggingService;
import org.eclipse.wst.common.uriresolver.internal.provisional.URIResolver;
import org.eclipse.wst.common.uriresolver.internal.provisional.URIResolverPlugin;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@SuppressWarnings( "restriction" )

public final class UrlResolver
{
    public static final String resolve( final String baseLocation,
                                         final String url )
    {
        final URIResolver idResolver = URIResolverPlugin.createResolver();

        String resolvedUrl = null;
        
        try
        {
            resolvedUrl = idResolver.resolve( baseLocation, null, url );
        }
        catch( Exception e )
        {
            LoggingService.log( e );
        }
         
        if( resolvedUrl == null )
        {
            resolvedUrl = url;
        }
        
        return resolvedUrl;
    }
    
}
