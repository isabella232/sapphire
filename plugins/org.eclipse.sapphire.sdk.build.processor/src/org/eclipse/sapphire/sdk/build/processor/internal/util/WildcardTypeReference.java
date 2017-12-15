/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.sdk.build.processor.internal.util;

import java.util.Set;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class WildcardTypeReference

    extends TypeReference

{
    public static final WildcardTypeReference INSTANCE = new WildcardTypeReference();
    
    private WildcardTypeReference()
    {
        super( "?" );
    }

    @Override
    public void contributeNecessaryImports( final Set<TypeReference> imports )
    {
        // Nothing to contribute.
    }
    
}

