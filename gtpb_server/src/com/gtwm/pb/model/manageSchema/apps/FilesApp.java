package com.gtwm.pb.model.manageSchema.apps;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import com.gtwm.pb.model.interfaces.AppFilesInfo;
import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.model.manageSchema.BaseReportDefn;
import com.gtwm.pb.util.RandomString;
import com.gtwm.pb.util.Enumerations.AppType;

@Entity
public class FilesApp extends AbstractApp implements AppFilesInfo {

	public FilesApp(String colour, BaseReportInfo report) {
		super.setColour(colour);
		super.setInternalAppName(RandomString.generate());
		super.setAppType(AppType.FILES);
		this.setReport(report);
	}

	@Transient
	@Override
	public String getAppName() {
		return this.getReport().getParentTable().getSimpleName();
	}

	@ManyToOne(targetEntity = BaseReportDefn.class)
	public BaseReportInfo getReport() {
		return this.report;
	}

	public void setReport(BaseReportInfo report) {
		this.report = report;		
	}
	
	private BaseReportInfo report = null;
}
