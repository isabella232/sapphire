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

package org.eclipse.sapphire.ui.assist;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.forms.events.IHyperlinkListener;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class PropertyEditorAssistContribution
{
    private String text;
    private final Map<String,Image> images;
    private final Map<String,Image> imagesReadOnly;
    private IHyperlinkListener listener;
    
    public PropertyEditorAssistContribution()
    {
        this.text = null;
        this.images = new HashMap<String,Image>();
        this.imagesReadOnly = Collections.unmodifiableMap( this.images );
        this.listener = null;
    }
    
    public String getText()
    {
        return this.text;
    }
    
    public void setText( final String text )
    {
        this.text = text;
    }
    
    public Map<String,Image> getImages()
    {
        return this.imagesReadOnly;
    }
    
    public Image getImage( final String name )
    {
        return this.images.get( name );
    }
    
    public void setImage( final String name,
                          final Image image )
    {
        this.images.put( name, image );
    }
    
    public IHyperlinkListener getHyperlinkListener()
    {
        return this.listener;
    }
    
    public void setHyperlinkListener( final IHyperlinkListener listener )
    {
        this.listener = listener;
    }
}
