package com.xy.lucky28.xjplu28.model;
import java.util.Date;

   /**
    * XjpLu28GaSession 实体类
    * author:	ch
    * createDate:	2017-03-23 18:27:22
    */ 


public class XjpLu28GaSession{
	private Integer sessionId;
	private String sessionNo;
	private Date startTime;
	private Date endTime;
	private String openStatus;
	private String openResult;
	private Date openTime;
	private Integer userCount;
	private Integer pointCount;
	private Integer sessionCash;
	private String countResult;//统计结果
	
	public void setSessionId(Integer sessionId){
	this.sessionId=sessionId;
	}
	public Integer getSessionId(){
		return sessionId;
	}
	public void setSessionNo(String sessionNo){
	this.sessionNo=sessionNo;
	}
	public String getSessionNo(){
		return sessionNo;
	}
	public void setStartTime(Date startTime){
	this.startTime=startTime;
	}
	public Date getStartTime(){
		return startTime;
	}
	public void setEndTime(Date endTime){
	this.endTime=endTime;
	}
	public Date getEndTime(){
		return endTime;
	}
	public void setOpenStatus(String openStatus){
	this.openStatus=openStatus;
	}
	public String getOpenStatus(){
		return openStatus;
	}
	public void setOpenResult(String openResult){
	this.openResult=openResult;
	}
	public String getOpenResult(){
		return openResult;
	}
	public void setOpenTime(Date openTime){
	this.openTime=openTime;
	}
	public Date getOpenTime(){
		return openTime;
	}
	public void setUserCount(Integer userCount){
	this.userCount=userCount;
	}
	public Integer getUserCount(){
		return userCount;
	}
	public void setPointCount(Integer pointCount){
	this.pointCount=pointCount;
	}
	public Integer getPointCount(){
		return pointCount;
	}
	public void setSessionCash(Integer sessionCash){
	this.sessionCash=sessionCash;
	}
	public Integer getSessionCash(){
		return sessionCash;
	}
	public String getCountResult() {
		return countResult;
	}
	public void setCountResult(String countResult) {
		this.countResult = countResult;
	}
	
}