/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ling Hao - initial implementation and ongoing maintenance
 ******************************************************************************/
package org.eclipse.sapphire.ui.swt.gef.tools;

import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.tools.DragEditPartsTracker;
import org.eclipse.swt.SWT;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public class SapphireNodeDragEditPartsTracker extends DragEditPartsTracker {
	
	protected final static String LAST_EDIT_PART = "LAST_EDIT_PART";

	public SapphireNodeDragEditPartsTracker(EditPart sourceEditPart) {
		super(sourceEditPart);
	}

	@SuppressWarnings("unchecked")
	protected void performSelection() {
		if (hasSelectionOccurred())
			return;
		setFlag(FLAG_SELECTION_PERFORMED, true);
		EditPartViewer viewer = getCurrentViewer();
		List<EditPart> selectedObjects = viewer.getSelectedEditParts();

		if (getCurrentInput().isModKeyDown(SWT.MOD1)) {
			EditPart deselectPart = getDeselectPart(selectedObjects, getSourceEditPart());
			if (deselectPart != null) {
				viewer.deselect(deselectPart);
			} else {
				viewer.appendSelection(getSourceEditPart());
				removeChildrenDuplicates(getSourceEditPart());
			}
			viewer.setProperty(LAST_EDIT_PART, getSourceEditPart());
		} else if (getCurrentInput().isShiftKeyDown()) {
			viewer.appendSelection(getSourceEditPart());

			viewer.setProperty(LAST_EDIT_PART, getSourceEditPart());
		}
		else {
			viewer.select(getSourceEditPart());

			viewer.setProperty(LAST_EDIT_PART, getSourceEditPart());
		}
	}
	
	protected EditPart getDeselectPart(List<EditPart> selectedObjects, EditPart sourceEditPart) {
		EditPart parent = sourceEditPart;
		while (parent != null) {
			if (selectedObjects.contains(parent)) {
				return parent;
			}
			parent = parent.getParent();
		}
		return null;
	}
	
	@SuppressWarnings("rawtypes")
	protected void removeChildrenDuplicates(EditPart sourceEditPart) {
		final EditPartViewer viewer = getCurrentViewer();
		List selectedParts = viewer.getSelectedEditParts();

		for (Object child : sourceEditPart.getChildren()) {
			EditPart childPart = (EditPart)child;
			if (selectedParts.contains(child)) {
				viewer.deselect(childPart);
			}
			removeChildrenDuplicates(childPart);
		}
	}
}
