/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ling Hao - initial implementation and ongoing maintenance
 ******************************************************************************/
package org.eclipse.sapphire.ui.swt.gef.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.tools.MarqueeSelectionTool;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */
public class SapphireMarqueeSelectionTool extends MarqueeSelectionTool {

	@SuppressWarnings("rawtypes")
	@Override
	protected Collection calculateMarqueeSelectedEditParts() {
		Collection parts = super.calculateMarqueeSelectedEditParts();
		List<EditPart> trimEditParts = new ArrayList<EditPart>();
		for (Object object : parts) {
			EditPart part = (EditPart) object;
			if (isParentSelected(parts, part)) {
				trimEditParts.add(part);
			}
		}
		
		for (EditPart trim : trimEditParts) {
			parts.remove(trim);
		}
		return parts;
	}

	@SuppressWarnings("rawtypes")
	private boolean isParentSelected(Collection collection, EditPart editPart) {
		EditPart parent = editPart.getParent();
		while (parent != null) {
			if (collection.contains(parent)) {
				return true;
			}
			parent = parent.getParent();
		}
		return false;

	}
}
