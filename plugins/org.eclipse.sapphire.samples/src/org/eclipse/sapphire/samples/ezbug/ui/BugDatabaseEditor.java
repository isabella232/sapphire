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

package org.eclipse.sapphire.samples.ezbug.ui;

import org.eclipse.sapphire.modeling.IModel;
import org.eclipse.sapphire.modeling.ModelStore;
import org.eclipse.sapphire.modeling.xml.ModelStoreForXml;
import org.eclipse.sapphire.samples.ezbug.internal.BugDatabase;
import org.eclipse.sapphire.ui.xml.SapphireEditorForXml;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class BugDatabaseEditor

    extends SapphireEditorForXml
    
{
    public BugDatabaseEditor()
    {
        super( "org.eclipse.sapphire.samples" );
        
        setEditorDefinitionPath( "org.eclipse.sapphire.samples/sdef/EzBug.sdef/editor.page" );
    }

    @Override
    protected IModel createModel( final ModelStore modelStore )
    {
        return new BugDatabase( (ModelStoreForXml) modelStore );
    }
    
}
