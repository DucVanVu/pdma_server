package org.pepfar.pdma.app.data.types;

public enum HTSRiskGroupEnum {
	// UNKNOW
	UNKNOWN(-1,-1,"Không rõ"),
	// Người nghiện chích ma túy
	answer1(1,1,"Người nghiện chích ma túy"),

	// Nam giới có QHTD đồng giới
	answer2(2,2,"Nam giới có QHTD đồng giới"),

	// Người bán dâm
	answer3(3,4,"Người bán dâm"),
	
	// Người chuyển giới
	answer4(4,3,"Người chuyển giới"),
	
	// Vợ/chồng/BT/con đẻ ≤15 tuổi của NCH
	answer5(5,5,"Vợ/chồng/BT/con đẻ ≤15 tuổi của NCH"),
	
	//Bạn chích chung của người có HIV
	answer6(6,6,"Bạn chích chung của người có HIV"),
	
	//Vợ/chồng/bạn tình của người nghiện chích ma túy
	answer7(7,8,"Vợ/chồng/bạn tình của người nghiện chích ma túy"),
	
	//Vợ/bạn tình nữ của nam có QHTD đồng giới
	answer8(8,9,"Vợ/bạn tình nữ của nam có QHTD đồng giới"),
	
	//Người mua dâm	
	answer9(9,10,"Người mua dâm"),
	
	//Người có nhiều bạn tình
	answer10(10,11,"Người có nhiều bạn tình"),
	
	//Bệnh nhân nghi AIDS		
	answer11(11,12,"Bệnh nhân nghi AIDS"),
	
	//Bệnh nhân lao	
	answer12(12,13,"Bệnh nhân lao"),
	
	//Người mắc nhiễm trùng LTQĐTD	
	answer13(13,14,"Người mắc nhiễm trùng LTQĐTD"),
	
	//Phạm nhân		
	answer14(14,7,"Phạm nhân"),
	
	//Phụ nữ mang thai	
	answer15(15,15,"Phụ nữ mang thai"),
	
	//	Khác, ghi rõ :
	answer16(16,16,"Khác, ghi rõ");

	private final int number;
	private final int priority;
	private final String description;
	
	private HTSRiskGroupEnum(int number, int priority,String description) {
		this.number = number;
		this.description = description;
		this.priority = priority;
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