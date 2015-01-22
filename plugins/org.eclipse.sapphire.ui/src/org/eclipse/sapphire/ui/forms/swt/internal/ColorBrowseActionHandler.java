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

package org.eclipse.sapphire.ui.forms.swt.internal;

import org.eclipse.sapphire.Color;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.ui.Presentation;
import org.eclipse.sapphire.ui.forms.BrowseActionHandler;
import org.eclipse.sapphire.ui.forms.PropertyEditorCondition;
import org.eclipse.sapphire.ui.forms.PropertyEditorPart;
import org.eclipse.sapphire.ui.forms.swt.FormComponentPresentation;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * Opens a color browse dialog. Activates if the property is a value property of type Color.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ColorBrowseActionHandler extends BrowseActionHandler
{
    @Override
    protected String browse( final Presentation context )
    {
        final Property property = property();
        
        final Shell parent = ( (FormComponentPresentation) context ).shell();
        final Rectangle bounds = parent.getBounds();
        
        // There is no means to compute the size of the color dialog. In the following
        // computations, measurements of the dialog on Windows 7 are used. Will need to
        // generalize in the future.
        
        final int x = bounds.x + bounds.width / 2 - 120;
        final int y = bounds.y + bounds.height / 2 - 170;

        final Shell shell = new Shell( parent );
        
        try
        {
            shell.setBounds( x, y, 0, 0 );
            
            final ColorDialog dialog = new ColorDialog( shell );
            
            dialog.setText( property.definition().getLabel( false, CapitalizationType.TITLE_STYLE, false ) );
            dialog.setRGB( convert( (Color) ( (Value<?>) property ).content() ) );
            
            final RGB pickedColor = dialog.open();
            
            if( pickedColor != null )
            {
                return convert( pickedColor ).toString();
            }
        }
        finally
        {
            shell.dispose();
        }
       
        return null;
    }
    
    private static Color convert( final RGB rgb )
    {
        return ( rgb == null ? null : new Color( rgb.red, rgb.green, rgb.blue ) );
    }
    
    private static RGB convert( final Color color )
    {
        return ( color == null ? null : new RGB( color.red(), color.green(), color.blue() ) );
    }
    
    public static final class Condition extends PropertyEditorCondition
    {
        @Override
        protected boolean evaluate( final PropertyEditorPart part )
        {
            return ( part.property().definition().getTypeClass() == Color.class );
        }
    }
    
}