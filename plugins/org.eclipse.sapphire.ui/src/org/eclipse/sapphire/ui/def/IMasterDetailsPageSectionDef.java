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

package org.eclipse.sapphire.ui.def;

import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValueProvider;
import org.eclipse.sapphire.modeling.xml.annotations.GenerateXmlBinding;
import org.eclipse.sapphire.ui.def.internal.MasterDetailsPageSectionDefLabelDefaultValueProvider;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateXmlBinding

public interface IMasterDetailsPageSectionDef

    extends ISapphireSectionDef
    
{
    ModelElementType TYPE = new ModelElementType( IMasterDetailsPageSectionDef.class );
    
    // *** Label ***
    
    @DefaultValueProvider( impl = MasterDetailsPageSectionDefLabelDefaultValueProvider.class )
    
    ValueProperty PROP_LABEL = new ValueProperty( TYPE, ISapphireSectionDef.PROP_LABEL );
    
}
