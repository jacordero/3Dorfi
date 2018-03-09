package nl.tue.vc.application.visual;

public class QTree implements BroadPhase {
	private final QTreeNode node;

	// define a quadtree extends as width and height, define quadtree depth.
	public QTree(final float worldExtends, int worldDepth) {
		node = new QTreeNode(0, 0, worldExtends, worldDepth);
	}

	// insert a CubeObject at the quadtree
	public void insertObject(final CubeObject obj) {
		node.insertObject(obj, obj.box);
	}

	// clean the quadtree
	public void clean() {
		node.clean();
	}
}
