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

import java.io.File;

import org.eclipse.sapphire.ImageData;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.Path;
import org.eclipse.sapphire.ui.Presentation;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.def.ActionHandlerDef;
import org.eclipse.sapphire.ui.forms.BrowseActionHandler;
import org.eclipse.swt.widgets.DirectoryDialog;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public class AbsoluteFolderPathBrowseActionHandler extends BrowseActionHandler
{
    public static final String ID = "Sapphire.Browse.Folder.Absolute";
    
    @Text( "absolute fol&der path" )
    private static LocalizableText label;

    static 
    {
        LocalizableText.init( AbsoluteFolderPathBrowseActionHandler.class );
    }

    @Override
    public void init( final SapphireAction action,
                      final ActionHandlerDef def )
    {
        super.init( action, def );

        setId( ID );
        setLabel( label.text() );
        addImage( ImageData.readFromClassLoader( AbsoluteFolderPathBrowseActionHandler.class, "Folder.png" ).required() );
    }

    @Override
    protected String browse( final Presentation context )
    {
        final Property property = property();
        
        final DirectoryDialog dialog = new DirectoryDialog( ( (FormComponentPresentation) context ).shell() );
        dialog.setText( property.definition().getLabel( true, CapitalizationType.FIRST_WORD_ONLY, false ) );
        dialog.setMessage( createBrowseDialogMessage( property.definition().getLabel( true, CapitalizationType.NO_CAPS, false ) ) );
        
        final Value<?> value = (Value<?>) property;
        final Path path = (Path) value.content();
        
        if( path != null )
        {
            File f = new File( path.toOSString() );
            
            while( f != null && ! f.exists() )
            {
                f = f.getParentFile();
            }
            
            if( f != null )
            {
                dialog.setFilterPath( f.getAbsolutePath() );
            }
        }
        
        return dialog.open();
    }
    
}