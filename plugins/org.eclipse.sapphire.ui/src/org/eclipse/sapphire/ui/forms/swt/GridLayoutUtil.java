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

package org.eclipse.sapphire.ui.forms.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class GridLayoutUtil
{
    public static final GridLayout glayout( final int columns )
    {
        return new GridLayout( columns, false );
    }

    public static final GridLayout glayout( final int columns,
                                            final int marginWidth,
                                            final int marginHeight )
    {
        final GridLayout layout = new GridLayout( columns, false );

        layout.marginWidth = marginWidth;
        layout.marginHeight = marginHeight;
        
        return layout;
    }

    public static final GridLayout glayout( final int columns,
                                            final int leftMargin,
                                            final int rightMargin,
                                            final int topMargin,
                                            final int bottomMargin )
    {
        final GridLayout layout = new GridLayout( columns, false );
        
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.marginLeft = leftMargin;
        layout.marginRight = rightMargin;
        layout.marginTop = topMargin;
        layout.marginBottom = bottomMargin;
        
        return layout;
    }
    
    public static final GridLayout glspacing( final GridLayout layout,
                                              final int spacing )
    {
        layout.horizontalSpacing = spacing;
        layout.verticalSpacing = spacing;
        
        return layout;
    }
    
    public static final GridLayout glspacing( final GridLayout layout,
                                              final int hspacing,
                                              final int vspacing )
    {
        layout.horizontalSpacing = hspacing;
        layout.verticalSpacing = vspacing;
        
        return layout;
    }

    public static final GridData gd()
    {
        return new GridData();
    }
    
    public static final GridData gdfill()
    {
        return new GridData( SWT.FILL, SWT.FILL, true, true );
    }
    
    public static final GridData gdhfill()
    {
        return new GridData( GridData.FILL_HORIZONTAL );
    }

    public static final GridData gdvfill()
    {
        return new GridData( GridData.FILL_VERTICAL );
    }
    
    public static final GridData gdhhint( final GridData gd,
                                          final int heightHint )
    {
        gd.heightHint = heightHint;
        return gd;
    }
    
    public static final GridData gdwhint( final GridData gd,
                                          final int widthHint )
    {
        gd.widthHint = widthHint;
        return gd;
    }
    
    public static final GridData gdhindent( final GridData gd,
                                            final int horizontalIndent )
    {
        gd.horizontalIndent = horizontalIndent;
        return gd;
    }

    public static final GridData gdvindent( final GridData gd,
                                            final int verticalIndent )
    {
        gd.verticalIndent = verticalIndent;
        return gd;
    }
    
    public static final GridData gdhspan( final GridData gd,
                                          final int span )
    {
        gd.horizontalSpan = span;
        return gd;
    }

    public static final GridData gdvspan( final GridData gd,
                                          final int span )
    {
        gd.verticalSpan = span;
        return gd;
    }
    
    public static final GridData gdhalign( final GridData gd,
                                           final int alignment )
    {
        gd.horizontalAlignment = alignment;
        return gd;
    }
    
    public static final GridData gdvalign( final GridData gd,
                                           final int alignment )
    {
        gd.verticalAlignment = alignment;
        return gd;
    }
    
}
