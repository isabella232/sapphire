/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation
 ******************************************************************************/

package org.eclipse.sapphire.sdk.internal;

import org.eclipse.sapphire.sdk.CreateExtensionManifestOp;
import org.eclipse.ui.IWorkbenchWizard;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class CreateExtensionManifestWizard 

    extends CreateWorkspaceFileWizard<CreateExtensionManifestOp>
    implements IWorkbenchWizard
    
{
    public CreateExtensionManifestWizard()
    {
        super( (CreateExtensionManifestOp) CreateExtensionManifestOp.TYPE.instantiate(),
               "org.eclipse.sapphire.sdk/org/eclipse/sapphire/sdk/ExtensionManifest.sdef!CreateExtensionManifestWizard" );
    }
    
}
