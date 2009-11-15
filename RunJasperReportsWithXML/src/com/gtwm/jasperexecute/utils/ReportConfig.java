package com.gtwm.jasperexecute.utils;

//Copyright 2007 Charly CLAIRMONT

//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at

//http://www.apache.org/licenses/LICENSE-2.0

//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamWriter;
import com.gtwm.jasperexecute.beans.*;


public class ReportConfig {
	
	private JobsReports jobReportConf;
	private String xmlBeanJobReportFile;
	
	
	public ReportConfig(){
		jobReportConf = new JobsReports();
		xmlBeanJobReportFile = new String();
	}
	
	public ReportConfig(String xmlBeanFile){
		xmlBeanJobReportFile = xmlBeanFile;
	}
	
	public void readConfig(){
		JAXBContext jc;
		// unmarshal from foo.xml
		Unmarshaller u;
		try {
			jc = JAXBContext.newInstance( "com.gtwm.jasperexecute.beans" );
			u = jc.createUnmarshaller();
			jobReportConf = (JobsReports)u.unmarshal( new File( xmlBeanJobReportFile) );
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void saveConfig(){
		JAXBContext jc;
		// unmarshal from foo.xml
		Marshaller m;
		try {
			jc = JAXBContext.newInstance( "com.gtwm.jasperexecute.beans" );
			m = jc.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			m.marshal(jobReportConf, new FileOutputStream( xmlBeanJobReportFile));
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//TODO validate the schema
	
	// getters and setters
	public String getXmlBeanJobReportFile() {
		return xmlBeanJobReportFile;
	}

	public void setXmlBeanJobReportFile(String xmlBeanJobReportFile) {
		this.xmlBeanJobReportFile = xmlBeanJobReportFile;
	}

	public JobsReports getJobReportConf() {
		return jobReportConf;
	}

	public void setJobReportConf(JobsReports jobReportConf) {
		this.jobReportConf = jobReportConf;
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		
		sb.append("name: ").append(jobReportConf.getName());
		sb.append("\n");
		sb.append("emailSubject: ").append(jobReportConf.getEmailSubject());
		sb.append("\n");
		sb.append("emailFrom: ").append(jobReportConf.getEmailFrom());
		sb.append("\n");
		sb.append("emailToList: ").append(jobReportConf.getEmailToList());
		sb.append("\n");
		sb.append("emailTemplate: ");
		sb.append("\n");
		sb.append("\tname: ").append(jobReportConf.getEmailTemplate().getName());
		sb.append("\n");
		sb.append("\ttemplate: ").append(jobReportConf.getEmailTemplate().getTemplate());
		sb.append("\n");
		sb.append("sources: ");
		sb.append("\n");
		for(int i=0; i < jobReportConf.getSources().getSource().size(); i++){
			sb.append("\tsource: ");
			sb.append("\t\tname: ").append(jobReportConf.getSources().getSource().get(i).getName());
			sb.append("\n");
			sb.append("\t\ttype: ").append(jobReportConf.getSources().getSource().get(i).getType());
			sb.append("\n");
			sb.append("\t\tdbprovider: ").append(jobReportConf.getSources().getSource().get(i).getDbprovider());
			sb.append("\n");
			sb.append("\t\tdbhostname: ").append(jobReportConf.getSources().getSource().get(i).getDbhostname());
			sb.append("\n");
			sb.append("\t\tdbport: ").append(jobReportConf.getSources().getSource().get(i).getDbPort());
			sb.append("\n");
			sb.append("\t\tdbname: ").append(jobReportConf.getSources().getSource().get(i).getDbname());
			sb.append("\n");
			sb.append("\t\tdbuser: ").append(jobReportConf.getSources().getSource().get(i).getDbuser());
			sb.append("\n");
			sb.append("\t\tdbpassword: ").append(jobReportConf.getSources().getSource().get(i).getDbpassword());
			sb.append("\n");
		}
		sb.append("\n");
		sb.append("reports: ");
		sb.append("\n");
		for(int i=0; i < jobReportConf.getReports().getReport().size(); i++){
			sb.append("\treport: ");
			sb.append("\t\toutputName: ").append(jobReportConf.getReports().getReport().get(i).getOutputName());
			sb.append("\n");
			sb.append("\t\tjrxmlFile: ").append(jobReportConf.getReports().getReport().get(i).getJrxmlFile());
			sb.append("\n");
			sb.append("\t\tsource: ").append(jobReportConf.getReports().getReport().get(i).getSource());
			sb.append("\n");
			
			if(jobReportConf.getReports().getReport().get(i).getParameters().getParameter() != null)
				for(int j=0; j < jobReportConf.getReports().getReport().get(i).getParameters().getParameter().size(); j++){
					sb.append("\t\tparameter: ");
					sb.append("\t\t\tname: ").append(jobReportConf.getReports().getReport().get(i).getParameters().getParameter().get(j).getName());
					sb.append("\n");
					sb.append("\t\t\ttype: ").append(jobReportConf.getReports().getReport().get(i).getParameters().getParameter().get(j).getType());
					sb.append("\n");
					sb.append("\t\t\tvalue: ").append(jobReportConf.getReports().getReport().get(i).getParameters().getParameter().get(j).getValue());
					sb.append("\n");
					
				}
			
		}
		sb.append("\n");
		sb.append("mailSmtp: ");
		sb.append("\n");
		sb.append("\tserverSmtpName: ").append(jobReportConf.getMailSmtp().getServerSmtpName());
		sb.append("\n");
		sb.append("\thostname: ").append(jobReportConf.getMailSmtp().getHostname());
		sb.append("\n");
		sb.append("\tlogin: ").append(jobReportConf.getMailSmtp().getLogin());
		sb.append("\n");
		sb.append("\tpassword: ").append(jobReportConf.getMailSmtp().getPassword());
		sb.append("\n");
		sb.append("outputFolder: ").append(jobReportConf.getOutputFolder());
		
		
		return sb.toString();
	}
	
}
