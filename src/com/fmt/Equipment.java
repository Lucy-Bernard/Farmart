package com.fmt;

import framework.fmt.Item;

// This class represents an Equipment item that is a subclass of Item and has an item code, type, name and model

public class Equipment extends Item {
	private String model;
	private String type;

	public Equipment(String itemCode, String type, String itemName, String model) {
		super(itemCode, itemName);
		this.model = model;
		this.type = type;
	}

	public String getModel() {
		return model;
	}

	public String getType() {
		return this.type;
	}

	public void setModel(String model) {
		this.model = model;
	}

	@Override
	public double getSubtotal() {
		return 0.0;
	}

	@Override
	public double getTaxes() {
		return 0.0;
	}

}
