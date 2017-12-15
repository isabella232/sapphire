/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Ling Hao - [bugzilla 329114] rewrite context help binding feature
 ******************************************************************************/

package org.eclipse.sapphire.ui;

import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.help.IContext;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.ui.def.ISapphirePartDef;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public interface ISapphirePart
{
    ISapphirePart getParentPart();
    <T> T getNearestPart( final Class<T> partType );
    IModelElement getModelElement();
    IStatus getValidationState();
    IContext getDocumentationContext();
    SapphireImageCache getImageCache();
    void addListener( SapphirePartListener listener );
    void removeListener( SapphirePartListener listener );
    void dispose();
    ISapphirePartDef getDefinition();
    
    Set<String> getActionContexts();
    String getMainActionContext();
    SapphireActionGroup getActions();
    SapphireActionGroup getActions( String context );
    SapphireAction getAction( String id );
    
}
