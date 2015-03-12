/**
 * 
 */
package ca.nengo.model.impl;

import ca.nengo.model.*;
import ca.nengo.util.ScriptGenException;

import java.util.HashMap;

/**
 * A Cloneable Node for testing copy&paste / drag&drop.
 * 
 * @author Bryan Tripp
 */
public class MockNode implements Node<Node>, Cloneable {

	private static final long serialVersionUID = 1L;

	private String myDocumentation;
	private String myName;
	private SimulationMode myMode;

	public MockNode(String name) {
		myName = name;
		myMode = SimulationMode.DEFAULT;
	}

	/**
	 * @see ca.nengo.model.Node#getDocumentation()
	 */
	public String getDocumentation() {
		return myDocumentation;
	}

	/**
	 * @see ca.nengo.model.Node#getName()
	 */
	public String getName() {
		return myName;
	}

	/**
	 * @see ca.nengo.model.Node#getSource(java.lang.String)
	 */
	public NSource getSource(String name) throws StructuralException {
		return null;
	}

	/**
	 * @see ca.nengo.model.Node#getSources()
	 */
	public NSource[] getSources() {
		return new NSource[0];
	}

	/**
	 * @see ca.nengo.model.Node#getTarget(java.lang.String)
	 */
	public NTarget getTarget(String name) throws StructuralException {
		return null;
	}

	/**
	 * @see ca.nengo.model.Node#getTargets()
	 */
	public NTarget[] getTargets() {
		return new NTarget[0];
	}

	/**
	 * @see ca.nengo.model.Node#run(float, float)
	 */
	public void run(float startTime, float endTime) throws SimulationException {
	}

	/**
	 * @see ca.nengo.model.Node#setDocumentation(java.lang.String)
	 */
	public void setDocumentation(String text) {
		myDocumentation = text;
	}

	/**
	 * @see ca.nengo.model.Resettable#reset(boolean)
	 */
	public void reset(boolean randomize) {
	}

	/**
	 * @see ca.nengo.model.SimulationMode.ModeConfigurable#getMode()
	 */
	public SimulationMode getMode() {
		return myMode;
	}

	/**
	 * @see ca.nengo.model.SimulationMode.ModeConfigurable#setMode(ca.nengo.model.SimulationMode)
	 */
	public void setMode(SimulationMode mode) {
		myMode = mode;
	}

	/**
	 * @see ca.nengo.util.VisiblyChanges#addChangeListener(ca.nengo.util.VisiblyChanges.Listener)
	 */
	public void addChangeListener(Listener listener) {
	}

	/**
	 * @see ca.nengo.util.VisiblyChanges#removeChangeListener(ca.nengo.util.VisiblyChanges.Listener)
	 */
	public void removeChangeListener(Listener listener) {
	}

	@Override
	public MockNode clone() throws CloneNotSupportedException {
		return (MockNode) super.clone();
	}

	public static void main(String[] args) {
		MockNode node = new MockNode("test");
		node.setDocumentation("documentation");
		node.setMode(SimulationMode.APPROXIMATE);

		try {
			MockNode other = node.clone();
			System.out.println("equals? " + (node == other));
			System.out.println(other.getName());
			System.out.println(other.getDocumentation());
			System.out.println(other.getMode());
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
	}

	public void setName(String myName) {
		this.myName = myName;
	}

	public Node[] getChildren() {
		return new Node[0];
	}

	public String toScript(HashMap<String, Object> scriptData) throws ScriptGenException {

		return "";
	}
}
