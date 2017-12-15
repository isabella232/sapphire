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

package org.eclipse.sapphire.ui.swt.renderer.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.ValidFileExtensions;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireBrowseActionHandler;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.def.ISapphireActionHandlerDef;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class AbsoluteFilePathBrowseActionHandler 

    extends SapphireBrowseActionHandler
    
{
    public static final String ID = "Sapphire.Browse.File.Absolute";
    public static final String PARAM_EXTENSIONS = "extensions";
    
    public List<String> extensions;
    
    @Override
    public void init( final SapphireAction action,
                      final ISapphireActionHandlerDef def )
    {
        super.init( action, def );

        setId( ID );
        setLabel( Resources.label );
        addImage( PlatformUI.getWorkbench().getSharedImages().getImageDescriptor( ISharedImages.IMG_OBJ_FILE ) );
        
        final ModelProperty property = getProperty();

        this.extensions = Collections.emptyList();
        
        final String paramExtensions = def.getParam( PARAM_EXTENSIONS );
        
        if( paramExtensions != null )
        {
            this.extensions = new ArrayList<String>();
            
            for( String extension : paramExtensions.split( "," ) )
            {
                extension = extension.trim();
                
                if( extension.length() > 0 )
                {
                    this.extensions.add( extension );
                }
            }
        }
        else
        {
            final ValidFileExtensions validFileExtensionsAnnotation = property.getAnnotation( ValidFileExtensions.class );
            
            if( validFileExtensionsAnnotation != null )
            {
                this.extensions = new ArrayList<String>();
                
                for( String extension : validFileExtensionsAnnotation.value() )
                {
                    extension = extension.trim();
                    
                    if( extension.length() > 0 )
                    {
                        this.extensions.add( extension );
                    }
                }
            }
        }
    }
    
    @Override
    protected String browse( final SapphireRenderingContext context )
    {
        final ValueProperty property = getProperty();
        
        final FileDialog dialog = new FileDialog( context.getShell() );
        dialog.setText( property.getLabel( true, CapitalizationType.FIRST_WORD_ONLY, false ) );
        
        final Value<IPath> value = getModelElement().read( property );
        final IPath path = value.getContent();
        
        if( path != null && path.segmentCount() > 1 )
        {
            dialog.setFilterPath( path.removeLastSegments( 1 ).toOSString() );
            dialog.setFileName( path.lastSegment() );
        }
        
        if( ! this.extensions.isEmpty() )
        {
            final StringBuilder buf = new StringBuilder();
            
            for( String extension : this.extensions )
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
    
    private static final class Resources extends NLS 
    {
        public static String label;

        static 
        {
            initializeMessages( AbsoluteFilePathBrowseActionHandler.class.getName(), Resources.class );
        }
    }

}