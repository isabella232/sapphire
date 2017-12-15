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

package org.eclipse.sapphire.modeling;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class ByteArrayModelStore

    extends ModelStore
    
{
    protected static final byte[] EMPTY_BYTE_ARRAY = new byte[ 0 ];
    
    private byte[] contents = EMPTY_BYTE_ARRAY;
    
    public ByteArrayModelStore()
    {
    }
    
    public ByteArrayModelStore( final byte[] contents )
    {
        setContents( contents );
    }
    
    public ByteArrayModelStore( final InputStream in )
    
        throws IOException
        
    {
        setContents( in );
    }
    
    public byte[] getContents()
    {
        return this.contents;
    }

    public void setContents( final byte[] contents )
    {
        this.contents = contents;
    }

    public void setContents( final InputStream in )
    
        throws IOException
        
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
    
    @Override
    public void open() throws IOException
    {
        // Nothing to do here.
    }

    @Override
    public void save() throws IOException
    {
        // Nothing to do here.
    }
   
}
