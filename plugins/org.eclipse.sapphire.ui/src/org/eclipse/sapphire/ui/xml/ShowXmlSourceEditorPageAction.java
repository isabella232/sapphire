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

package org.eclipse.sapphire.ui.xml;

import org.eclipse.osgi.util.NLS;
import org.eclipse.sapphire.ui.SapphireEditor;
import org.eclipse.sapphire.ui.SapphireImageCache;
import org.eclipse.sapphire.ui.actions.Action;
import org.eclipse.swt.widgets.Shell;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class ShowXmlSourceEditorPageAction

    extends Action
    
{
    public static final String ACTION_ID = "editor:show-xml-source-editor-page"; //$NON-NLS-1$
    
    public ShowXmlSourceEditorPageAction()
    {
        setId( ACTION_ID );
        setLabel( Resources.showXmlSourceEditorPageActionLabel );
        setImageDescriptor( SapphireImageCache.ACTION_SHOW_XML_EDITOR_PAGE );
    }

    @Override
    protected Object run( final Shell shell )
    {
        final SapphireEditor editor = getPart().getNearestPart( SapphireEditor.class );
        editor.showPage( SapphireEditorForXml.PAGE_MAIN_SOURCE );
        return null;
    }
    
    private static final class Resources
    
        extends NLS
    
    {
        public static String showXmlSourceEditorPageActionLabel;
        
        static
        {
            initializeMessages( ShowXmlSourceEditorPageAction.class.getName(), Resources.class );
        }
    }
    
}
