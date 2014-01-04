/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ling Hao - initial implementation and ongoing maintenance
 *    Shenxue Zhou - Fix the new object and object type.
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.gef;

import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.sapphire.ui.diagram.editor.DiagramNodeTemplate;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public class NodeCreationFactory implements CreationFactory {
	private DiagramNodeTemplate nodeTemplate;
	
	public NodeCreationFactory(DiagramNodeTemplate nodeTemplate) {
		this.nodeTemplate = nodeTemplate;
	}

	public Object getNewObject() {
		return this.nodeTemplate;
	}

	public Object getObjectType() {
		return DiagramNodeTemplate.class;
	}

}
