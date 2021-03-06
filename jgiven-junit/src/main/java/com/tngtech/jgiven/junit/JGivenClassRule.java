package com.tngtech.jgiven.junit;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import com.tngtech.jgiven.report.impl.CommonReportHelper;

public class JGivenClassRule extends TestWatcher {

    private CommonReportHelper commonReportHelper;

    public JGivenClassRule() {
        this.commonReportHelper = new CommonReportHelper();
    }

    public CommonReportHelper getCommonReportHelper() {
        return commonReportHelper;
    }

    @Override
    protected void finished( Description description ) {
        commonReportHelper.finishReport( ScenarioModelHolder.getInstance().getAndRemoveReportModel( description.getTestClass() ) );
    }
}
