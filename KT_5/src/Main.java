import java.io.*;
import java.lang.reflect.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;


class ReflectionSerializer {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    public static String serialize(Object object) throws IllegalAccessException {
        StringBuilder serializedData = new StringBuilder();
        Class<?> clazz = object.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            Object value = field.get(object);
            String formattedValue = formatValue(value);
            serializedData.append("'").append(field.getName()).append("'='").append(formattedValue).append("'\n");
        }

        return serializedData.toString();
    }

    private static String formatValue(Object value) {
        if (value instanceof LocalDate) {
            return ((LocalDate) value).format(DATE_FORMAT);
        } else if (value instanceof LocalTime) {
            return ((LocalTime) value).format(TIME_FORMAT);
        } else if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).format(DATETIME_FORMAT);
        } else {
            return value.toString();
        }
    }
}

class ReflectionDeserializer {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    public static Object deserialize(String serializedData, String className) throws Exception {
        Class<?> clazz = Class.forName(className);
        Object instance = clazz.getDeclaredConstructor().newInstance();

        Map<String, String> fieldValues = parseSerializedData(serializedData);

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            String value = fieldValues.get(field.getName());
            Object parsedValue = parseValue(value, field.getType());
            field.set(instance, parsedValue);
        }

        return instance;
    }

    private static Map<String, String> parseSerializedData(String serializedData) {
        Map<String, String> fieldValues = new HashMap<>();
        String[] lines = serializedData.split("\\n");

        for (String line : lines) {
            String[] parts = line.split("='");
            String key = parts[0].substring(1);
            String value = parts[1].substring(0, parts[1].length() - 1);
            fieldValues.put(key, value);
        }

        return fieldValues;
    }

    private static Object parseValue(String value, Class<?> type) {
        if (type == LocalDate.class) {
            return LocalDate.parse(value, DATE_FORMAT);
        } else if (type == LocalTime.class) {
            return LocalTime.parse(value, TIME_FORMAT);
        } else if (type == LocalDateTime.class) {
            return LocalDateTime.parse(value, DATETIME_FORMAT);
        } else if (type == int.class || type == Integer.class) {
            return Integer.parseInt(value);
        } else {
            return value;
        }
    }
}

class Meeting {
    private String title;
    private String description;
    private int usersLimit;
    private LocalDate startDate;
    private LocalTime startTimeFrom;
    private LocalTime startTimeTo;
    private LocalDateTime registrationAvailableUntil;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getUsersLimit() { return usersLimit; }
    public void setUsersLimit(int usersLimit) { this.usersLimit = usersLimit; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalTime getStartTimeFrom() { return startTimeFrom; }
    public void setStartTimeFrom(LocalTime startTimeFrom) { this.startTimeFrom = startTimeFrom; }

    public LocalTime getStartTimeTo() { return startTimeTo; }
    public void setStartTimeTo(LocalTime startTimeTo) { this.startTimeTo = startTimeTo; }

    public LocalDateTime getRegistrationAvailableUntil() { return registrationAvailableUntil; }
    public void setRegistrationAvailableUntil(LocalDateTime registrationAvailableUntil) { this.registrationAvailableUntil = registrationAvailableUntil; }
}

// Main class
public class Main {
    public static void main(String[] args) {
        String fileName = "data/test.meeting"; //МЕНЯЙТЕ НА СВОЙ ПУТЬ!!!!!!!!!!!!!!!!!!!!!!!!!! МОГУТ БЫТЬ ОШИБКИ.
        String className = "Meeting";

        try {
            String serializedData = readFile(fileName);

            Meeting meeting = (Meeting) ReflectionDeserializer.deserialize(serializedData, className);

            meeting.setTitle("Updated Meeting Title");
            meeting.setUsersLimit(50);

            String updatedSerializedData = ReflectionSerializer.serialize(meeting);
            writeFile(fileName, updatedSerializedData);

            System.out.println("Сохранено.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String readFile(String fileName) throws IOException {
        File file = new File(fileName);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write("'title'='Тестовое собрание'\n");
                writer.write("'description'='Описание тестового собрания'\n");
                writer.write("'usersLimit'='45'\n");
                writer.write("'startDate'='20.08.2023'\n");
                writer.write("'startTimeFrom'='10:00:00'\n");
                writer.write("'startTimeTo'='18:00:00'\n");
                writer.write("'registrationAvailableUntil'='19.08.2023 10:45:30'\n");
            }
        }
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            content.append(line).append("\n");
        }
        reader.close();
        return content.toString();
    }

    private static void writeFile(String fileName, String data) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        writer.write(data);
        writer.close();
    }
}
