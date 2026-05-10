package com.tana.tana_common.util;

import com.tana.tana_common.constant.CommonConstants;
import com.tana.tana_common.constant.CustomCodeErrors;
import com.tana.tana_common.constant.enums.TanaDateFormat;
import com.tana.tana_common.constant.enums.TanaFileName;
import com.tana.tana_common.constant.exception.TanaException;
import jakarta.mail.internet.MimeMessage;
import org.apache.commons.io.FilenameUtils;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class CommonUtils {


    public static String getUploadImage(Object id, String fileDirectory, String fileName) throws TanaException {
        try {
            java.nio.file.Path fullFileDirectory = Paths.get(fileDirectory, id.toString());
            String fullFileName = fullFileDirectory + File.separator + fileName;

            // Check if the file exists
            java.nio.file.Path filePath = Paths.get(fullFileName);
            if (!Files.exists(filePath)) {
                return null; // Return null if the file does not exist
            }

            byte[] imageBytes = Files.readAllBytes(filePath);
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException o) {
            return null; // Return null in case of an IOException
        }
    }

    /***
     * Upload Image
     *
     * @param id            used to create a folder to append in directory
     * @param fileDirectory the directory where the image file will be saved
     * @param fileName      the file name generated using
     *                      {@link #generateFileName(TanaFileName, MultipartFile)}
     * @param base64Image   the uploaded image file
     * @throws TanaException if an error occurs while saving the file
     */
    public void uploadImage(Object id, String fileDirectory, String fileName, String base64Image) {
        try {
            // Remove the data:image/<image-type>;base64, prefix
            String imageBase64 = base64Image.replaceFirst("data:image/[^;]+;base64,", "");

            // Check if directory already exists
            java.nio.file.Path fullFileDirectory = Paths.get(fileDirectory, id.toString());
            if (!Files.exists(fullFileDirectory)) {
                Files.createDirectories(fullFileDirectory);
            }

            // Decode base64 and process the image as needed
            byte[] bytes = Base64.getDecoder().decode(imageBase64);
            String fullFileName = fullFileDirectory + File.separator + fileName;
            java.nio.file.Path path = Paths.get(fullFileName);

            Files.write(path, bytes);
        } catch (IOException o) {
            throw new TanaException(CustomCodeErrors.GENERIC_ERROR);
        }
    }

    /***
     * Generates a file name
     *
     * @param name the {@link TanaFileName} enum value to use as the base of the
     *             file name
     * @param file the object to generate the file name for
     * @return representing the generated file name
     * @deprecated new update is available please use that instead
     */
    @Deprecated
    public static String generateFileName(TanaFileName name, MultipartFile file) {
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(TanaDateFormat.YYYYMMDDHHMMSS.getValue());
        String formattedDateTime = LocalDateTime.now().format(formatter);
        return String.format(CommonConstants.FILE_NAME,
                name.getValue(),
                formattedDateTime,
                extension);
    }


    /**
     * Parses a comma-separated string into a list of strings.
     * Trims whitespace from each item and handles empty strings.
     *
     * @param input The comma-separated string to parse.
     * @return A list of strings, or an empty list if input is null or empty.
     */
    public static List<String> parseCommaSeparatedString(String input) {
        if (input == null || input.trim().isEmpty()) {
            return new ArrayList<>();
        }
        // Split by comma, trim each part, and filter out empty strings
        return Arrays.stream(input.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    /**
     * Validates the fields of an object based on regular expressions and excluded fields.
     *
     * @param dto The object to validate.
     * @throws TanaException If the input is invalid.
     */
    public <T> T validateObjectData(T dto) throws TanaException {

        try {
            Class<?> currentClass = dto.getClass();
            List<Field> allFields = getAllFields(currentClass);
            for (Field field : allFields) {
                field.setAccessible(true);
                Object value = field.get(dto);
                if (value instanceof Set<?> set) {
                    Set<Object> formattedStringSet = new HashSet<>();
                    for (Object element : set) {
                        formattedStringSet.add(validateObjectData(element));

                    }
                    field.set(dto, formattedStringSet);

                } else if (value instanceof List<?> list) {
                    List<Object> formattedStringSet = new ArrayList<>();
                    for (Object element : list) {
                        Class<?> clazzType = element.getClass();
                        if (clazzType.getPackageName().endsWith(".dto")) {
                            formattedStringSet.add(validateObjectData(element));
                        } else {
                            formattedStringSet.add(validateDefaultDatatype(element, null));
                        }

                    }
                    field.set(dto, formattedStringSet);

                }

            }
            return dto;
        } catch (IllegalAccessException e) {
            throw new TanaException(CustomCodeErrors.GENERIC_ERROR);
        }
    }

    /**
     * Retrieves all fields from the given class and its superclasses.
     *
     * @param clazz The class for which to retrieve fields.
     * @return A list containing all fields from the class and its superclasses.
     */
    private List<Field> getAllFields(Class<?> clazz) {

        List<Field> fields = new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()));
        Class<?> superClass = clazz.getSuperclass();

        while (superClass != null) {
            fields.addAll(Arrays.asList(superClass.getDeclaredFields()));
            superClass = superClass.getSuperclass();

        }
        return fields;

    }

    /**
     * Validates and formats the input value for default datatypes such as String, Integer, Long, etc.
     * Handles special characters and ensures a maximum length of 256 characters.
     *
     * @param stringValue The input value to be validated and formatted.
     * @param requestType The type of request (e.g., REQUEST or RESPONSE).
     * @return The validated and formatted input value.
     * @throws TanaException If special characters are present or if the length exceeds 256 characters.
     */
    public String validateDefaultDatatype(Object stringValue, String requestType) {
        for (String specialChar : CommonConstants.SPECIAL_CHARS) {
            String doubledSpecialChar = CommonConstants.ESC_CHAR + specialChar;

            if (Objects.equals(requestType, CommonConstants.RESPONSE)) {
                if (stringValue.toString().contains(specialChar)) {
                    return stringValue.toString().replace(doubledSpecialChar, specialChar);
                } else if (stringValue.toString().contains(CommonConstants.ESC_CHAR)) {
                    return stringValue.toString().replace(CommonConstants.ESC_CHAR, CommonConstants.EMPTY_STRING);
                }
            }

            // Check if stringValue contains a special character that is not escaped
            if (stringValue.toString().contains(specialChar) && !stringValue.toString().contains(doubledSpecialChar)) {
                throw new TanaException(CustomCodeErrors.GENERIC_ERROR);
            }

        }

        if (stringValue.toString().length() > 255) {
            throw new TanaException(CustomCodeErrors.GENERIC_ERROR);
        }

        return stringValue.toString();
    }

    public String uploadImage(
        String userName,
        Long userId,
        String folderName,
        String uploadDir,
        MultipartFile file
    ) {

        if (file == null || file.isEmpty()) {
            return null;
        }

        try {

            // ✅ sanitize folder name
            String safeFolder = folderName.replaceAll("[^a-zA-Z0-9-_]", "");

            // ✅ create directory safely
            Path dirPath = Paths.get(uploadDir, safeFolder);
            Files.createDirectories(dirPath);

            // ✅ sanitize username
            String safeUsername = userName == null
                ? "user"
                : userName.replaceAll("[^a-zA-Z0-9-_]", "");

            // ✅ get extension safely
            String originalName = file.getOriginalFilename();

            String ext = "";

            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }

            // ✅ unique filename
            String fileName = safeUsername + "-" + userId + ext;

            // ✅ final file path
            Path filePath = dirPath.resolve(fileName);

            // ✅ save file
            Files.copy(
                file.getInputStream(),
                filePath,
                StandardCopyOption.REPLACE_EXISTING
            );

            // ✅ return relative path
            return safeFolder + "/" + fileName;

        } catch (IOException e) {
            throw new RuntimeException("File upload failed", e);
        }
    }

}
