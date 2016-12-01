<h1>Metric Mover</h1>

A utility that moves metric data from AppDynamics Controller for a time period to Elasticsearch and InfluxDB.

<h2>Prerequisites</h2>

- Java 1.7 JDK
- Maven 
- Elasticsearch/Kibana or InfluxDB/Grafana

<h2>Steps to use</h2>

1. Download the zip file from
https://github.com/Appdynamics/MetricMover

2. Unzip the file on your local machine

3. cd into the unzipped folder MetricMover

4. Build the project<br />
  a. mvn clean<br />
  b. mvn install<br />

5. Use a terminal to<br /> 
  a. cd bin<br />
  b. chmod +x *.sh<br />
  c. ./MetricMover.sh <br />

<h2>Current Support</h2>
1. 4.1 Controller and BT metrics



