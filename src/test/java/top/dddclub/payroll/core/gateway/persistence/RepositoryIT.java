package top.dddclub.payroll.core.gateway.persistence;

import org.junit.Before;
import org.junit.Test;
import top.dddclub.payroll.employeecontext.domain.*;
import top.dddclub.payroll.fixture.EmployeeFixture;
import top.dddclub.payroll.fixture.EntityManagerFixture;
import top.dddclub.payroll.payrollcontext.domain.Salary;
import top.dddclub.payroll.payrollcontext.domain.hourlyemployee.HourlyEmployee;
import top.dddclub.payroll.payrollcontext.domain.hourlyemployee.TimeCard;
import top.dddclub.payroll.payrollcontext.domain.salariedemployee.Absence;
import top.dddclub.payroll.payrollcontext.domain.salariedemployee.SalariedEmployee;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class RepositoryIT {
    private EntityManager entityManager;

    @Before
    public void setUp() {
        entityManager = EntityManagerFixture.createEntityManager();
    }

    @Test
    public void should_query_employee_table_by_id() {
        //given
        String employeeId = "emp200109101000001";
        Repository<Employee, EmployeeId> employeeRepo = createEmployeeRepository();

        //when
        Optional<Employee> optEmployee = employeeRepo.findById(EmployeeId.of(employeeId));

        //then
        assertThat(optEmployee.isPresent()).isTrue();

        Employee employee = optEmployee.get();
        assertThat(employee.id().value()).isEqualTo(employeeId);
        assertThat(employee.name()).isEqualTo("Bruce");
        assertThat(employee.email().value()).isEqualTo("bruce@payroll.com");
        assertThat(employee.isHourly()).isTrue();
        assertThat(employee.isMale()).isTrue();
        assertThat(employee.address()).isEqualTo(new Address("China", "SiChuan", "chengdu", "qingyang avenue", "600000"));
        assertThat(employee.contact()).isEqualTo(Contact.of("15028150000"));
        assertThat(employee.contact().homePhone()).isNull();
        assertThat(employee.boardingDate()).isEqualTo(LocalDate.of(2001, 9, 10));
    }

    @Test
    public void should_query_hourly_employee_and_related_timecards_by_id() {
        //given
        String employeeId = "emp200109101000001";
        Repository<HourlyEmployee, EmployeeId> employeeRepo = createHourlyEmployeeRepository();

        //when
        Optional<HourlyEmployee> optEmployee = employeeRepo.findById(EmployeeId.of(employeeId));

        //then
        assertThat(optEmployee.isPresent()).isTrue();

        HourlyEmployee employee = optEmployee.get();
        assertThat(employee.id().value()).isEqualTo(employeeId);
        assertThat(employee.salaryOfHour()).isEqualTo(Salary.of(100.00));

        List<TimeCard> timeCards = employee.timeCards();
        assertThat(timeCards)
                .isNotNull()
                .hasSize(5);

        TimeCard timeCard = timeCards.get(0);
        assertThat(timeCard.workHours()).isEqualTo(8);
        assertThat(timeCard.getRegularWorkHours()).isEqualTo(8);
        assertThat(timeCard.getOvertimeWorkHours()).isEqualTo(0);
        assertThat(timeCard.isOvertime()).isFalse();
    }

    @Test
    public void should_query_salaried_employee_and_related_absences_by_id() {
        //given
        String employeeId = "emp201110101000003";
        Repository<SalariedEmployee, EmployeeId> employeeRepo = createSalariedEmployeeRepository();

        //when
        Optional<SalariedEmployee> optEmployee = employeeRepo.findById(EmployeeId.of(employeeId));

        //then
        assertThat(optEmployee.isPresent()).isTrue();

        SalariedEmployee employee = optEmployee.get();
        assertThat(employee.id().value()).isEqualTo(employeeId);
        assertThat(employee.salaryOfMonth()).isEqualTo(Salary.of(10000.00));

        List<Absence> absences = employee.absences();
        assertThat(absences).isNotNull().hasSize(4);

        Absence absence = absences.get(0);
        assertThat(absence.isPaidLeave()).isFalse();
    }

    @Test
    public void should_get_all_employees() {
        //given
        Repository<Employee, EmployeeId> repository = createEmployeeRepository();

        //when
        List<Employee> employees = repository.findAll();

        //then
        assertThat(employees).isNotNull().hasSize(5);
    }

    @Test
    public void should_get_all_hourly_employees() {
        //given
        Repository<HourlyEmployee, EmployeeId> repository = createHourlyEmployeeRepository();

        //when
        List<HourlyEmployee> employees = repository.findAll();

        //then
        assertThat(employees).isNotNull().hasSize(2);
    }

    @Test
    public void should_get_all_salaried_employees() {
        //given
        Repository<SalariedEmployee, EmployeeId> repository = createSalariedEmployeeRepository();

        //when
        List<SalariedEmployee> employees = repository.findAll();

        //then
        assertThat(employees).isNotNull().hasSize(1);
    }

    @Test
    public void should_get_all_entities_by_employee_type() {
        Repository<Employee, EmployeeId> repository = createEmployeeRepository();

        List<Employee> hourlyEmployees = repository.findBy((builder, query, root) ->
                builder.equal(root.get("employeeType"), EmployeeType.Hourly)
        );

        assertThat(hourlyEmployees).isNotNull().hasSize(2);
    }

    @Test
    public void should_add_new_employee_then_remove_it() {
        //given
        Repository<Employee, EmployeeId> repository = createEmployeeRepository();
        Employee employee = EmployeeFixture.employeeOf("emp200109101000099", "test", "test@payroll.com", EmployeeType.Hourly);

        repository.saveOrUpdate(employee);

        Optional<Employee> foundEmp = repository.findById(EmployeeId.of("emp200109101000099"));
        assertThat(foundEmp.isPresent()).isTrue();

        repository.delete(employee);
        foundEmp = repository.findById(EmployeeId.of("emp200109101000099"));
        assertThat(foundEmp.isPresent()).isFalse();
    }

    private Repository<SalariedEmployee, EmployeeId> createSalariedEmployeeRepository() {
        return new Repository<>(SalariedEmployee.class, entityManager);
    }

    private Repository<HourlyEmployee, EmployeeId> createHourlyEmployeeRepository() {
        return new Repository<>(HourlyEmployee.class, entityManager);
    }

    private Repository<Employee, EmployeeId> createEmployeeRepository() {
        return new Repository<>(Employee.class, entityManager);
    }
}