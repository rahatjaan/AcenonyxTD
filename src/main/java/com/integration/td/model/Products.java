package com.integration.td.model;

public class Products {
	private int productId;
	private int productSizeId;
	private int productQuantity;
	private Modifiers modifiers;
	private Parameters parameters;
	public int getProductId() {
		return productId;
	}
	public void setProductId(int productId) {
		this.productId = productId;
	}
	public int getProductSizeId() {
		return productSizeId;
	}
	public void setProductSizeId(int productSizeId) {
		this.productSizeId = productSizeId;
	}
	public int getProductQuantity() {
		return productQuantity;
	}
	public void setProductQuantity(int productQuantity) {
		this.productQuantity = productQuantity;
	}
	public Modifiers getModifiers() {
		return modifiers;
	}
	public void setModifiers(Modifiers modifiers) {
		this.modifiers = modifiers;
	}
	public Parameters getParameters() {
		return parameters;
	}
	public void setParameters(Parameters parameters) {
		this.parameters = parameters;
	}
	
}
