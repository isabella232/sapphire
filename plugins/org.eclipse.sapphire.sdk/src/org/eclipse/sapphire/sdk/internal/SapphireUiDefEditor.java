/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.sdk.internal;

import org.eclipse.sapphire.ui.def.ISapphireUiDef;
import org.eclipse.sapphire.ui.swt.xml.editor.SapphireEditorForXml;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SapphireUiDefEditor

    extends SapphireEditorForXml
    
{
    public SapphireUiDefEditor()
    {
        super( "org.eclipse.sapphire.sdk" );
        
        setRootModelElementType( ISapphireUiDef.TYPE );
        setEditorDefinitionPath( "org.eclipse.sapphire.sdk/org/eclipse/sapphire/sdk/DefinitionEditor.sdef/main" );
    }

}
