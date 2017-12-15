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

package org.eclipse.sapphire.samples.zoo;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.sapphire.modeling.xml.ModelStoreForXml;
import org.eclipse.sapphire.samples.zoo.internal.ZooModel;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ZooModelFactory
{
    public static IZooModel getModel( final File file )
    {
        return getModel( new ModelStoreForXml( file ) );
    }
    
    public static IZooModel getModel( final IFile file )
    {
        return getModel( new ModelStoreForXml( file ) );
    }
    
    public static IZooModel getModel( final ModelStoreForXml modelStore )
    {
        return new ZooModel( modelStore );
    }
    
}
