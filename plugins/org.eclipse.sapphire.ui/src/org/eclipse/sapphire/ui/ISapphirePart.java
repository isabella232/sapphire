/******************************************************************************
 * Copyright (c) 2014 Oracle and Liferay
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
import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.ui.def.PartDef;
import org.eclipse.sapphire.ui.forms.swt.SwtResourceCache;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 * @author <a href="mailto:gregory.amerson@liferay.com">Gregory Amerson</a>
 */

public interface ISapphirePart
{
    ISapphirePart parent();
    <T> T nearest( final Class<T> partType );
    Element getModelElement();
    Element getLocalModelElement();
    Status validation();
    IContext getDocumentationContext();
    SwtResourceCache getSwtResourceCache();
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
