package org.pepfar.pdma.app.data.types;

public enum PNSCaseContactRelationshipType {
	// UNKNOW
	UNKNOWN(-1,-1,"Không rõ"),	
	answer1(1,1,"Vợ/chồng"),
	answer2(2,2,"Bạn tình khác"),
	answer3(3,3,"Bạn TCMT chung"),
	answer4(4,4,"Con đẻ ≤15 tuổi của mẹ HIV+"),
	answer5(5,5,"Mẹ sinh con ≤15 tuổi HIV+");

	private final int number;
	private final int priority;
	private final String description;

	private PNSCaseContactRelationshipType(int number,int priority,String description) {
		this.number = number;
		this.priority = priority;
		this.description = description;
	}

	public int getNumber() {
		return number;
	}

	public String getDescription() {
		return description;
	}

	public int getPriority() {
		return priority;
	}
}
