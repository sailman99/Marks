package com.trace.shared;

import java.io.Serializable;

public class ResultMessage implements Serializable{
	/**
	 * 
	 */
	//private static final long serialVersionUID = -490551590855351544L;
	private static final long serialVersionUID = -490551590855351564L;
	private boolean ok;
	private String uppublic;//公共版本控制
	private String upprivate;//私有版本控制
	private int userid;//私有userid
	private String privatesql;//私有升级SQL
	private String check;//判断是由哪返回的
	public boolean isOk() {
		return ok;
	}

	public void setOk(boolean ok) {
		this.ok = ok;
	}

	public String getUppublic() {
		return uppublic;
	}

	public void setUppublic(String uppublic) {
		this.uppublic = uppublic;
	}

	public String getUpprivate() {
		return upprivate;
	}

	public void setUpprivate(String upprivate) {
		this.upprivate = upprivate;
	}

	
	public int getUserid() {
		return userid;
	}

	public void setUserid(int userid) {
		this.userid = userid;
	}

	public String getPrivatesql() {
		return privatesql;
	}

	public void setPrivatesql(String privatesql) {
		this.privatesql = privatesql;
	}

	public String getCheck() {
		return check;
	}

	public void setCheck(String check) {
		this.check = check;
	}
	
	
}
