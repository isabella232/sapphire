/******************************************************************************
 * Copyright (c) 2013 Oracle and Liferay
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Ling Hao - [329114] rewrite context help binding feature
 *    Gregory Amerson - [372816] Provide adapt mechanism for SapphirePart 
 ******************************************************************************/

package org.eclipse.sapphire.ui;

import java.util.List;
import java.util.Set;

import org.eclipse.help.IContext;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.ui.def.PartDef;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 * @author <a href="mailto:gregory.amerson@liferay.com">Gregory Amerson</a>
 */

public interface ISapphirePart
{
    ISapphirePart getParentPart();
    <T> T nearest( final Class<T> partType );
    IModelElement getModelElement();
    IModelElement getLocalModelElement();
    Status getValidationState();
    IContext getDocumentationContext();
    SapphireImageCache getImageCache();
    void dispose();
    PartDef definition();
    
    Set<String> getActionContexts();
    String getMainActionContext();
    SapphireActionGroup getActions();
    SapphireActionGroup getActions( String context );
    SapphireAction getAction( String id );
    
    <A> A adapt( Class<A> adapterType );
    
    <S extends Service> S service( Class<S> serviceType );
    <S extends Service> List<S> services( Class<S> serviceType );
    
}
