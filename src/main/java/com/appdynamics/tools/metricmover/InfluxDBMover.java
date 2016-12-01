package com.appdynamics.tools.metricmover;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.appdynamics.appdrestapi.RESTAccess;
import org.appdynamics.appdrestapi.data.BusinessTransaction;
import org.appdynamics.appdrestapi.data.BusinessTransactions;
import org.appdynamics.appdrestapi.data.MetricData;
import org.appdynamics.appdrestapi.data.MetricDatas;
import org.appdynamics.appdrestapi.data.MetricValue;
import org.appdynamics.appdrestapi.data.MetricValues;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDB.ConsistencyLevel;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InfluxDBMover implements Mover {

	private static final Logger logger = LoggerFactory.getLogger(InfluxDBMover.class);
	
	public void move(Properties props) throws Exception {
		String controller = props.getProperty("controller_url");
		String port = props.getProperty("controller_port");
		boolean useSSL = Boolean.parseBoolean(props.getProperty("controller_useSSL"));
		String user = props.getProperty("controller_user");
		String passwd = props.getProperty("controller_passwd");
		String account = props.getProperty("controller_account");
		String app = props.getProperty("controller_app");

		String dbUrl = props.getProperty("influxDB_url");
		String dbPort = props.getProperty("influxDB_port");
		String dbuser = props.getProperty("influxDB_user");
		String dbpasswd = props.getProperty("influxDB_passwd");
		String dbName = props.getProperty("influxDB_db_name");
		long start = Long.parseLong(props.getProperty("controller_start_time"));
		long end = Long.parseLong(props.getProperty("controller_end_time"));
		
		InfluxDB influxDB = createDatabase(dbUrl, dbPort, dbuser, dbpasswd, dbName);
		
		RESTAccess access = new RESTAccess(controller, port, useSSL, user, passwd, account);
		BusinessTransactions bts = access.getBTSForApplication(app);
		BatchPoints batchPoints = BatchPoints.database(dbName).retentionPolicy("autogen").consistency(ConsistencyLevel.ALL).build();
		for (BusinessTransaction bt : bts.getBusinessTransactions()) {
			for (int i = 0; i < 10; i++) {
				MetricDatas mDatas = access.getRESTBTMetricQuery(i, app, bt.getTierName(), bt.getName(), start, end);
				if (mDatas != null) {
					ArrayList<MetricData> mDataList = mDatas.getMetric_data();
					if (mDataList != null) {
						for (MetricData mData : mDataList) {
							String freq = mData.getFrequency();
							int mId = mData.getMetricId();
							String mPath = mData.getMetricPath();
							String metricName = getMeasurement(mPath);					
							//measurement: metricName (from mPath); time: time 
							//Tag: bt_name, tier, app
							//Field: count, current, val, max, min
							ArrayList<MetricValues> mValss = mData.getMetricValues();
							if (mValss != null) {
								for (MetricValues mVals : mValss) {
									ArrayList<MetricValue> mVal = mVals.getMetricValue();
									if (mVal != null) {
										for (MetricValue mv : mVal) {
											long count = mv.getCount();
											long current = mv.getCurrent();
											long val = mv.getValue();
											long max = mv.getMax();
											long min = mv.getMin();
											long sum = mv.getSum();
											double std = mv.getStdDev();
											long occurrences = mv.getOccurrences();
											
											long time = mv.getStartTimeInMillis();
											Point point = Point.measurement(metricName).time(time, TimeUnit.MILLISECONDS)
											.addField("count", count).addField("current", current).addField("val", val).addField("max", max).addField("min", min)
											.addField("sum", sum).addField("std", std).addField("occurrences", occurrences)
											.tag("bt", bt.getName()).tag("tier", bt.getTierName()).tag("app", app)
											.build();
											batchPoints.point(point);
										}										
									}
								}								
							}
						}						
					}
				} else {
					logger.info("Empty data for " + bt.getName());
				}
			}
			influxDB.write(batchPoints);
			logger.info("Done for BT " + bt.getName() + " , " + bt.getTierName());
		}

	}

	private static InfluxDB createDatabase(String dbUrl, String dbPort, String user, String passwd, String dbName) {
		InfluxDB influxDB = InfluxDBFactory.connect("http://" + dbUrl + ":" + dbPort, user, passwd);
		List<String> dbs = influxDB.describeDatabases();
		if (!dbs.contains(dbName)) {
			influxDB.createDatabase(dbName);
		}
		return influxDB;
	}
	
	private static String getMeasurement(String mPath) {
		//Business Transaction Performance|Business Transactions|Rocklin|search|Average Block Time (ms)
		if (mPath.contains("|")) {
			return mPath.substring(mPath.lastIndexOf("|") + 1);			
		} else {
			return mPath;
		}
	}
	
//	public static void main5(String[] args) {
//		InfluxDB influxDB = InfluxDBFactory.connect("http://localhost:8086", "root", "root");
//		String dbName = "aTimeSeries";
//		influxDB.deleteDatabase(dbName);		
//	}
	
}