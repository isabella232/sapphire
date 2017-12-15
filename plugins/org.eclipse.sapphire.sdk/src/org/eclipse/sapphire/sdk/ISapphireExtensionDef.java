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

package org.eclipse.sapphire.sdk;

import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.extensibility.ISapphireModelingExtensionDef;
import org.eclipse.sapphire.modeling.xml.annotations.XmlRootBinding;
import org.eclipse.sapphire.ui.def.ISapphireUiExtensionDef;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateImpl
@XmlRootBinding( elementName = "extension", namespace = "http://www.eclipse.org/sapphire/xmlns/extension" )

public interface ISapphireExtensionDef

    extends ISapphireModelingExtensionDef, ISapphireUiExtensionDef
    
{
    ModelElementType TYPE = new ModelElementType( ISapphireExtensionDef.class );

}
