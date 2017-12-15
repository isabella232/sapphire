/******************************************************************************
 * Copyright (c) 2016 Oracle and Liferay
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Gregory Amerson - [444202] Lazy loading of editor pages
 ******************************************************************************/

package org.eclipse.sapphire.sdk.internal;

import java.util.Collections;
import java.util.List;

import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.sdk.extensibility.ExtensionSummaryExportOp;
import org.eclipse.sapphire.sdk.extensibility.SapphireExtensionDef;
import org.eclipse.sapphire.ui.swt.xml.editor.SapphireEditorForXml;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.ui.PartInitException;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 * @author <a href="mailto:gregory.amerson@liferay.com">Gregory Amerson</a>
 */

public final class ExtensionManifestEditor extends SapphireEditorForXml
{
    @Text( "Summary" )
    private static LocalizableText summaryPageTitle;
    
    static 
    {
        LocalizableText.init( ExtensionManifestEditor.class, ExtensionManifestEditor.class.getName() + "Ext" );
    }

    private Browser browser;
    
    public ExtensionManifestEditor()
    {
        super( SapphireExtensionDef.TYPE, null );
    }

    @Override
    protected void createFormPages() throws PartInitException
    {
        super.createFormPages();

        this.browser = new Browser( getContainer(), SWT.NONE );
        
        addPage( 2, this.browser );
        setPageText( 2, summaryPageTitle.text() );
    }
    
    @Override
    protected void pageChange( final int newPageIndex ) 
    {
        if( newPageIndex == 2 )
        {
            final List<SapphireExtensionDef> extensions = Collections.singletonList( (SapphireExtensionDef) getModelElement() );
            final ExtensionSummaryExportOp op = ExtensionSummaryExportOp.TYPE.instantiate();
            final String text = op.execute( extensions, null );
            
            this.browser.setText( text );
        }
        
        super.pageChange( newPageIndex );
    }
    
}
