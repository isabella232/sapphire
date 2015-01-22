/******************************************************************************
 * Copyright (c) 2015 Oracle
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
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.CountConstraint;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.Image;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Service;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;
import org.eclipse.sapphire.ui.def.MarginPresentation;
import org.eclipse.sapphire.ui.def.Orientation;
import org.eclipse.sapphire.ui.forms.internal.SplitFormDefImageService;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@Label( standard = "split form" )
@Image( path = "SplitFormDef.png" )
@Service( impl = SplitFormDefImageService.class )
@XmlBinding( path = "split-form" )

public interface SplitFormDef extends FormComponentDef, MarginPresentation
{
    ElementType TYPE = new ElementType( SplitFormDef.class );
    
    // *** Orientation ***
    
    @Type( base = Orientation.class )
    @Label( standard = "orientation" )
    @DefaultValue( text = "horizontal" )
    @XmlBinding( path = "orientation" )
    
    ValueProperty PROP_ORIENTATION = new ValueProperty( TYPE, "Orientation" );
    
    Value<Orientation> getOrientation();
    void setOrientation( String value );
    void setOrientation( Orientation value );
    
    // *** Sections ***
    
    @Type( base = SplitFormSectionDef.class )
    @Label( standard = "sections" )
    @CountConstraint( min = 2 )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "block", type = SplitFormSectionDef.class ) )
    
    ListProperty PROP_SECTIONS = new ListProperty( TYPE, "Sections" );
    
    ElementList<SplitFormSectionDef> getSections();
    
}
