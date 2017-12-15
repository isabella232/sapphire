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

package org.eclipse.sapphire.ui.form.editors.masterdetails.def;

import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Service;
import org.eclipse.sapphire.ui.def.ISapphireSectionDef;
import org.eclipse.sapphire.ui.form.editors.masterdetails.def.internal.MasterDetailsSectionDefLabelDefaultValueProvider;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@Label( standard = "section" )
@GenerateImpl

public interface IMasterDetailsSectionDef

    extends ISapphireSectionDef
    
{
    ModelElementType TYPE = new ModelElementType( IMasterDetailsSectionDef.class );
    
    // *** Label ***
    
    @Service( impl = MasterDetailsSectionDefLabelDefaultValueProvider.class )
    
    ValueProperty PROP_LABEL = new ValueProperty( TYPE, ISapphireSectionDef.PROP_LABEL );
    
}
