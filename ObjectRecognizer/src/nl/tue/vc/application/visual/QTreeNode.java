package nl.tue.vc.application.visual;

import java.util.ArrayList;

import javafx.scene.shape.Box;

public class QTreeNode {
	private final int currDepth; // the current depth of this node
	private final Vector center; // the center of this node
	private final QTreeNode[] nodes; // the child nodes

	private final ArrayList<CubeObject> objects; // the objects stored at this node

	public QTreeNode(float centerX, float centerY, float halfWidth, int stopDepth) {
		this.currDepth = stopDepth;

		// set Vector to current x-y-z values
		this.center = new Vector(centerX, centerY, 0.0f);

		this.objects = new ArrayList<CubeObject>();

		float offsetX = 0.0f;
		float offsetY = 0.0f;

		if (stopDepth > 0) {
			// create 4 child nodes as long as depth is still greater than 0
			this.nodes = new QTreeNode[4];

			// halve child nodes size
			float step = halfWidth * 0.5f;

			// loop through and create new child nodes
			for (int i = 0; i < 4; i++) {

				// compute the offsets of the child nodes
				offsetX = (((i & 1) == 0) ? step : -step);
				offsetY = (((i & 2) == 0) ? step : -step);

				nodes[i] = new QTreeNode(centerX + offsetX, centerY + offsetY, step, stopDepth - 1);
			}
		} else {
			this.nodes = null;
		}
	}

	public void insertObject(final CubeObject obj, final Box box) {
		objects.add(obj);
	}

	public void clean() {
		objects.clear();

		// clean children if available
		if (currDepth > 0) {
			for (int i = 0; i < 4; i++) {
				nodes[i].clean();
			}
		}
	}
}
