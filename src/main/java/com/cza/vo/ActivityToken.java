package com.cza.vo;

public class ActivityToken {
	
	/**
	 * 口令
	 */
	private String token;
	
	/**
	 * 剩余库存
	 */
	private Integer stock;
	/**
	 * 总库存
	 */
	private Integer totalStock;
	/**
	 * 口令权重
	 */
	private Integer weight;
	
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public Integer getStock() {
		return stock;
	}
	public void setStock(Integer stock) {
		this.stock = stock;
	}
	public Integer getTotalStock() {
		return totalStock;
	}
	public void setTotalStock(Integer totalStock) {
		this.totalStock = totalStock;
	}
	public Integer getWeight() {
		return weight;
	}
	public void setWeight(Integer weight) {
		this.weight = weight;
	}
	
	
}
