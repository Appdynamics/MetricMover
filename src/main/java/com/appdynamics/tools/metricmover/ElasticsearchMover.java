package com.appdynamics.tools.metricmover;

import java.util.ArrayList;
import java.util.Properties;

import org.appdynamics.appdrestapi.RESTAccess;
import org.appdynamics.appdrestapi.data.BusinessTransaction;
import org.appdynamics.appdrestapi.data.BusinessTransactions;
import org.appdynamics.appdrestapi.data.MetricData;
import org.appdynamics.appdrestapi.data.MetricDatas;
import org.appdynamics.appdrestapi.data.MetricValue;
import org.appdynamics.appdrestapi.data.MetricValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Bulk;
import io.searchbox.core.Index;
import io.searchbox.indices.CreateIndex;
import io.searchbox.indices.mapping.PutMapping;

public class ElasticsearchMover implements Mover {
	
	private static final Logger logger = LoggerFactory.getLogger(ElasticsearchMover.class);
	
	public void move(Properties props) throws Exception {
		String controller = props.getProperty("controller_url");
		String port = props.getProperty("controller_port");
		boolean useSSL = Boolean.parseBoolean(props.getProperty("controller_useSSL"));
		String user = props.getProperty("controller_user");
		String passwd = props.getProperty("controller_passwd");
		String account = props.getProperty("controller_account");
		String app = props.getProperty("controller_app");

		String esUrl = props.getProperty("elasticsearch_url");
		String esPort = props.getProperty("elasticsearch_port");
		String esuser = props.getProperty("elasticsearch_user");
		String espass = props.getProperty("elasticsearch_passwd");
		String index = props.getProperty("elasticsearch_index");
		long start = Long.parseLong(props.getProperty("controller_start_time"));
		long end = Long.parseLong(props.getProperty("controller_end_time"));
		
		// Construct a new Jest client according to configuration via factory
		JestClientFactory factory = new JestClientFactory();
		factory.setHttpClientConfig(new HttpClientConfig.Builder("http://" + esUrl + ":" + esPort).defaultCredentials(esuser, espass).multiThreaded(true).build());			
		JestClient client = factory.getObject();

		RESTAccess access = new RESTAccess(controller, port, useSSL, user, passwd, account);
		BusinessTransactions bts = access.getBTSForApplication(app);
		client.execute(new CreateIndex.Builder(index).build());

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
							Bulk.Builder bulkBuilder = new Bulk.Builder().defaultIndex(index).defaultType(metricName);
							PutMapping putMapping = new PutMapping.Builder(index, metricName,
									"{ \"" + metricName
											+ "\" : { \"properties\" : { "
														+ "\"startTimeInMillis\": { \"type\": \"date\""
																			+ ",\"format\": \"epoch_millis\""
																			+ " }"
														+ " } "
											+ "} "
									+ "}")
													.build();
							try {
								client.execute(putMapping);								
							} catch (Exception e) {
								client.execute(putMapping);
							}
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
											Metric met = new Metric(count, current, val, max, min, sum, std, occurrences, time, bt.getName(), bt.getTierName(), app);
											Index idx = new Index.Builder(met).index(index).type(metricName).build();
											bulkBuilder.addAction(idx);
										}
									}
								}
							}
							Bulk bulk = bulkBuilder.build();
							client.execute(bulk);
						}
					}
				} else {
					logger.info("Empty data for " + bt.getName());
				}
			}
			logger.info("Done for " + bt.getName() + " - " + bt.getTierName());
//			Thread.sleep(500);
		}

	}

	private static String getMeasurement(String mPath) {
		// Business Transaction Performance|Business Transactions|Tier|search|Average Block Time (ms)
		if (mPath.contains("|")) {
			return mPath.substring(mPath.lastIndexOf("|") + 1).toLowerCase();
		} else {
			return mPath.toLowerCase();
		}
	}

//	public static void main1(String[] args) throws Exception {
//	String esUrl = "192.168.64.57";
//	//String esUrl = "localhost";
//	String esPort = "9200";
//
//	// Construct a new Jest client according to configuration via factory
//	JestClientFactory factory = new JestClientFactory();
//	factory.setHttpClientConfig(new HttpClientConfig.Builder("http://" + esUrl + ":" + esPort).multiThreaded(true).build());
//	JestClient client = factory.getObject();
//
//	String index = "appdbts";
//	client.execute(new DeleteIndex.Builder(index).build());
//}	
//
}