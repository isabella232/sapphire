/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ling Hao - initial implementation and ongoing maintenance
 *    Konstantin Komissarchik - [333782] Problems in whitespace handling
 ******************************************************************************/

package org.eclipse.sapphire.modeling.util.internal;

import org.eclipse.sapphire.modeling.docsys.BoldPart;
import org.eclipse.sapphire.modeling.docsys.CodePart;
import org.eclipse.sapphire.modeling.docsys.DocumentationContent;
import org.eclipse.sapphire.modeling.docsys.DocumentationPart;
import org.eclipse.sapphire.modeling.docsys.LineBreakPart;
import org.eclipse.sapphire.modeling.docsys.ListItem;
import org.eclipse.sapphire.modeling.docsys.ListPart;
import org.eclipse.sapphire.modeling.docsys.OrderedListPart;
import org.eclipse.sapphire.modeling.docsys.ParagraphBreakPart;
import org.eclipse.sapphire.modeling.docsys.TextPart;
import org.eclipse.sapphire.modeling.docsys.UnorderedListPart;
import org.eclipse.sapphire.services.StandardValueNormalizationService;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public class DocumentationUtil {

    public final static String NEW_LINE = System.getProperty("line.separator");
    public final static String NEW_LINE_2 = NEW_LINE + NEW_LINE;
    
    private final static String BOLD_BEGIN = "<@#$b>"; 
    private final static String BOLD_END = "</@#$b>"; 
    
    public final static String decodeDocumentationTags(final String str) {
        if (str == null)
            return str;
        
        DocumentationContent content = DocumentationContent.parse(str.trim());

        final StringBuilder buf = new StringBuilder();
        FormatFlags flags = new FormatFlags();
        format( buf, content, -1, flags );
        return buf.toString();

    }
    
    private static boolean startsWithSpaces(final String text) {
        final char ch = text.charAt( 0 );
        return Character.isWhitespace( ch );
    }
    
    private static boolean endsWithSpaces(final String text) {
        final char ch = text.charAt( text.length() - 1 );
        return Character.isWhitespace( ch );
    }

    private static void format(final StringBuilder buf, final DocumentationPart part, int index, FormatFlags flags) {
        boolean collapseSpaces = flags.collapseSpaces;
        if (part instanceof TextPart) {
            final String str = ((TextPart) part).getText();
            final boolean endsWithSpace = endsWithSpaces(str);
            final boolean startsWithSpace = startsWithSpaces(str);
            if (startsWithSpace && buf.toString().endsWith(BOLD_END)) {
                buf.append(' ');
            }
            buf.append(collapseSpaces ? StandardValueNormalizationService.collapse(str) : str);
            flags.endsInSpace = endsWithSpace;
        } else if (part instanceof LineBreakPart) {
            buf.append(NEW_LINE);
            flags.endsInSpace = false;
        } else if (part instanceof ParagraphBreakPart) {
            buf.append(NEW_LINE_2);
            flags.endsInSpace = false;
        } else if (part instanceof OrderedListPart) {
            buf.append(NEW_LINE_2);

            int childIndex = 0;
            for (ListItem item : ((ListPart) part).getItems()) {
                childIndex++;
                format(buf, item, childIndex, flags);
            }
        } else if (part instanceof UnorderedListPart) {
            buf.append(NEW_LINE_2);

            for (ListItem item : ((ListPart) part).getItems()) {
                format(buf, item, -1, flags);
            }
        } else if (part instanceof ListItem) {
            buf.append(index == -1 ? "*" : index);
            buf.append("  ");

            for (DocumentationPart child : ((ListItem) part).getChildren()) {
                format(buf, child, -1, flags);
            }

            buf.append(NEW_LINE);
            flags.endsInSpace = false;
        } else if (part instanceof BoldPart) {
            BoldPart boldPart = (BoldPart)part;
            if (boldPart.isOpen()) {
                if (flags.endsInSpace) {
                    buf.append(' ');
                }
                buf.append(BOLD_BEGIN);
            } else {
                buf.append(BOLD_END);
            }
        } else if (part instanceof CodePart) {
            CodePart codePart = (CodePart)part;
            if (codePart.isOpen()) {
                flags.collapseSpaces = false;
            } else {
                flags.collapseSpaces = true;
            }
        } else if (part instanceof DocumentationContent) {
            for (DocumentationPart child : ((DocumentationContent) part).getChildren()) {
                format(buf, child, -1, flags);
            }
        }
    }
    
    private static class FormatFlags {
        boolean collapseSpaces = true;
        boolean endsInSpace = false;
        
        FormatFlags() {
        }
    }
}
