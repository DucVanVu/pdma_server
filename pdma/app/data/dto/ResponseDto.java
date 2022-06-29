package org.pepfar.pdma.app.data.dto;

import org.pepfar.pdma.app.data.types.YesNoNone;

public class ResponseDto<T> {
	private T responseObject;
	private YesNoNone code;
	private String message;
	public T getResponseObject() {
		return responseObject;
	}
	public void setResponseObject(T responseObject) {
		this.responseObject = responseObject;
	}
	public YesNoNone getCode() {
		return code;
	}
	public void setCode(YesNoNone code) {
		this.code = code;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
	
}
