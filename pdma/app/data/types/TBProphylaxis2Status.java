package org.pepfar.pdma.app.data.types;

public  enum TBProphylaxis2Status {

	// Chưa bắt đầu
	NotYetStart(0),

	// Đang điều trị
	BeingTreated(1),

	// Ngưng điều trị
	DiscontinueTreatment(2),

	// Hoàn thành điều trị
	CompleteTreatment(3),

	// bỏ điều trị
	QuitTreatment(4);

	private  int number;

	private TBProphylaxis2Status(int number) {
		this.number = number;
	}

	public int getNumber() {
		return number;
	}
	
}
