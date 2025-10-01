package info.kgeorgiy.ja.vysotin.student;

import info.kgeorgiy.java.advanced.student.GroupName;
import info.kgeorgiy.java.advanced.student.Student;
import info.kgeorgiy.java.advanced.student.StudentQuery;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class StudentDB implements StudentQuery {
    private final Function<Student, Integer> ID = Student::getId;
    private final Function<Student, String> FIRST_NAME = Student::getFirstName;
    private final Function<Student, String> LAST_NAME = Student::getLastName;
    private final Function<Student, GroupName> GROUP = Student::getGroup;
    private final Function<Student, String> FULL_NAME = student -> student.getFirstName() + " " + student.getLastName();
    private final Comparator<Student> NAME_COMPARATOR = Comparator.comparing(LAST_NAME)
            .thenComparing(FIRST_NAME)
            .thenComparing(GROUP);

    @Override
    public List<String> getFirstNames(List<Student> students) {
        return mapElements(students, FIRST_NAME);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return mapElements(students, LAST_NAME);
    }

    @Override
    public List<GroupName> getGroups(List<Student> students) {
        return mapElements(students, GROUP);
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return mapElements(students, FULL_NAME);
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return new TreeSet<>(mapElements(students, FIRST_NAME));
    }

    @Override
    public String getMaxStudentFirstName(List<Student> students) {
        return students.stream()
                .max(Comparator.comparing(ID))
                .map(Student::getFirstName)
                .orElse("");
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return sortStudents(students, Comparator.comparing(ID));
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return sortStudents(students, NAME_COMPARATOR);
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return filterAndSortStudents(students,
                student -> student.getFirstName().equals(name),
                Comparator.comparing(LAST_NAME));
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return filterAndSortStudents(students,
                student -> student.getLastName().equals(name),
                Comparator.comparing(FIRST_NAME));
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, GroupName group) {
        return filterAndSortStudents(students,
                student -> student.getGroup().equals(group),
                NAME_COMPARATOR);
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, GroupName group) {
        return students.stream()
                .filter(student -> student.getGroup().equals(group))
                .collect(Collectors.toMap(LAST_NAME, FIRST_NAME, BinaryOperator.minBy(String::compareTo)));
    }

    private <E> List<E> mapElements(List<Student> students, Function<Student, E> f) {
        return students.stream()
                .map(f)
                .collect(Collectors.toList());
    }

    private List<Student> sortStudents(Collection<Student> students, Comparator<Student> comparator) {
        return students.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }


    private List<Student> filterAndSortStudents(Collection<Student> students, Predicate<Student> predicate, Comparator<Student> comparator) {
        return students.stream()
                .filter(predicate)
                .sorted(comparator)
                .collect(Collectors.toList());
    }
}
