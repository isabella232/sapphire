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

package org.eclipse.sapphire.ui.swt.xml.editor.internal;

import org.eclipse.sapphire.ConversionService;
import org.eclipse.sapphire.ui.swt.xml.editor.XmlEditorResourceStore;
import org.eclipse.ui.IEditorInput;

/**
 * ConversionService implementation for XmlEditorResourceStore to IEditorInput conversions.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class XmlEditorResourceStoreToIEditorInputConversionService extends ConversionService<XmlEditorResourceStore,IEditorInput>
{
    public XmlEditorResourceStoreToIEditorInputConversionService()
    {
        super( XmlEditorResourceStore.class, IEditorInput.class );
    }

    @Override
    public IEditorInput convert( final XmlEditorResourceStore store )
    {
        return store.getXmlEditor().getEditorInput();
    }

}
