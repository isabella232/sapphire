/******************************************************************************
 * Copyright (c) 2015 Oracle and Liferay
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Gregory Amerson - [444202] Lazy loading of editor pages
 ******************************************************************************/

package org.eclipse.sapphire.samples.po;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.ui.SapphireEditor;
import org.eclipse.sapphire.ui.swt.xml.editor.XmlEditorResourceStore;
import org.eclipse.ui.PartInitException;
import org.eclipse.wst.sse.ui.StructuredTextEditor;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 * @author <a href="mailto:gregory.amerson@liferay.com">Gregory Amerson</a>
 */

public final class PurchaseOrderEditor extends SapphireEditor
{
    private StructuredTextEditor sourcePage;

    @Override
    protected void createEditorPages() throws PartInitException 
    {
        addDeferredPage( "General", "GeneralPage" );
        addDeferredPage( "Entries", "EntriesPage" );
        
        this.sourcePage = new StructuredTextEditor();
        this.sourcePage.setEditorPart( this );
        
        int index = addPage( this.sourcePage, getEditorInput() );
        setPageText( index, "Source" );
    }

    @Override
    protected Element createModel()
    {
        return PurchaseOrder.TYPE.instantiate( new RootXmlResource( new XmlEditorResourceStore( this, this.sourcePage ) ) );
    }

}
