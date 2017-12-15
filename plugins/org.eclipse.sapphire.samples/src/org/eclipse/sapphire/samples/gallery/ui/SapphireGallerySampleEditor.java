/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.samples.gallery.ui;

import org.eclipse.sapphire.samples.gallery.IGallery;
import org.eclipse.sapphire.ui.swt.xml.editor.SapphireEditorForXml;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SapphireGallerySampleEditor

    extends SapphireEditorForXml
    
{
    public SapphireGallerySampleEditor()
    {
        super( "org.eclipse.sapphire.samples" );
        
        setRootModelElementType( IGallery.TYPE );
        setEditorDefinitionPath( "org.eclipse.sapphire.samples/org/eclipse/sapphire/samples/gallery/GalleryEditor.sdef/main" );
    }
    
}
