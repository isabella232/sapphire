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

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ImpliedElementProperty;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public interface IPropertiesViewContributorDef

    extends IModelElement
    
{
    ModelElementType TYPE = new ModelElementType( IPropertiesViewContributorDef.class );

    // *** PropertiesViewContribution ***
    
    @Type( base = IPropertiesViewContributionDef.class )
    @Label( standard = "properties view contribution" )
    @XmlBinding( path = "properties-view" )

    ImpliedElementProperty PROP_PROPERTIES_VIEW_CONTRIBUTION = new ImpliedElementProperty( TYPE, "PropertiesViewContribution" );
    
    IPropertiesViewContributionDef getPropertiesViewContribution();

}
