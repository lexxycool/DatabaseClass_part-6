package com.techelevator.projects.dao;

import com.techelevator.projects.model.Timesheet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.util.List;

public class JdbcTimesheetDaoTests extends EmployeeProjectsDaoTests {
    
    private static final Timesheet TIMESHEET_1 = new Timesheet(1L, 1L, 1L, 
            LocalDate.parse("2021-01-01"), 1.0, true, "Timesheet 1");
    private static final Timesheet TIMESHEET_2 = new Timesheet(2L, 1L, 1L,
            LocalDate.parse("2021-01-02"), 1.5, true, "Timesheet 2");
    private static final Timesheet TIMESHEET_3 = new Timesheet(3L, 2L, 1L,
            LocalDate.parse("2021-01-01"), 0.25, true, "Timesheet 3");
    private static final Timesheet TIMESHEET_4 = new Timesheet(4L, 2L, 2L,
            LocalDate.parse("2021-02-01"), 2.0, false, "Timesheet 4");
    
    private JdbcTimesheetDao sut;

    private Timesheet testTimesheet;
    
    @Before
    public void setup() {
        sut = new JdbcTimesheetDao(dataSource);
        testTimesheet = new Timesheet(99L, 2L, 1L, LocalDate.now(), 9.9,
                true, "Test Timesheet");
    }

    @Test
    public void getTimesheet_returns_correct_timesheet_for_id() {
        Timesheet timesheet = sut.getTimesheet(1L);
        Assert.assertNotNull("getTimesheet returned null", timesheet);
        assertTimesheetsMatch("getTimesheet returned wrong or partial data", TIMESHEET_1, timesheet);

        timesheet = sut.getTimesheet(4L);
        Assert.assertNotNull("getTimesheet returned null", timesheet);
        assertTimesheetsMatch("getTimesheet returned wrong or partial data", TIMESHEET_4, timesheet);
    }

    @Test
    public void getTimesheet_returns_null_when_id_not_found() {
        Timesheet timesheet = sut.getTimesheet(99L);
        Assert.assertNull("getTimesheet failed to return null for id not in database", timesheet);
    }

    @Test
    public void getTimesheetsByEmployeeId_returns_list_of_all_timesheets_for_employee() {
        List<Timesheet> timesheets = sut.getTimesheetsByEmployeeId(1L);
        Assert.assertEquals("getTimesheetsByEmployeeId returned wrong number of timesheets", 2, timesheets.size());
        assertTimesheetsMatch("getTimesheetsByEmployeeId returned wrong or partial data", TIMESHEET_1, timesheets.get(0));
        assertTimesheetsMatch("getTimesheetsByEmployeeId returned wrong or partial data", TIMESHEET_2, timesheets.get(1));

        timesheets = sut.getTimesheetsByEmployeeId(2L);
        Assert.assertEquals("getTimesheetsByEmployeeId returned wrong number of timesheets", 2, timesheets.size());
        assertTimesheetsMatch("getTimesheetsByEmployeeId returned wrong or partial data", TIMESHEET_3, timesheets.get(0));
        assertTimesheetsMatch("getTimesheetsByEmployeeId returned wrong or partial data", TIMESHEET_4, timesheets.get(1));
    }

    @Test
    public void getTimesheetsByProjectId_returns_list_of_all_timesheets_for_project() {
        List<Timesheet> timesheets = sut.getTimesheetsByProjectId(1L);
        Assert.assertEquals("getTimesheetsByProjectId returned wrong number of timesheets", 3, timesheets.size());
        assertTimesheetsMatch("getTimesheetsByProjectId returned wrong or partial data", TIMESHEET_1, timesheets.get(0));
        assertTimesheetsMatch("getTimesheetsByProjectId returned wrong or partial data", TIMESHEET_2, timesheets.get(1));
        assertTimesheetsMatch("getTimesheetsByProjectId returned wrong or partial data", TIMESHEET_3, timesheets.get(2));

        timesheets = sut.getTimesheetsByProjectId(2L);
        Assert.assertEquals("getTimesheetsByProjectId returned wrong number of timesheets", 1, timesheets.size());
        assertTimesheetsMatch("getTimesheetsByProjectId returned wrong or partial data", TIMESHEET_4, timesheets.get(0));
    }

    @Test
    public void createTimesheet_returns_timesheet_with_id_and_expected_values() {
        Timesheet createdTimesheet = sut.createTimesheet(testTimesheet);

        Assert.assertNotNull("createTimesheet returned null", createdTimesheet);

        long newId = createdTimesheet.getTimesheetId();
        Assert.assertTrue("createTimesheet failed to return a timesheet with an id", newId > 0);

        testTimesheet.setTimesheetId(newId);
        assertTimesheetsMatch("createTimesheet returned timesheet with wrong or partial data", testTimesheet, createdTimesheet);
    }

    @Test
    public void created_timesheet_has_expected_values_when_retrieved() {
        Timesheet createdTimesheet = sut.createTimesheet(testTimesheet);
        
        long newId = createdTimesheet.getTimesheetId();
        Timesheet retrievedTimesheet = sut.getTimesheet(newId);

        assertTimesheetsMatch("createTimesheet did not save correct data in database", createdTimesheet, retrievedTimesheet);
    }

    @Test
    public void updated_timesheet_has_expected_values_when_retrieved() {
        Timesheet timesheet = sut.getTimesheet(1L);
        timesheet.setEmployeeId(2L);
        timesheet.setProjectId(2L);
        timesheet.setDateWorked(LocalDate.now());
        timesheet.setHoursWorked(9.9);
        timesheet.setBillable(false);
        timesheet.setDescription("Test");

        sut.updateTimesheet(timesheet);

        Timesheet updatedTimesheet = sut.getTimesheet(1L);
        assertTimesheetsMatch("updateTimesheet failed to update all fields in database", timesheet, updatedTimesheet);
    }

    @Test
    public void deleted_timesheet_cant_be_retrieved() {
        sut.deleteTimesheet(1L);

        Timesheet timesheet = sut.getTimesheet(1L);
        Assert.assertNull("deleteTimesheet failed to remove timesheet from database", timesheet);

        List<Timesheet> timesheets = sut.getTimesheetsByEmployeeId(1L);
        Assert.assertEquals("deleteTimesheet removed the wrong number of timesheets", 1, timesheets.size());
        assertTimesheetsMatch("deleteTimesheet deleted wrong timesheet", TIMESHEET_2, timesheets.get(0));
    }

    @Test
    public void getBillableHours_returns_correct_total() {
        double total = sut.getBillableHours(1L, 1L);
        Assert.assertEquals("getBillableHours returned incorrect total for multiple timesheets",
                2.5, total, 0.001);

        total = sut.getBillableHours(2L, 1L);
        Assert.assertEquals("getBillableHours returned incorrect total for single timesheet",
                .25, total, 0.001);

        total = sut.getBillableHours(2L, 2L);
        Assert.assertEquals("getBillableHours failed to return 0 for no matching timesheets",
                0, total, 0.001);
    }

    //Note that the version of this method provided to students does not have the message parameter.
    private void assertTimesheetsMatch(String message, Timesheet expected, Timesheet actual) {
        Assert.assertEquals(message, expected.getTimesheetId(), actual.getTimesheetId());
        Assert.assertEquals(message, expected.getEmployeeId(), actual.getEmployeeId());
        Assert.assertEquals(message, expected.getProjectId(), actual.getProjectId());
        Assert.assertEquals(message, expected.getDateWorked(), actual.getDateWorked());
        Assert.assertEquals(message, expected.getHoursWorked(), actual.getHoursWorked(), 0.001);
        Assert.assertEquals(message, expected.isBillable(), actual.isBillable());
        Assert.assertEquals(message, expected.getDescription(), actual.getDescription());
    }

}
