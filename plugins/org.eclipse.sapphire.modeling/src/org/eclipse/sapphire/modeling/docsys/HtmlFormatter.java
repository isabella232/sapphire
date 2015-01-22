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

package org.eclipse.sapphire.modeling.docsys;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class HtmlFormatter
{
    public static String format( final DocumentationContent content )
    {
        final StringBuilder buf = new StringBuilder();
        format( buf, content );
        return buf.toString();
    }
    
    private static void format( final StringBuilder buf,
                                final DocumentationPart part )
    {
        if( part instanceof TextPart )
        {
            buf.append( ( (TextPart) part ).getText() );
        }
        else if( part instanceof LineBreakPart )
        {
            buf.append( "<br/>" );
        }
        else if( part instanceof ParagraphBreakPart )
        {
            buf.append( "<br/><br/>" );
        }
        else if( part instanceof BoldPart )
        {
            if ( ((BoldPart)part).isOpen() ) 
            {
                buf.append( "<b>" );
            }
            else
            {
                buf.append( "</b>" );
            }
        }
        else if( part instanceof OrderedListPart )
        {
            buf.append( "<ol>" );
            
            for( ListItem item : ( (ListPart) part ).getItems() )
            {
                format( buf, item );
            }

            buf.append( "</ol>" );
        }
        else if( part instanceof UnorderedListPart )
        {
            buf.append( "<ul>" );
            
            for( ListItem item : ( (ListPart) part ).getItems() )
            {
                format( buf, item );
            }

            buf.append( "</ul>" );
        }
        else if( part instanceof ListItem )
        {
            buf.append( "<li>" );
            
            for( DocumentationPart child : ( (ListItem) part ).getChildren() )
            {
                format( buf, child );
            }
            
            buf.append( "</li>" );
        }
        else if( part instanceof DocumentationContent )
        {
            for( DocumentationPart child : ( (DocumentationContent) part ).getChildren() )
            {
                format( buf, child );
            }
        }
    }

}
