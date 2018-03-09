package nl.tue.vc.application.visual;

public interface BroadPhase {
	public void insertObject(final CubeObject obj);
	public void clean();
}
