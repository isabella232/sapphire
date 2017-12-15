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

package org.eclipse.sapphire.sdk.internal;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sapphire.sdk.extensibility.IExtensionSummaryExportOp;
import org.eclipse.sapphire.sdk.extensibility.ISapphireExtensionDef;
import org.eclipse.sapphire.ui.swt.xml.editor.SapphireEditorForXml;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.ui.PartInitException;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SapphireExtensionEditor

    extends SapphireEditorForXml
    
{
    private Browser browser;
    
    public SapphireExtensionEditor()
    {
        super( "org.eclipse.sapphire.sdk" );
        
        setRootModelElementType( ISapphireExtensionDef.TYPE );
        setEditorDefinitionPath( "org.eclipse.sapphire.sdk/org/eclipse/sapphire/sdk/ExtensionEditor.sdef/main" );
    }

    @Override
    protected void createFormPages()
    
        throws PartInitException
        
    {
        super.createFormPages();
        
        this.browser = new Browser( getContainer(), SWT.NONE );
        
        addPage( 2, this.browser );
        setPageId( this.browser, "summary", null );
        setPageText( 2, Resources.summaryPageTitle );
    }
    
    @Override
    protected void pageChange( final int newPageIndex ) 
    {
        if( newPageIndex == 2 )
        {
            final List<ISapphireExtensionDef> extensions = Collections.singletonList( (ISapphireExtensionDef) getModelElement() );
            final IExtensionSummaryExportOp op = IExtensionSummaryExportOp.TYPE.instantiate();
            final String text = op.execute( extensions, new NullProgressMonitor() );
            
            this.browser.setText( text );
        }
        
        super.pageChange( newPageIndex );
    }
    
    private static final class Resources extends NLS
    {
        public static String summaryPageTitle;
    
        static 
        {
            initializeMessages( SapphireExtensionEditor.class.getName(), Resources.class );
        }
    }
    
}
