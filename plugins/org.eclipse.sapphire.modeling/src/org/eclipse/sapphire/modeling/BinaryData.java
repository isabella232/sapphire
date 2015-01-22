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

package org.eclipse.sapphire.modeling;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public class BinaryData
{
    private final byte[] data;
    
    public BinaryData( final InputStream stream ) throws IOException
    {
        final ByteArrayOutputStream buf = new ByteArrayOutputStream();
        final byte[] bytes = new byte[ 1024 ];
        
        for( int count = stream.read( bytes ); count != -1; count = stream.read( bytes ) )
        {
            buf.write( bytes, 0, count );
        }
        
        this.data = buf.toByteArray();
    }
    
    public BinaryData( final byte[] data )
    {
        this.data = new byte[ data.length ];
        System.arraycopy( data, 0, this.data, 0, data.length );
    }
    
    public InputStream contents()
    {
        return new ByteArrayInputStream( this.data );
    }

    @Override
    public boolean equals( final Object obj )
    {
        if( obj instanceof BinaryData )
        {
            return Arrays.equals( this.data, ( (BinaryData) obj ).data );
        }
        
        return false;
    }

    @Override
    public int hashCode()
    {
        return Arrays.hashCode( this.data );
    }
    
}
