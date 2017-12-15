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

package org.eclipse.sapphire.samples.calendar;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlRootBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateImpl

@XmlRootBinding( namespace = "http://www.eclipse.org/sapphire/samples/calendar",
                 schemaLocation = "http://www.eclipse.org/sapphire/samples/calendar/1.0",
                 defaultPrefix = "cal",
                 elementName = "calendar" )

public interface ICalendar

    extends IModelElement
    
{
    ModelElementType TYPE = new ModelElementType( ICalendar.class );
    
    // *** Events ***

    @Type( base = IEvent.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "event", type = IEvent.class ) )
    
    ListProperty PROP_EVENTS = new ListProperty( TYPE, "Events" );
    
    ModelElementList<IEvent> getEvents();
    
}
