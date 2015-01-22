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

import static org.eclipse.sapphire.util.StringUtil.UTF8;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public class ByteArrayResourceStore extends ResourceStore
{
    protected static final byte[] EMPTY_BYTE_ARRAY = new byte[ 0 ];
    
    private byte[] contents = EMPTY_BYTE_ARRAY;
    
    public ByteArrayResourceStore()
    {
    }

    public ByteArrayResourceStore( final byte[] contents )
    {
        setContents( contents );
    }
    
    public ByteArrayResourceStore( final String contents )
    {
        this.contents = contents.getBytes( UTF8 );
    }
    
    public ByteArrayResourceStore( final InputStream in ) throws ResourceStoreException
    {
        setContents( in );
    }
    
    public byte[] getContents()
    {
        final int length = this.contents.length;
        final byte[] copy = new byte[ length ];
        System.arraycopy( this.contents, 0, copy, 0, length );
        
        return copy;
    }

    public void setContents( final byte[] contents )
    {
        if( contents == null )
        {
            throw new IllegalArgumentException();
        }
        
        final int length = contents.length;
        final byte[] copy = new byte[ length ];
        System.arraycopy( contents, 0, copy, 0, length );
        
        this.contents = copy;
    }

    public void setContents( final InputStream in ) throws ResourceStoreException
    {
        if( in == null )
        {
            throw new IllegalArgumentException();
        }
        
        try
        {
            byte[] buffer = new byte[ 16 * 1024 ];
            int bufferUsedLength = 0;
            int bytesRead = 0;
            
            while( ( bytesRead = in.read( buffer, bufferUsedLength, buffer.length - bufferUsedLength ) ) != -1 )
            {
                bufferUsedLength += bytesRead;
                
                if( buffer.length - bufferUsedLength < 1024 )
                {
                    byte[] newBuffer = new byte[ buffer.length * 2 ];
                    System.arraycopy( buffer, 0, newBuffer, 0, bufferUsedLength );
                    buffer = newBuffer;
                }
            }
            
            this.contents = new byte[ bufferUsedLength ];
            System.arraycopy( buffer, 0, this.contents, 0, bufferUsedLength );
        }
        catch( final IOException e )
        {
            throw new ResourceStoreException( e );
        }
    }
    
}
