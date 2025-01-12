package io.whitesource.cure;

import io.whitesource.cure.mock.Academy;
import io.whitesource.cure.mock.Campus;
import io.whitesource.cure.mock.inner.Student;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.*;

public class SecureDeserializationTest {

    private static final List<Class<?>> CLASSES = Arrays.asList(
            Academy.class, Campus.class, Student.class, HashSet.class, ArrayList.class);
    private static final Set<String> PACKAGES = new HashSet<>(Arrays.asList("io.whitesource.cure.mock", "java.util"));
    private static final Academy university = createAcademy();

    @Test
    public void secureObjectInputStream_withClassesWhiteList() throws IOException, ClassNotFoundException {
        Set<String> classNames = new HashSet<>();
        CLASSES.forEach(clazz -> classNames.add(clazz.getName()));
        SecureObjectInputStream ois = new SecureObjectInputStream(serialize(university), classNames);
        Assertions.assertEquals(ois.readObject(), university);
    }

    @Test
    public void secureObjectInputStream_withPackageWhiteList() throws IOException, ClassNotFoundException {
        SecureObjectInputStream ois = new SecureObjectInputStream(serialize(university), new HashSet<>(), PACKAGES);
        Assertions.assertEquals(university, ois.readObject());
    }

    @Test
    public void secureObjectInputStream_withLackingWhiteList() throws IOException {
        SecureObjectInputStream ois = new SecureObjectInputStream(
                serialize(university), new HashSet<>(), Collections.singleton("java.util"));
        Assertions.assertThrows(InvalidClassException.class, ois::readObject);
    }

    private static ByteArrayInputStream serialize(Object toSerialize) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(toSerialize);
        oos.flush();
        byte[] exploit = bos.toByteArray();
        return new ByteArrayInputStream(exploit);
    }

    private static Academy createAcademy() {
        List<Student> students = new ArrayList<>();
        students.add(new Student(22.2f, "Scarlet", "Johansson", "BSc"));
        Campus campus1 = new Campus("Stanford", "California");
        Campus campus2 = new Campus("MIT", "Massachusetts");
        Set<Campus> campuses = new HashSet<>(Arrays.asList(campus1, campus2));
        return new Academy(students, "Ivy League", campuses);
    }
}
