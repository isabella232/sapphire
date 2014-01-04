/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.forms;

import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.ReferenceValue;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Image;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.MustExist;
import org.eclipse.sapphire.modeling.annotations.PossibleValues;
import org.eclipse.sapphire.modeling.annotations.Reference;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.Service;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.FoldingXmlValueBindingImpl;
import org.eclipse.sapphire.modeling.xml.annotations.CustomXmlValueBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;
import org.eclipse.sapphire.ui.def.ISapphireParam;
import org.eclipse.sapphire.ui.def.internal.FormPartIncludeReferenceService;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@Label( standard = "form component reference" )
@Image( path = "FormComponentRef.png" )
@XmlBinding( path = "include" )

public interface FormComponentRef extends FormComponentDef
{
    ElementType TYPE = new ElementType( FormComponentRef.class );
    
    // *** Part ***

    @Reference( target = FormComponentDef.class )
    @Service( impl = FormPartIncludeReferenceService.class )
    @Label( standard = "part" )
    @PossibleValues( property = "/PartDefs/Id" )
    @Required
    @MustExist
    @CustomXmlValueBinding( impl = FoldingXmlValueBindingImpl.class, params = "part" )
    
    ValueProperty PROP_PART = new ValueProperty( TYPE, "Part" );
    
    ReferenceValue<String,FormComponentDef> getPart();
    void setPart( String part );
    
    // *** Params ***
    
    @Label( standard = "params" )
    @Type( base = ISapphireParam.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "param", type = ISapphireParam.class ) )
    
    ListProperty PROP_PARAMS = new ListProperty( TYPE, "Params" );
    
    ElementList<ISapphireParam> getParams();

}
