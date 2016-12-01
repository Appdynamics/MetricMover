package com.appdynamics.tools.metricmover;

import io.searchbox.annotations.JestId;

public class Metric {

	@JestId
	private String documentId;
	private long startTimeInMillis;
	private long value;
	private long min;
	private long max;
	private long current;
	private long sum;
	private long count;
	private double stdDev;
	private long occurrences;
	private String bt;
	private String tier;
	private String app;

	public Metric(long count, long current, long val, long max, long min, long sum, double std, long occurrences,
			long time, String bt, String tier, String app) {
		this.count = count;
		this.current = current;
		this.value = val;
		this.max = max;
		this.min = min;
		this.sum = sum;
		this.stdDev = std;
		this.occurrences = occurrences;
		this.startTimeInMillis = time;
		this.bt = bt;
		this.tier = tier;
		this.app = app;
	}

	public long getStartTimeInMillis() {
		return startTimeInMillis;
	}

	public void setStartTimeInMillis(long startTimeInMillis) {
		this.startTimeInMillis = startTimeInMillis;
	}

	public long getValue() {
		return value;
	}

	public void setValue(long value) {
		this.value = value;
	}

	public long getMin() {
		return min;
	}

	public void setMin(long min) {
		this.min = min;
	}

	public long getMax() {
		return max;
	}

	public void setMax(long max) {
		this.max = max;
	}

	public long getCurrent() {
		return current;
	}

	public void setCurrent(long current) {
		this.current = current;
	}

	public long getSum() {
		return sum;
	}

	public void setSum(long sum) {
		this.sum = sum;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

	public double getStdDev() {
		return stdDev;
	}

	public void setStdDev(double stdDev) {
		this.stdDev = stdDev;
	}

	public long getOccurrences() {
		return occurrences;
	}

	public void setOccurrences(long occurrences) {
		this.occurrences = occurrences;
	}

	public String getDocumentId() {
		return documentId;
	}

	public void setDocumentId(String documentId) {
		this.documentId = documentId;
	}

	public String getBt() {
		return bt;
	}

	public void setBt(String bt) {
		this.bt = bt;
	}

	public String getTier() {
		return tier;
	}

	public void setTier(String tier) {
		this.tier = tier;
	}

	public String getApp() {
		return app;
	}

	public void setApp(String app) {
		this.app = app;
	}
	
}
