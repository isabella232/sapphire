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

package org.eclipse.sapphire.modeling.annotations;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class DocumentationResource
{
    private final String label;
    private final String url;
    
    public DocumentationResource( final String label,
                                  final String url )
    {
        this.label = label;
        this.url = url;
    }
    
    public String getLabel()
    {
        return this.label;
    }
    
    public String getUrl()
    {
        return this.url;
    }

}
