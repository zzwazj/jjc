package com.xy.lucky28.bjlu28.model.dto;

import java.math.BigDecimal;
import java.util.Date;


public class BjLu28BetDTO {
    private String sessionNo;
    private Date startTime;
    private Date endTime;
    private BigDecimal totalPoint;
    private BigDecimal winCash;
    private BigDecimal payoff;
    public BjLu28BetDTO(){
    	
    }
    public BjLu28BetDTO(BigDecimal totalPoint,
			BigDecimal winCash,BigDecimal payoff){

		this.totalPoint = totalPoint;
		this.winCash = winCash;
		this.payoff = payoff;
	}
	public BjLu28BetDTO(String sessionNo,BigDecimal totalPoint,
			BigDecimal winCash,BigDecimal payoff){

		this.sessionNo = sessionNo;
		this.totalPoint = totalPoint;
		this.winCash = winCash;
		this.payoff = payoff;
	}
	public BjLu28BetDTO(String sessionNo,BigDecimal totalPoint,
			BigDecimal winCash,Date startTime,Date endTime,BigDecimal payoff){
		
		this.sessionNo = sessionNo;
		this.totalPoint = totalPoint;
		this.winCash = winCash;
		this.startTime = startTime;
		this.endTime = endTime;
		this.payoff = payoff;
	}

	/**
	 * @return the sessionNo
	 */
	public String getSessionNo() {
		return sessionNo;
	}
	/**
	 * @param sessionNo the sessionNo to set
	 */
	public void setSessionNo(String sessionNo) {
		this.sessionNo = sessionNo;
	}
	/**
	 * @return the endTime
	 */
	public Date getEndTime() {
		return endTime;
	}
	/**
	 * @param endTime the endTime to set
	 */
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	/**
	 * @return the startTime
	 */
	public Date getStartTime() {
		return startTime;
	}
	/**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	/**
	 * @return the totalPoint
	 */
	public BigDecimal getTotalPoint() {
		return totalPoint;
	}
	/**
	 * @param totalPoint the totalPoint to set
	 */
	public void setTotalPoint(BigDecimal totalPoint) {
		this.totalPoint = totalPoint;
	}
	/**
	 * @return the winCash
	 */
	public BigDecimal getWinCash() {
		return winCash;
	}
	/**
	 * @param winCash the winCash to set
	 */
	public void setWinCash(BigDecimal winCash) {
		this.winCash = winCash;
	}
	public BigDecimal getPayoff() {
		return payoff;
	}
	public void setPayoff(BigDecimal payoff) {
		this.payoff = payoff;
	}
	
	
}
