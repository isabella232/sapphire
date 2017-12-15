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

import org.eclipse.sapphire.modeling.IModel;
import org.eclipse.sapphire.modeling.ModelStore;
import org.eclipse.sapphire.modeling.xml.ModelStoreForXml;
import org.eclipse.sapphire.ui.def.SapphireUiDefFactory;
import org.eclipse.sapphire.ui.xml.SapphireEditorForXml;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class SapphireUiDefinitionEditor

    extends SapphireEditorForXml
    
{
    public SapphireUiDefinitionEditor()
    {
        super( "org.eclipse.sapphire.sdk" );
        
        setEditorDefinitionPath( "org.eclipse.sapphire.sdk/sdef/SapphireUiDefinitionEditor.sdef/main" );
    }

    @Override
    protected IModel createModel( final ModelStore modelStore )
    {
        return SapphireUiDefFactory.load( (ModelStoreForXml) modelStore, true );
    }
    
}
