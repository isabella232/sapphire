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

package org.eclipse.sapphire.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.ui.actions.Action;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public interface ISapphirePart
{
    ISapphirePart getParentPart();
    <T> T getNearestPart( final Class<T> partType );
    IModelElement getModelElement();
    Action getAction( String id );
    IStatus getValidationState();
    String getHelpContextId();
    SapphireImageCache getImageCache();
    void addListener( SapphirePartListener listener );
    void removeListener( SapphirePartListener listener );
    void dispose();
    
}
