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
    private Timesheet time;


    @Before
    public void setup() {
        sut = new JdbcTimesheetDao(dataSource);
        time = new Timesheet(5L, 1L, 2L,
                LocalDate.parse("2021-01-05"), 2.0, true, "Timesheet 5");
    }

    @Test
    public void getTimesheet_returns_correct_timesheet_for_id() {
            Timesheet time1 = sut.getTimesheet(1L);

            assertTimesheetsMatch(TIMESHEET_1, time1);

    }

    @Test
    public void getTimesheet_returns_null_when_id_not_found() {
        Timesheet time2 = sut.getTimesheet(10L);

        Assert.assertNull(time2);
    }

    @Test
    public void getTimesheetsByEmployeeId_returns_list_of_all_timesheets_for_employee() {
       List<Timesheet> employeeTime = sut.getTimesheetsByEmployeeId(1L);
       Assert.assertEquals(2, employeeTime.size());

       assertTimesheetsMatch(TIMESHEET_1, employeeTime.get(0));
       assertTimesheetsMatch(TIMESHEET_2, employeeTime.get(1));

        employeeTime = sut.getTimesheetsByEmployeeId(2L);
        Assert.assertEquals(2, employeeTime.size());

        assertTimesheetsMatch(TIMESHEET_3, employeeTime.get(0));
        assertTimesheetsMatch(TIMESHEET_4, employeeTime.get(1));


    }

    @Test
    public void getTimesheetsByProjectId_returns_list_of_all_timesheets_for_project() {
      List<Timesheet> employeeProjectTime = sut.getTimesheetsByProjectId(1L);
       Assert.assertEquals(3, employeeProjectTime.size());

        assertTimesheetsMatch(TIMESHEET_1, employeeProjectTime.get(0));
        assertTimesheetsMatch(TIMESHEET_2, employeeProjectTime.get(1));
        assertTimesheetsMatch(TIMESHEET_3, employeeProjectTime.get(2));

        employeeProjectTime = sut.getTimesheetsByProjectId(2L);
        Assert.assertEquals(1, employeeProjectTime.size());


        assertTimesheetsMatch(TIMESHEET_4, employeeProjectTime.get(0));
    }

    @Test
    public void createTimesheet_returns_timesheet_with_id_and_expected_values() {
        Timesheet createTime = sut.createTimesheet(time);

        long newId = createTime.getTimesheetId();
        Assert.assertTrue(newId > 0);

        time.setTimesheetId(newId);
        assertTimesheetsMatch(time, createTime);

    }

    @Test
    public void created_timesheet_has_expected_values_when_retrieved() {
        Timesheet createTime = sut.createTimesheet(time);

        long newId = createTime.getTimesheetId();
        Timesheet retrievedTime = sut.getTimesheet(newId);

        assertTimesheetsMatch(createTime, retrievedTime);
    }

    @Test
    public void updated_timesheet_has_expected_values_when_retrieved() {
      Timesheet timeToUpdate = sut.getTimesheet(1L);


      timeToUpdate.setEmployeeId(2L);
      timeToUpdate.setProjectId(1L);
      timeToUpdate.setDateWorked(LocalDate.parse("2021-01-03"));
      timeToUpdate.setHoursWorked(3.0);
      timeToUpdate.setBillable(true);
      timeToUpdate.setDescription("TimeSheet 2");

      sut.updateTimesheet(timeToUpdate);

      Timesheet retrievedTime = sut.getTimesheet(1L);
      assertTimesheetsMatch(timeToUpdate, retrievedTime);


    }

    @Test
    public void deleted_timesheet_cant_be_retrieved() {
        sut.deleteTimesheet(4L);

        Timesheet retrievedTime = sut.getTimesheet(4L);
        Assert.assertNull(retrievedTime);

        List<Timesheet> time = sut.getTimesheetsByEmployeeId(1L);
        Assert.assertEquals(2, time.size());
        assertTimesheetsMatch(TIMESHEET_1, time.get(0));
        assertTimesheetsMatch(TIMESHEET_2, time.get(1));
    }

    @Test
    public void getBillableHours_returns_correct_total() {

        double actual = sut.getBillableHours(2L, 2L);

        double expected = 2.0;

        Assert.assertTrue(expected == actual);

    }

    private void assertTimesheetsMatch(Timesheet expected, Timesheet actual) {
        Assert.assertEquals(expected.getTimesheetId(), actual.getTimesheetId());
        Assert.assertEquals(expected.getEmployeeId(), actual.getEmployeeId());
        Assert.assertEquals(expected.getProjectId(), actual.getProjectId());
        Assert.assertEquals(expected.getDateWorked(), actual.getDateWorked());
        Assert.assertEquals(expected.getHoursWorked(), actual.getHoursWorked(), 0.001);
        Assert.assertEquals(expected.isBillable(), actual.isBillable());
        Assert.assertEquals(expected.getDescription(), actual.getDescription());
    }

}
