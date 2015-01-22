/*******************************************************************************
 * Copyright (c) 2015 Oracle and Accenture Services Pvt Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation
 *    Kamesh Sampath - initial implementation
 ******************************************************************************/

package org.eclipse.sapphire.services;

import org.eclipse.sapphire.ImageData;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 * @author <a href="mailto:kamesh.sampath@accenture.com">Kamesh Sampath</a>
 */

public final class ContentProposal
{
    private String content;
    private String description;
    private String label;
    private ImageData image;

    public ContentProposal( final String content )
    {
        this( content, null, null, null );
    }
    
    public ContentProposal( final String content, 
                            final String description,
                            final String label, 
                            final ImageData image )
    {
        if( content == null ) 
        {
            throw new IllegalStateException();
        }
        
        this.content = content;
        this.description = description;
        this.label = ( label == null ? content : label );
        this.image = image;
    }

    public String content()
    {
        return this.content;
    }

    public String description()
    {
        return this.description;
    }

    public String label()
    {
        return this.label;
    }

    public ImageData image()
    {
        return this.image;
    }

}
