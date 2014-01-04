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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.sapphire.ImageData;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.Path;
import org.eclipse.sapphire.services.FileExtensionsService;
import org.eclipse.sapphire.ui.Presentation;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.def.ActionHandlerDef;
import org.eclipse.sapphire.ui.forms.BrowseActionHandler;
import org.eclipse.swt.widgets.FileDialog;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public class AbsoluteFilePathBrowseActionHandler extends BrowseActionHandler
{
    public static final String ID = "Sapphire.Browse.File.Absolute";
    public static final String PARAM_EXTENSIONS = "extensions";
    
    @Text( "absolute &file path" )
    private static LocalizableText label;

    static 
    {
        LocalizableText.init( AbsoluteFilePathBrowseActionHandler.class );
    }

    private FileExtensionsService fileExtensionService;
    private List<String> staticFileExtensionsList;
    
    @Override
    public void init( final SapphireAction action,
                      final ActionHandlerDef def )
    {
        super.init( action, def );

        setId( ID );
        setLabel( label.text() );
        addImage( ImageData.readFromClassLoader( AbsoluteFilePathBrowseActionHandler.class, "File.png" ).required() );
        
        final String staticFileExtensions = def.getParam( PARAM_EXTENSIONS );
        
        if( staticFileExtensions == null )
        {
            this.fileExtensionService = property().service( FileExtensionsService.class );
            
            if( this.fileExtensionService == null )
            {
                this.staticFileExtensionsList = Collections.emptyList();
            }
        }
        else
        {
            this.staticFileExtensionsList = new ArrayList<String>();
            
            for( String extension : staticFileExtensions.split( "," ) )
            {
                extension = extension.trim();
                
                if( extension.length() > 0 )
                {
                    this.staticFileExtensionsList.add( extension );
                }
            }
        }
    }
    
    @Override
    protected String browse( final Presentation context )
    {
        final Property property = property();
        
        final FileDialog dialog = new FileDialog( ( (FormComponentPresentation) context ).shell() );
        dialog.setText( property.definition().getLabel( true, CapitalizationType.FIRST_WORD_ONLY, false ) );
        
        final Value<?> value = (Value<?>) property;
        final Path path = (Path) value.content();
        
        if( path != null && path.segmentCount() > 1 )
        {
            dialog.setFilterPath( path.removeLastSegments( 1 ).toOSString() );
            dialog.setFileName( path.lastSegment() );
        }
        
        final List<String> extensions;
        
        if( this.fileExtensionService == null )
        {
            extensions = this.staticFileExtensionsList;
        }
        else
        {
            extensions = this.fileExtensionService.extensions();
        }
        
        if( ! extensions.isEmpty() )
        {
            final StringBuilder buf = new StringBuilder();
            
            for( String extension : extensions )
            {
                if( buf.length() > 0 )
                {
                    buf.append( ';' );
                }
                
                buf.append( "*." );
                buf.append( extension );
            }
            
            dialog.setFilterExtensions( new String[] { buf.toString() } );
        }
        
        return dialog.open();
    }
    
}