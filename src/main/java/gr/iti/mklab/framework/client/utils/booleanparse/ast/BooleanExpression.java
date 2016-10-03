package gr.iti.mklab.framework.client.utils.booleanparse.ast;

import java.util.List;

public abstract class BooleanExpression extends Node {

	public List<Node> operands;

	public List<Node> getOperands() {
		return operands;
	}
	public void setOperands(List<Node> operands) {
		this.operands = operands;
	}
	
}