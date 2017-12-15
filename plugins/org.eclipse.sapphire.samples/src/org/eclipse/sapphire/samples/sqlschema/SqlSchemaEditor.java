/******************************************************************************
 * Copyright (c) 2015 Oracle and Liferay
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 *    Konstantin Komissarchik - ongoing maintenance
 *    Gregory Amerson - [444202] Lazy loading of editor pages
 ******************************************************************************/

package org.eclipse.sapphire.samples.sqlschema;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.ui.SapphireEditor;
import org.eclipse.sapphire.ui.swt.xml.editor.XmlEditorResourceStore;
import org.eclipse.ui.PartInitException;
import org.eclipse.wst.sse.ui.StructuredTextEditor;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 * @author <a href="mailto:gregory.amerson@liferay.com">Gregory Amerson</a>
 */

public class SqlSchemaEditor extends SapphireEditor 
{
    private StructuredTextEditor schemaSourceEditor;

    @Override
    protected void createEditorPages() throws PartInitException 
    {
        addDeferredPage( "Schema", "DiagramPage" );
        addDeferredPage( "Details", "DetailsPage" );
        
        this.schemaSourceEditor = new StructuredTextEditor();
        this.schemaSourceEditor.setEditorPart(this);
        
        int index = addPage( this.schemaSourceEditor, getEditorInput() );
        setPageText( index, "Source" );
    }

    @Override
    protected Element createModel()
    {
        return Schema.TYPE.instantiate(new RootXmlResource(new XmlEditorResourceStore(this, this.schemaSourceEditor)));
    }

}
