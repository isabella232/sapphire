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

package org.eclipse.sapphire.modeling.docsys;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.sapphire.modeling.docsys.internal.DocumentationContentParser;
import org.eclipse.sapphire.modeling.el.parser.internal.TokenMgrError;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public class DocumentationContent

    extends DocumentationPart
    
{
    private final List<DocumentationPart> children = new ArrayList<DocumentationPart>();
    
    public static DocumentationContent parse( final String text )
    {
        final DocumentationContentParser parser = new DocumentationContentParser( new StringReader( text ) );
        
        try
        {
            return parser.Start();
        }
        catch( TokenMgrError e )
        {
            throw new RuntimeException( e );
        }
        catch( Exception e )
        {
            throw new RuntimeException( e );
        }
    }
    
    public List<DocumentationPart> getChildren()
    {
        return this.children;
    }
    
    public void addChild( final DocumentationPart part )
    {
        this.children.add( part );
    }
}
