package org.pepfar.pdma.app.data.dto;

public class ReleaseHistoryDto extends AuditableEntityDto
{

	private Long id;

	private String versionName;

	private String changes;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	public String getChanges() {
		return changes;
	}

	public void setChanges(String changes) {
		this.changes = changes;
	}

}
