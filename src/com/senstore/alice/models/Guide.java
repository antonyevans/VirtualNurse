package com.senstore.alice.models;

public class Guide {
	String simpleName;
	String fileName;
	String startOption;

	@Override
	public String toString() {
		return "Guide [simpleName=" + simpleName + ", fileName=" + fileName
				+ ", startOption=" + startOption + "]";
	}

	public String getSimpleName() {
		return simpleName;
	}

	public void setSimpleName(String simpleName) {
		this.simpleName = simpleName;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getStartOption() {
		return startOption;
	}

	public void setStartOption(String startOption) {
		this.startOption = startOption;
	}

}
