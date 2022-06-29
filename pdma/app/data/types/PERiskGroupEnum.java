package org.pepfar.pdma.app.data.types;

public enum PERiskGroupEnum {
	
	UNKNOWN(-1,-1,"Không rõ"),	
	answer1(1,1,"Người nghiện chích ma túy"),
	answer2(2,2,"Nam giới có QHTD đồng giới"),
	answer3(3,4,"Người bán dâm"),
	answer4(4,3,"Người chuyển giới"),
	answer5(5,5,"Vợ/chồng/BT/con đẻ ≤15 tuổi của NCH"),
	answer6(6,6,"Bạn chích chung của NCH");

	private final int number;
	private final int priority;
	private final String description;

	private PERiskGroupEnum(int number,int priority,String description) {
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
